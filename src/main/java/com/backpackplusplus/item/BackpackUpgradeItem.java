package com.backpackplusplus.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class BackpackUpgradeItem extends Item {
	public enum UpgradeType {
		MAGNET("magnet", "tooltip.backpackplusplus.upgrade.magnet"),
		AUTO_PICKUP("pickup", "tooltip.backpackplusplus.upgrade.pickup"),
		FEEDING("feeding", "tooltip.backpackplusplus.upgrade.feeding"),
		CRAFTING("crafting", "tooltip.backpackplusplus.upgrade.crafting"),
		SMELTING("smelting", "tooltip.backpackplusplus.upgrade.smelting");

		private final String id;
		private final String tooltipKey;

		UpgradeType(String id, String tooltipKey) {
			this.id = id;
			this.tooltipKey = tooltipKey;
		}

		public String id() {
			return id;
		}

		public String tooltipKey() {
			return tooltipKey;
		}
	}

	private final UpgradeType upgradeType;

	public BackpackUpgradeItem(UpgradeType upgradeType, Properties properties) {
		super(properties.stacksTo(1));
		this.upgradeType = upgradeType;
	}

	public UpgradeType upgradeType() {
		return upgradeType;
	}

	@Override
	public void appendHoverText(
		ItemStack stack,
		TooltipContext context,
		TooltipDisplay display,
		Consumer<Component> tooltipAdder,
		TooltipFlag flag
	) {
		tooltipAdder.accept(Component.translatable(upgradeType.tooltipKey()).withStyle(ChatFormatting.GRAY));
	}
}
