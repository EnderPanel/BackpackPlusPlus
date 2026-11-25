package com.backpackplusplus.registry;

import com.backpackplusplus.BackpackMod;
import com.backpackplusplus.menu.BackpackContainer.BackpackLocator;
import com.backpackplusplus.menu.BackpackMenu;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {
	public static final MenuType<BackpackMenu> BACKPACK_MENU = Registry.register(
		BuiltInRegistries.MENU,
		Identifier.fromNamespaceAndPath(BackpackMod.MOD_ID, "backpack"),
		new ExtendedMenuType<>(BackpackMenu::new, BackpackLocator.STREAM_CODEC)
	);

	private ModMenus() {
	}

	public static void initialize() {
	}
}
