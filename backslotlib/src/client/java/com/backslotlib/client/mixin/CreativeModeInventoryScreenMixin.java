package com.backslotlib.client.mixin;

import com.backslotlib.BackSlotLib;
import com.backslotlib.api.BackSlotApi;
import com.backslotlib.slot.BackSlot;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
	@Unique
	private static final Identifier GENERIC_CONTAINER = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
	@Unique
	private static final Identifier EMPTY_BACK_ICON = Identifier.fromNamespaceAndPath(
		BackSlotLib.MOD_ID,
		"textures/gui/empty_back_slot.png"
	);

	protected CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
	}

	@Shadow
	public abstract boolean isInventoryOpen();

	@ModifyArgs(
		method = "selectTab",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen$SlotWrapper;<init>(Lnet/minecraft/world/inventory/Slot;III)V"
		)
	)
	private void backslotlib$positionCreativeBackSlot(Args args) {
		Slot target = args.get(0);
		if (target instanceof BackSlot) {
			args.set(2, BackSlot.CREATIVE_X);
			args.set(3, BackSlot.CREATIVE_Y);
		}
	}

	@Inject(method = "extractBackground", at = @At("TAIL"))
	private void backslotlib$renderCreativeBackSlot(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		if (!this.isInventoryOpen()) {
			return;
		}
		int sx = this.leftPos + BackSlot.CREATIVE_X - 1;
		int sy = this.topPos + BackSlot.CREATIVE_Y - 1;
		graphics.blit(RenderPipelines.GUI_TEXTURED, GENERIC_CONTAINER, sx, sy, 7.0F, 17.0F, 18, 18, 256, 256);
		if (this.minecraft != null && this.minecraft.player != null && BackSlotApi.get(this.minecraft.player).isEmpty()) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, EMPTY_BACK_ICON, sx + 1, sy + 1, 0.0F, 0.0F, 16, 16, 16, 16);
		}
	}
}
