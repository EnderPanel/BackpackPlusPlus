package com.backpackplusplus;

import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;

public enum BackpackTier {
	LEATHER("leather_backpack", 2, 1, 0xA06540, false, ChatFormatting.GOLD),
	IRON("iron_backpack", 3, 2, 0xA06540, false, ChatFormatting.GRAY),
	GOLD("gold_backpack", 4, 3, 0xA06540, false, ChatFormatting.YELLOW),
	DIAMOND("diamond_backpack", 5, 4, 0xA06540, false, ChatFormatting.AQUA),
	NETHERITE("netherite_backpack", 6, 4, 0x4A3B3D, true, ChatFormatting.DARK_PURPLE);

	public static final int COLUMNS = 9;
	public static final int MAX_UPGRADE_SLOTS = 4;

	private final String id;
	private final int rows;
	private final int upgradeSlots;
	private final int defaultColor;
	private final boolean fireResistant;
	private final ChatFormatting nameColor;
	private final Identifier modelOverlayTexture;

	BackpackTier(String id, int rows, int upgradeSlots, int defaultColor, boolean fireResistant, ChatFormatting nameColor) {
		this.id = id;
		this.rows = rows;
		this.upgradeSlots = upgradeSlots;
		this.defaultColor = defaultColor;
		this.fireResistant = fireResistant;
		this.nameColor = nameColor;
		this.modelOverlayTexture = Identifier.fromNamespaceAndPath(BackpackMod.MOD_ID, "textures/entity/" + id + "_overlay.png");
	}

	public String id() {
		return id;
	}

	public int rows() {
		return rows;
	}

	public int storageSlots() {
		return rows * COLUMNS;
	}

	public int upgradeSlots() {
		return upgradeSlots;
	}

	public int defaultColor() {
		return defaultColor;
	}

	public boolean fireResistant() {
		return fireResistant;
	}

	public ChatFormatting nameColor() {
		return nameColor;
	}

	public Identifier modelOverlayTexture() {
		return modelOverlayTexture;
	}
}
