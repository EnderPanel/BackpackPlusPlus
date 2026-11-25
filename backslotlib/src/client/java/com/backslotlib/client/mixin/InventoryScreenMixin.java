package com.backslotlib.client.mixin;

import com.backslotlib.BackSlotLib;
import com.backslotlib.slot.BackSlot;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractRecipeBookScreen<InventoryMenu> {
	@Unique
	private static final Identifier GENERIC_CONTAINER = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
	@Unique
	private static final Identifier EMPTY_BACK_ICON = Identifier.fromNamespaceAndPath(
		BackSlotLib.MOD_ID,
		"textures/gui/empty_back_slot.png"
	);

	protected InventoryScreenMixin(InventoryMenu menu, Inventory inv, Component title) {
		super(menu, null, inv, title);
	}

	@Inject(method = "extractBackground", at = @At("TAIL"))
	private void backslotlib$renderSurvivalBackSlot(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		for (Slot slot : this.menu.slots) {
			if (slot instanceof BackSlot) {
				int sx = this.leftPos + slot.x - 1;
				int sy = this.topPos + slot.y - 1;
				graphics.blit(RenderPipelines.GUI_TEXTURED, GENERIC_CONTAINER, sx, sy, 7.0F, 17.0F, 18, 18, 256, 256);
				if (!slot.hasItem()) {
					graphics.blit(RenderPipelines.GUI_TEXTURED, EMPTY_BACK_ICON, sx + 1, sy + 1, 0.0F, 0.0F, 16, 16, 16, 16);
				}
				break;
			}
		}
	}
}
