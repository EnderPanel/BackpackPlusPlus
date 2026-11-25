package com.backslotlib.slot;

import com.backslotlib.api.BackSlotApi;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class BackSlot extends Slot {
	public static final int SURVIVAL_X = 77;
	public static final int SURVIVAL_Y = 44;
	public static final int CREATIVE_X = 127;
	public static final int CREATIVE_Y = 20;

	public BackSlot(Container container, int x, int y) {
		super(container, 0, x, y);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return BackSlotApi.canEquip(stack);
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public boolean mayPickup(Player player) {
		ItemStack stack = getItem();
		if (!stack.isEmpty() && !player.isCreative()
			&& EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
			return false;
		}
		return super.mayPickup(player);
	}
}
