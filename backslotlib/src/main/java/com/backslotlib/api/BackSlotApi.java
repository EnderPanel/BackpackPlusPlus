package com.backslotlib.api;

import com.backslotlib.BackSlotLib;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class BackSlotApi {
	public static final TagKey<Item> BACK_WEARABLE_TAG = TagKey.create(
		Registries.ITEM,
		Identifier.fromNamespaceAndPath(BackSlotLib.MOD_ID, "back_wearable")
	);

	public static final AttachmentType<ItemStack> BACK_SLOT = AttachmentRegistry.create(
		Identifier.fromNamespaceAndPath(BackSlotLib.MOD_ID, "back_slot"),
		builder -> builder
			.persistent(ItemStack.OPTIONAL_CODEC)
			.initializer(() -> ItemStack.EMPTY)
			.syncWith(ItemStack.OPTIONAL_STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
	);

	private static final AttachmentType<ItemStack> VISIBLE_BACK_SLOT = AttachmentRegistry.create(
		Identifier.fromNamespaceAndPath(BackSlotLib.MOD_ID, "visible_back_slot"),
		builder -> builder
			.initializer(() -> ItemStack.EMPTY)
			.syncWith(ItemStack.OPTIONAL_STREAM_CODEC, AttachmentSyncPredicate.allButTarget())
	);

	private static final Set<DataComponentType<?>> PRIVATE_COMPONENTS = new CopyOnWriteArraySet<>(List.of(
		DataComponents.CONTAINER,
		DataComponents.BUNDLE_CONTENTS,
		DataComponents.CUSTOM_DATA
	));

	private static final List<Predicate<ItemStack>> PREDICATES = new CopyOnWriteArrayList<>();

	private BackSlotApi() {
	}

	public static void registerItem(Item item) {
		registerPredicate(stack -> stack.is(item));
	}

	public static void registerPredicate(Predicate<ItemStack> predicate) {
		PREDICATES.add(predicate);
	}

	public static void registerPrivateComponent(DataComponentType<?> type) {
		PRIVATE_COMPONENTS.add(type);
	}

	public static ItemStack getVisible(LivingEntity entity) {
		ItemStack stack = get(entity);
		if (!stack.isEmpty() || entity == null) {
			return stack;
		}
		return entity.getAttachedOrElse(VISIBLE_BACK_SLOT, ItemStack.EMPTY);
	}

	public static void updateVisible(Player player) {
		if (player.level().isClientSide()) {
			return;
		}
		ItemStack visible = get(player).copy();
		for (DataComponentType<?> type : PRIVATE_COMPONENTS) {
			visible.remove(type);
		}
		if (!ItemStack.matches(player.getAttachedOrElse(VISIBLE_BACK_SLOT, ItemStack.EMPTY), visible)) {
			player.setAttached(VISIBLE_BACK_SLOT, visible);
		}
	}

	public static boolean canEquip(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		if (stack.is(BACK_WEARABLE_TAG)) {
			return true;
		}
		for (Predicate<ItemStack> predicate : PREDICATES) {
			if (predicate.test(stack)) {
				return true;
			}
		}
		return false;
	}

	public static ItemStack get(LivingEntity entity) {
		if (entity == null) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = entity.getAttachedOrElse(BACK_SLOT, ItemStack.EMPTY);
		return stack != null ? stack : ItemStack.EMPTY;
	}

	public static void set(Player player, ItemStack stack) {
		if (player == null) {
			return;
		}
		ItemStack normalized = (stack == null || stack.isEmpty()) ? ItemStack.EMPTY : stack;
		player.setAttached(BACK_SLOT, normalized);
		updateVisible(player);
	}

	public static void sync(Player player) {
		if (player == null || player.level().isClientSide()) {
			return;
		}
		ItemStack current = get(player);
		player.setAttached(BACK_SLOT, current.isEmpty() ? ItemStack.EMPTY : current.copy());
		updateVisible(player);
	}

	public static InteractionResult tryEquip(Player player, ItemStack handStack) {
		if (!canEquip(handStack)) {
			return InteractionResult.PASS;
		}
		ItemStack existing = get(player);
		if (!existing.isEmpty() && EnchantmentHelper.has(existing, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) && !player.isCreative()) {
			return InteractionResult.FAIL;
		}

		if (!player.level().isClientSide()) {
			ItemStack toEquip = handStack.copy();
			toEquip.setCount(1);
			handStack.shrink(1);

			if (!existing.isEmpty() && !player.getInventory().add(existing.copy())) {
				player.drop(existing.copy(), false, Prediction.SERVER_ONLY);
			}

			set(player, toEquip);
			player.level().playSound(
				null,
				player.getX(),
				player.getY(),
				player.getZ(),
				SoundEvents.ARMOR_EQUIP_LEATHER.value(),
				player.getSoundSource(),
				1.0F,
				1.0F
			);
		}
		return InteractionResult.SUCCESS;
	}

	public static void initialize() {
	}
}
