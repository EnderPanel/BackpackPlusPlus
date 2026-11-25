package com.backpackplusplus.item;

import com.backpackplusplus.BackpackTier;
import com.backpackplusplus.menu.BackpackContainer.BackpackLocator;
import com.backpackplusplus.menu.BackpackMenu;
import com.backpackplusplus.registry.ModDataComponents;
import com.backslotlib.api.BackSlotApi;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class BackpackItem extends Item {
	private final BackpackTier tier;

	public BackpackItem(BackpackTier tier, Properties properties) {
		super(buildProperties(tier, properties));
		this.tier = tier;
	}

	private static Properties buildProperties(BackpackTier tier, Properties properties) {
		Properties configured = properties
			.stacksTo(1)
			.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
			.component(ModDataComponents.BACKPACK_UPGRADES, ItemContainerContents.EMPTY);
		if (tier.fireResistant()) {
			configured = configured.fireResistant();
		}
		return configured;
	}

	public BackpackTier tier() {
		return tier;
	}

	@Override
	public boolean canFitInsideContainerItems() {
		return false;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player.isShiftKeyDown()) {
			return BackSlotApi.tryEquip(player, stack);
		}

		if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
			int invSlot = hand == InteractionHand.MAIN_HAND
				? player.getInventory().getSelectedSlot()
				: Inventory.SLOT_OFFHAND;
			openBackpack(serverPlayer, new BackpackLocator(false, invSlot, tier.ordinal()));
		}
		return InteractionResult.SUCCESS;
	}

	public static void openBackpack(ServerPlayer player, BackpackLocator locator) {
		ItemStack stack = locator.get(player);
		if (!(stack.getItem() instanceof BackpackItem backpack)) {
			return;
		}
		if (!stack.has(ModDataComponents.BACKPACK_ID)) {
			stack.set(ModDataComponents.BACKPACK_ID, UUID.randomUUID());
			if (locator.backSlot()) {
				BackSlotApi.sync(player);
			}
		}
		player.level().playSound(
			null,
			player.getX(),
			player.getY(),
			player.getZ(),
			SoundEvents.ARMOR_EQUIP_LEATHER.value(),
			player.getSoundSource(),
			0.75F,
			1.15F
		);
		player.openMenu(new ExtendedMenuProvider<BackpackLocator>() {
			@Override
			public BackpackLocator getScreenOpeningData(ServerPlayer p) {
				return new BackpackLocator(locator.backSlot(), locator.inventorySlot(), backpack.tier().ordinal());
			}

			@Override
			public Component getDisplayName() {
				return stack.getHoverName();
			}

			@Override
			public AbstractContainerMenu createMenu(int syncId, Inventory playerInv, Player p) {
				return new BackpackMenu(syncId, playerInv, new BackpackLocator(locator.backSlot(), locator.inventorySlot(), backpack.tier().ordinal()));
			}
		});
	}

	public static BackpackLocator findBestBackpack(Player player) {
		ItemStack back = BackSlotApi.get(player);
		if (back.getItem() instanceof BackpackItem backBackpack) {
			return new BackpackLocator(true, -1, backBackpack.tier().ordinal());
		}
		Inventory inv = player.getInventory();
		int selected = inv.getSelectedSlot();
		if (inv.getItem(selected).getItem() instanceof BackpackItem selectedBackpack) {
			return new BackpackLocator(false, selected, selectedBackpack.tier().ordinal());
		}
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack candidate = inv.getItem(i);
			if (candidate.getItem() instanceof BackpackItem bp) {
				return new BackpackLocator(false, i, bp.tier().ordinal());
			}
		}
		return null;
	}

	public static boolean hasUpgrade(ItemStack backpackStack, BackpackUpgradeItem.UpgradeType type) {
		if (!(backpackStack.getItem() instanceof BackpackItem)) {
			return false;
		}
		ItemContainerContents upgrades = backpackStack.getOrDefault(ModDataComponents.BACKPACK_UPGRADES, ItemContainerContents.EMPTY);
		for (ItemStackTemplate template : upgrades.nonEmptyItems()) {
			if (template.item().value() instanceof BackpackUpgradeItem upgrade && upgrade.upgradeType() == type) {
				return true;
			}
		}
		return false;
	}

	public static boolean tryInsert(ItemStack backpackStack, ItemStack incoming) {
		if (incoming.isEmpty() || !incoming.getItem().canFitInsideContainerItems()) {
			return false;
		}
		if (!(backpackStack.getItem() instanceof BackpackItem backpack)) {
			return false;
		}

		int capacity = backpack.tier().storageSlots();
		NonNullList<ItemStack> items = NonNullList.withSize(capacity, ItemStack.EMPTY);
		backpackStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(items);

		boolean changed = insertInto(items, incoming);
		if (changed) {
			backpackStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
		}
		return changed;
	}

	public static boolean insertInto(NonNullList<ItemStack> items, ItemStack incoming) {
		boolean changed = false;
		for (int i = 0; i < items.size() && !incoming.isEmpty(); i++) {
			ItemStack existing = items.get(i);
			if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, incoming)) {
				int space = existing.getMaxStackSize() - existing.getCount();
				if (space > 0) {
					int move = Math.min(space, incoming.getCount());
					existing.grow(move);
					incoming.shrink(move);
					changed = true;
				}
			}
		}

		for (int i = 0; i < items.size() && !incoming.isEmpty(); i++) {
			if (items.get(i).isEmpty()) {
				int move = Math.min(incoming.getMaxStackSize(), incoming.getCount());
				items.set(i, incoming.split(move));
				changed = true;
			}
		}
		return changed;
	}

	public static int getColor(ItemStack stack) {
		if (stack.getItem() instanceof BackpackItem backpack) {
			return DyedItemColor.getOrDefault(stack, backpack.tier().defaultColor());
		}
		return DyedItemColor.LEATHER_COLOR;
	}

	@Override
	public void appendHoverText(
		ItemStack stack,
		TooltipContext context,
		TooltipDisplay display,
		Consumer<Component> tooltipAdder,
		TooltipFlag flag
	) {
		ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
		long usedSlots = contents.nonEmptyItemCopyStream().count();
		tooltipAdder.accept(
			Component.translatable("tooltip.backpackplusplus.capacity", usedSlots, tier.storageSlots())
				.withStyle(tier.nameColor())
		);

		ItemContainerContents upgrades = stack.getOrDefault(ModDataComponents.BACKPACK_UPGRADES, ItemContainerContents.EMPTY);
		List<ItemStack> upgradeList = upgrades.nonEmptyItemCopyStream().toList();
		if (!upgradeList.isEmpty()) {
			StringBuilder names = new StringBuilder();
			for (int i = 0; i < upgradeList.size(); i++) {
				if (i > 0) {
					names.append(", ");
				}
				names.append(upgradeList.get(i).getHoverName().getString());
			}
			tooltipAdder.accept(
				Component.translatable("tooltip.backpackplusplus.upgrades", names.toString())
					.withStyle(ChatFormatting.GREEN)
			);
		} else {
			tooltipAdder.accept(
				Component.translatable("tooltip.backpackplusplus.upgrade_slots", tier.upgradeSlots())
					.withStyle(ChatFormatting.DARK_GRAY)
			);
		}

		tooltipAdder.accept(Component.translatable("tooltip.backpackplusplus.usage_hint").withStyle(ChatFormatting.GRAY));
	}
}
