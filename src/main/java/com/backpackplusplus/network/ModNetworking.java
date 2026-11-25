package com.backpackplusplus.network;

import com.backpackplusplus.BackpackMod;
import com.backpackplusplus.item.BackpackItem;
import com.backpackplusplus.menu.BackpackContainer.BackpackLocator;
import com.backpackplusplus.menu.BackpackMenu;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;

public final class ModNetworking {
	private ModNetworking() {
	}

	public record OpenBackpackPayload() implements CustomPacketPayload {
		public static final OpenBackpackPayload INSTANCE = new OpenBackpackPayload();
		public static final Type<OpenBackpackPayload> TYPE = new Type<>(
			Identifier.fromNamespaceAndPath(BackpackMod.MOD_ID, "open_backpack")
		);
		public static final StreamCodec<ByteBuf, OpenBackpackPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record OpenPortableCraftPayload() implements CustomPacketPayload {
		public static final OpenPortableCraftPayload INSTANCE = new OpenPortableCraftPayload();
		public static final Type<OpenPortableCraftPayload> TYPE = new Type<>(
			Identifier.fromNamespaceAndPath(BackpackMod.MOD_ID, "open_portable_craft")
		);
		public static final StreamCodec<ByteBuf, OpenPortableCraftPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public static void initialize() {
		PayloadTypeRegistry.serverboundPlay().register(OpenBackpackPayload.TYPE, OpenBackpackPayload.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(OpenPortableCraftPayload.TYPE, OpenPortableCraftPayload.STREAM_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(OpenBackpackPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			if (player.isSpectator() || !player.isAlive()) {
				return;
			}
			BackpackLocator locator = BackpackItem.findBestBackpack(player);
			if (locator != null) {
				BackpackItem.openBackpack(player, locator);
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(OpenPortableCraftPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			if (player.containerMenu instanceof BackpackMenu backpackMenu && backpackMenu.hasCraftingUpgrade()) {
				player.openMenu(new SimpleMenuProvider(
					(syncId, inv, p) -> new CraftingMenu(syncId, inv, ContainerLevelAccess.create(player.level(), player.blockPosition())) {
						@Override
						public boolean stillValid(Player p2) {
							return true;
						}
					},
					Component.translatable("container.crafting")
				));
			}
		});
	}
}
