package com.backslotlib.mixin;

import com.backslotlib.slot.BackSlot;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
	@Shadow
	public ServerPlayer player;

	@Inject(method = "handleSetCreativeModeSlot", at = @At("TAIL"))
	private void backslotlib$handleCreativeBackSlot(ServerboundSetCreativeModeSlotPacket packet, CallbackInfo ci) {
		if (!this.player.hasInfiniteMaterials()) {
			return;
		}
		int slotNum = packet.slotNum();
		if (slotNum < 0 || slotNum >= this.player.inventoryMenu.slots.size()) {
			return;
		}
		Slot slot = this.player.inventoryMenu.getSlot(slotNum);
		if (!(slot instanceof BackSlot)) {
			return;
		}
		ItemStack stack = packet.itemStack();
		if (!stack.isItemEnabled(this.player.level().enabledFeatures())) {
			return;
		}
		if (stack.isEmpty() || (stack.getCount() <= slot.getMaxStackSize(stack) && slot.mayPlace(stack))) {
			slot.setByPlayer(stack);
			this.player.inventoryMenu.broadcastChanges();
		}
	}
}
