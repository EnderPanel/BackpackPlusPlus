package com.backslotlib.mixin;

import com.backslotlib.api.BackSlotApi;
import com.backslotlib.slot.BackSlot;
import com.backslotlib.slot.BackSlotContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends AbstractContainerMenu {
	protected InventoryMenuMixin(MenuType<?> menuType, int containerId) {
		super(menuType, containerId);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void backslotlib$addBackSlot(Inventory playerInventory, boolean active, Player owner, CallbackInfo ci) {
		this.addSlot(new BackSlot(new BackSlotContainer(owner), BackSlot.SURVIVAL_X, BackSlot.SURVIVAL_Y));
	}

	@Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
	private void backslotlib$quickMoveBackSlot(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
		int backSlotIndex = -1;
		for (int i = 0; i < this.slots.size(); i++) {
			if (this.slots.get(i) instanceof BackSlot) {
				backSlotIndex = i;
				break;
			}
		}
		if (backSlotIndex < 0) {
			return;
		}

		Slot slot = this.slots.get(index);
		if (!slot.hasItem()) {
			return;
		}

		ItemStack stack = slot.getItem();
		if (index == backSlotIndex) {
			ItemStack copy = stack.copy();
			if (!this.moveItemStackTo(stack, InventoryMenu.INV_SLOT_START, InventoryMenu.USE_ROW_SLOT_END, false)) {
				cir.setReturnValue(ItemStack.EMPTY);
				return;
			}
			if (stack.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY, copy);
			} else {
				slot.setChanged();
			}
			slot.onTake(player, stack);
			cir.setReturnValue(copy);
		} else if (index >= InventoryMenu.INV_SLOT_START && index < InventoryMenu.USE_ROW_SLOT_END
			&& BackSlotApi.canEquip(stack)
			&& !this.slots.get(backSlotIndex).hasItem()) {
			ItemStack copy = stack.copy();
			if (this.moveItemStackTo(stack, backSlotIndex, backSlotIndex + 1, false)) {
				if (stack.isEmpty()) {
					slot.setByPlayer(ItemStack.EMPTY, copy);
				} else {
					slot.setChanged();
				}
				slot.onTake(player, stack);
				cir.setReturnValue(copy);
			}
		}
	}
}
