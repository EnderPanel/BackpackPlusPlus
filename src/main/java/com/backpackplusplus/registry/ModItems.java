package com.backpackplusplus.registry;

import com.backpackplusplus.BackpackMod;
import com.backpackplusplus.BackpackTier;
import com.backpackplusplus.item.BackpackItem;
import com.backpackplusplus.item.BackpackUpgradeItem;
import com.backpackplusplus.item.BackpackUpgradeItem.UpgradeType;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ModItems {
	public static final Map<BackpackTier, BackpackItem> BACKPACKS = new EnumMap<>(BackpackTier.class);
	public static final Map<UpgradeType, BackpackUpgradeItem> UPGRADES = new EnumMap<>(UpgradeType.class);

	public static final BackpackItem LEATHER_BACKPACK = registerBackpack(BackpackTier.LEATHER);
	public static final BackpackItem IRON_BACKPACK = registerBackpack(BackpackTier.IRON);
	public static final BackpackItem GOLD_BACKPACK = registerBackpack(BackpackTier.GOLD);
	public static final BackpackItem DIAMOND_BACKPACK = registerBackpack(BackpackTier.DIAMOND);
	public static final BackpackItem NETHERITE_BACKPACK = registerBackpack(BackpackTier.NETHERITE);

	public static final BackpackUpgradeItem MAGNET_UPGRADE = registerUpgrade(UpgradeType.MAGNET, "magnet_upgrade");
	public static final BackpackUpgradeItem PICKUP_UPGRADE = registerUpgrade(UpgradeType.AUTO_PICKUP, "pickup_upgrade");
	public static final BackpackUpgradeItem FEEDING_UPGRADE = registerUpgrade(UpgradeType.FEEDING, "feeding_upgrade");
	public static final BackpackUpgradeItem CRAFTING_UPGRADE = registerUpgrade(UpgradeType.CRAFTING, "crafting_upgrade");
	public static final BackpackUpgradeItem SMELTING_UPGRADE = registerUpgrade(UpgradeType.SMELTING, "smelting_upgrade");

	public static final ResourceKey<CreativeModeTab> BACKPACK_TAB_KEY = ResourceKey.create(
		Registries.CREATIVE_MODE_TAB,
		Identifier.fromNamespaceAndPath(BackpackMod.MOD_ID, "backpacks")
	);

	public static final CreativeModeTab BACKPACK_TAB = Registry.register(
		BuiltInRegistries.CREATIVE_MODE_TAB,
		BACKPACK_TAB_KEY,
		FabricCreativeModeTab.builder()
			.icon(() -> new ItemStack(LEATHER_BACKPACK))
			.title(Component.translatable("itemGroup.backpackplusplus.backpacks"))
			.displayItems((context, entries) -> {
				for (BackpackTier tier : BackpackTier.values()) {
					entries.accept(BACKPACKS.get(tier));
				}
				for (UpgradeType type : UpgradeType.values()) {
					entries.accept(UPGRADES.get(type));
				}
			})
			.build()
	);

	private ModItems() {
	}

	private static BackpackItem registerBackpack(BackpackTier tier) {
		BackpackItem item = register(tier.id(), props -> new BackpackItem(tier, props));
		BACKPACKS.put(tier, item);
		return item;
	}

	private static BackpackUpgradeItem registerUpgrade(UpgradeType type, String id) {
		BackpackUpgradeItem item = register(id, props -> new BackpackUpgradeItem(type, props));
		UPGRADES.put(type, item);
		return item;
	}

	private static <T extends Item> T register(String id, Function<Item.Properties, T> factory) {
		Identifier identifier = Identifier.fromNamespaceAndPath(BackpackMod.MOD_ID, id);
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, identifier);
		T item = factory.apply(new Item.Properties().setId(key));
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}

	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
			for (BackpackTier tier : BackpackTier.values()) {
				entries.accept(BACKPACKS.get(tier));
			}
			for (UpgradeType type : UpgradeType.values()) {
				entries.accept(UPGRADES.get(type));
			}
		});
	}
}
