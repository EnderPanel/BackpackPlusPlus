package com.backpackplusplus.client;

import com.backpackplusplus.BackpackMod;
import com.backpackplusplus.client.render.BackpackLayer;
import com.backpackplusplus.client.render.BackpackModel;
import com.backpackplusplus.client.screen.BackpackScreen;
import com.backpackplusplus.item.BackpackItem;
import com.backpackplusplus.network.ModNetworking.OpenBackpackPayload;
import com.backpackplusplus.registry.ModMenus;
import com.backslotlib.client.api.BackSlotClientApi;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.Identifier;

public class BackpackModClient implements ClientModInitializer {
	private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(
		Identifier.fromNamespaceAndPath(BackpackMod.MOD_ID, "keybinds")
	);

	private static KeyMapping openBackpackKey;

	@Override
	public void onInitializeClient() {
		MenuScreens.register(ModMenus.BACKPACK_MENU, BackpackScreen::new);

		openBackpackKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.backpackplusplus.open_backpack",
			InputConstants.Type.KEYBOARD,
			InputConstants.KEY_B,
			KEY_CATEGORY
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openBackpackKey.consumeClick()) {
				if (client.player != null) {
					ClientPlayNetworking.send(OpenBackpackPayload.INSTANCE);
				}
			}
		});

		ModelLayerRegistry.registerModelLayer(BackpackModel.LAYER_LOCATION, BackpackModel::createBodyLayer);

		BackSlotClientApi.registerRenderer(
			stack -> stack.getItem() instanceof BackpackItem,
			context -> {
				BackpackLayer layer = new BackpackLayer(context);
				return layer::render;
			}
		);
	}
}
