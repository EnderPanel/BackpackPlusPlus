package com.backpackplusplus.client.screen;

import com.backpackplusplus.menu.BackpackMenu;
import com.backpackplusplus.network.ModNetworking.OpenPortableCraftPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BackpackScreen extends AbstractContainerScreen<BackpackMenu> {
	private static final Identifier CONTAINER_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
	private static final ItemStack CRAFTING_ICON = new ItemStack(Items.CRAFTING_TABLE);

	private final int containerRows;
	private Button craftButton;

	public BackpackScreen(BackpackMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title, BackpackMenu.GUI_WIDTH, 114 + menu.tier().rows() * 18);
		this.containerRows = menu.tier().rows();
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	protected void init() {
		super.init();
		int buttonX = this.leftPos + BackpackMenu.UPGRADE_SLOT_X - 1;
		int buttonY = this.topPos + BackpackMenu.UPGRADE_SLOT_Y + this.menu.tier().upgradeSlots() * 18 + 4;
		this.craftButton = Button.builder(Component.empty(), button -> ClientPlayNetworking.send(OpenPortableCraftPayload.INSTANCE))
			.bounds(buttonX, buttonY, 18, 18)
			.tooltip(Tooltip.create(Component.translatable("tooltip.backpackplusplus.portable_craft")))
			.build();
		this.craftButton.visible = this.menu.hasCraftingUpgrade();
		addRenderableWidget(this.craftButton);
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		if (this.craftButton != null) {
			this.craftButton.visible = this.menu.hasCraftingUpgrade();
		}
	}

	@Override
	public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractContents(graphics, mouseX, mouseY, partialTick);
		if (this.craftButton != null && this.craftButton.visible) {
			graphics.item(CRAFTING_ICON, this.craftButton.getX() + 1, this.craftButton.getY() + 1);
		}
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);

		int x = this.leftPos;
		int y = this.topPos;

		graphics.blit(
			RenderPipelines.GUI_TEXTURED,
			CONTAINER_BACKGROUND,
			x,
			y,
			0.0F,
			0.0F,
			BackpackMenu.STORAGE_PANEL_WIDTH,
			this.containerRows * 18 + 17,
			256,
			256
		);
		graphics.blit(
			RenderPipelines.GUI_TEXTURED,
			CONTAINER_BACKGROUND,
			x,
			y + this.containerRows * 18 + 17,
			0.0F,
			126.0F,
			BackpackMenu.STORAGE_PANEL_WIDTH,
			96,
			256,
			256
		);

		int upgradeCount = this.menu.tier().upgradeSlots();
		int panelX = x + BackpackMenu.STORAGE_PANEL_WIDTH - 3;
		int panelY = y + 6;
		int panelW = 28;
		int panelH = 18 + upgradeCount * 18 + (this.menu.hasCraftingUpgrade() ? 22 : 4);

		graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFF373737);
		graphics.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + panelH - 1, 0xFFC6C6C6);
		graphics.fill(panelX + 1, panelY + 1, panelX + panelW - 2, panelY + 2, 0xFFFFFFFF);
		graphics.fill(panelX + panelW - 2, panelY + 2, panelX + panelW - 1, panelY + panelH - 1, 0xFF555555);
		graphics.fill(panelX + 1, panelY + panelH - 2, panelX + panelW - 1, panelY + panelH - 1, 0xFF555555);

		for (int i = 0; i < upgradeCount; i++) {
			int slotX = x + BackpackMenu.UPGRADE_SLOT_X - 1;
			int slotY = y + BackpackMenu.UPGRADE_SLOT_Y + i * 18 - 1;
			graphics.blit(
				RenderPipelines.GUI_TEXTURED,
				CONTAINER_BACKGROUND,
				slotX,
				slotY,
				7.0F,
				17.0F,
				18,
				18,
				256,
				256
			);
		}
	}
}
