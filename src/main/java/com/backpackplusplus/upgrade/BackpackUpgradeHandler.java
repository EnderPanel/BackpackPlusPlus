package com.backpackplusplus.upgrade;

import com.backpackplusplus.BackpackMod;
import com.backpackplusplus.item.BackpackItem;
import com.backpackplusplus.item.BackpackUpgradeItem.UpgradeType;
import com.backpackplusplus.menu.BackpackMenu;
import com.backpackplusplus.registry.ModDataComponents;
import com.backslotlib.api.BackSlotApi;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BackpackUpgradeHandler {
	private static final double MAGNET_RADIUS = 6.5;
	private static final double MAGNET_PULL_SPEED = 0.42;

	private static final Set<Item> UNSAFE_AUTO_FOODS = Set.of(
		Items.ROTTEN_FLESH,
		Items.SPIDER_EYE,
		Items.PUFFERFISH,
		Items.POISONOUS_POTATO,
		Items.CHORUS_FRUIT,
		Items.SUSPICIOUS_STEW,
		Items.ENCHANTED_GOLDEN_APPLE,
		Items.GOLDEN_APPLE,
		Items.CHICKEN
	);

	private static final int SMELT_INTERVAL = 200;

	private static final TagKey<Item> AUTO_SMELTABLE = TagKey.create(
		Registries.ITEM,
		Identifier.fromNamespaceAndPath(BackpackMod.MOD_ID, "auto_smeltable")
	);

	private static final Map<Item, Integer> SMELT_FUELS = Map.of(
		Items.COAL, 8,
		Items.CHARCOAL, 8,
		Items.COAL_BLOCK, 80,
		Items.BLAZE_ROD, 12,
		Items.DRIED_KELP_BLOCK, 20
	);

	private BackpackUpgradeHandler() {
	}

	public static void initialize() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (!player.isAlive() || player.isSpectator()) {
					continue;
				}
				tickPlayer(player);
			}
		});
	}

	private static void tickPlayer(ServerPlayer player) {
		boolean hasMagnet = false;
		boolean hasFeeding = false;

		ItemStack back = BackSlotApi.get(player);
		if (back.getItem() instanceof BackpackItem) {
			hasMagnet |= BackpackItem.hasUpgrade(back, UpgradeType.MAGNET);
			hasFeeding |= BackpackItem.hasUpgrade(back, UpgradeType.FEEDING);
		}

		Inventory inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize() && (!hasMagnet || !hasFeeding); i++) {
			ItemStack stack = inv.getItem(i);
			if (stack.getItem() instanceof BackpackItem) {
				hasMagnet |= BackpackItem.hasUpgrade(stack, UpgradeType.MAGNET);
				hasFeeding |= BackpackItem.hasUpgrade(stack, UpgradeType.FEEDING);
			}
		}

		if (hasMagnet && (player.tickCount % 2 == 0)) {
			pullNearbyItems(player);
		}

		if (player.containerMenu instanceof BackpackMenu) {
			return;
		}

		if (hasFeeding && (player.tickCount % 20 == 0)) {
			tryAutoFeed(player);
		}

		if (player.tickCount % SMELT_INTERVAL == 0) {
			smeltInBackpacks(player);
		}
	}

	private static void smeltInBackpacks(ServerPlayer player) {
		if (trySmeltOne(player, BackSlotApi.get(player))) {
			BackSlotApi.sync(player);
		}
		Inventory inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			trySmeltOne(player, inv.getItem(i));
		}
	}

	private static boolean trySmeltOne(ServerPlayer player, ItemStack backpackStack) {
		if (!(backpackStack.getItem() instanceof BackpackItem backpack)
			|| !BackpackItem.hasUpgrade(backpackStack, UpgradeType.SMELTING)) {
			return false;
		}

		int capacity = backpack.tier().storageSlots();
		NonNullList<ItemStack> items = NonNullList.withSize(capacity, ItemStack.EMPTY);
		backpackStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(items);

		ServerLevel level = player.level();
		for (int i = 0; i < capacity; i++) {
			ItemStack candidate = items.get(i);
			if (candidate.isEmpty() || !candidate.is(AUTO_SMELTABLE)) {
				continue;
			}
			SingleRecipeInput input = new SingleRecipeInput(candidate);
			var recipe = level.recipeAccess().getRecipeFor(RecipeType.SMELTING, input, level);
			if (recipe.isEmpty()) {
				continue;
			}
			ItemStack result = recipe.get().value().assemble(input);

			NonNullList<ItemStack> updated = NonNullList.withSize(capacity, ItemStack.EMPTY);
			for (int j = 0; j < capacity; j++) {
				updated.set(j, items.get(j).copy());
			}
			updated.get(i).shrink(1);

			int charge = backpackStack.getOrDefault(ModDataComponents.SMELT_CHARGE, 0);
			if (charge <= 0) {
				charge = burnFuel(updated);
				if (charge <= 0) {
					return false;
				}
			}

			BackpackItem.insertInto(updated, result);
			if (!result.isEmpty()) {
				continue;
			}

			backpackStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(updated));
			backpackStack.set(ModDataComponents.SMELT_CHARGE, charge - 1);
			return true;
		}
		return false;
	}

	private static int burnFuel(NonNullList<ItemStack> items) {
		for (ItemStack stack : items) {
			Integer value = stack.isEmpty() ? null : SMELT_FUELS.get(stack.getItem());
			if (value != null) {
				stack.shrink(1);
				return value;
			}
		}
		return 0;
	}

	private static void pullNearbyItems(ServerPlayer player) {
		ServerLevel level = player.level();
		AABB box = player.getBoundingBox().inflate(MAGNET_RADIUS);
		List<ItemEntity> items = level.getEntitiesOfClass(
			ItemEntity.class,
			box,
			item -> item.isAlive() && !item.hasPickUpDelay() && !item.getItem().isEmpty()
		);
		if (items.isEmpty()) {
			return;
		}

		Vec3 target = player.position().add(0.0, player.getBbHeight() * 0.5, 0.0);
		for (ItemEntity item : items) {
			Vec3 delta = target.subtract(item.position());
			if (delta.lengthSqr() < 0.04) {
				continue;
			}
			item.setDeltaMovement(delta.normalize().scale(MAGNET_PULL_SPEED));
			item.syncVelocity = true;
		}
	}

	private static void tryAutoFeed(ServerPlayer player) {
		FoodData foodData = player.getFoodData();
		if (!foodData.needsFood() || foodData.getFoodLevel() > 18) {
			return;
		}

		ItemStack back = BackSlotApi.get(player);
		if (tryFeedFromBackpack(player, back)) {
			BackSlotApi.sync(player);
			return;
		}

		Inventory inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			if (tryFeedFromBackpack(player, inv.getItem(i))) {
				return;
			}
		}
	}

	private static boolean tryFeedFromBackpack(ServerPlayer player, ItemStack backpackStack) {
		if (!(backpackStack.getItem() instanceof BackpackItem backpack)) {
			return false;
		}
		if (!BackpackItem.hasUpgrade(backpackStack, UpgradeType.FEEDING)) {
			return false;
		}

		int capacity = backpack.tier().storageSlots();
		NonNullList<ItemStack> items = NonNullList.withSize(capacity, ItemStack.EMPTY);
		backpackStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(items);

		for (int i = 0; i < capacity; i++) {
			ItemStack candidate = items.get(i);
			if (candidate.isEmpty() || UNSAFE_AUTO_FOODS.contains(candidate.getItem())) {
				continue;
			}
			FoodProperties food = candidate.get(DataComponents.FOOD);
			if (food == null || food.nutrition() <= 0 || !candidate.has(DataComponents.CONSUMABLE)) {
				continue;
			}

			ItemStack remainder = candidate.finishUsingItem(player.level(), player);
			items.set(i, remainder);
			backpackStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
			return true;
		}
		return false;
	}

	public static boolean tryAutoPickup(Player player, ItemStack stack) {
		if (stack.isEmpty() || !stack.getItem().canFitInsideContainerItems() || player.containerMenu instanceof BackpackMenu) {
			return false;
		}

		boolean changed = false;
		ItemStack back = BackSlotApi.get(player);
		if (BackpackItem.hasUpgrade(back, UpgradeType.AUTO_PICKUP) && BackpackItem.tryInsert(back, stack)) {
			BackSlotApi.sync(player);
			changed = true;
		}

		if (stack.isEmpty()) {
			return changed;
		}

		Inventory inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize() && !stack.isEmpty(); i++) {
			ItemStack candidate = inv.getItem(i);
			if (BackpackItem.hasUpgrade(candidate, UpgradeType.AUTO_PICKUP)) {
				changed |= BackpackItem.tryInsert(candidate, stack);
			}
		}
		return changed;
	}
}
