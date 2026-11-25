package com.backpackplusplus.registry;

import com.backpackplusplus.BackpackMod;
import com.mojang.serialization.Codec;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.ItemContainerContents;

public final class ModDataComponents {
	public static final DataComponentType<ItemContainerContents> BACKPACK_UPGRADES = Registry.register(
		BuiltInRegistries.DATA_COMPONENT_TYPE,
		Identifier.fromNamespaceAndPath(BackpackMod.MOD_ID, "backpack_upgrades"),
		DataComponentType.<ItemContainerContents>builder()
			.persistent(ItemContainerContents.CODEC)
			.networkSynchronized(ItemContainerContents.STREAM_CODEC)
			.cacheEncoding()
			.build()
	);

	public static final DataComponentType<Integer> SMELT_CHARGE = Registry.register(
		BuiltInRegistries.DATA_COMPONENT_TYPE,
		Identifier.fromNamespaceAndPath(BackpackMod.MOD_ID, "smelt_charge"),
		DataComponentType.<Integer>builder()
			.persistent(Codec.INT)
			.networkSynchronized(ByteBufCodecs.VAR_INT)
			.build()
	);

	public static final DataComponentType<UUID> BACKPACK_ID = Registry.register(
		BuiltInRegistries.DATA_COMPONENT_TYPE,
		Identifier.fromNamespaceAndPath(BackpackMod.MOD_ID, "backpack_id"),
		DataComponentType.<UUID>builder()
			.persistent(UUIDUtil.CODEC)
			.networkSynchronized(UUIDUtil.STREAM_CODEC)
			.build()
	);

	private ModDataComponents() {
	}

	public static void initialize() {
	}
}
