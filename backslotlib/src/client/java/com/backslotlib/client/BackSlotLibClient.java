package com.backslotlib.client;

import com.backslotlib.client.render.BackSlotRenderLayer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;

public class BackSlotLibClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityRenderer instanceof AvatarRenderer<?> avatarRenderer) {
				registrationHelper.register(new BackSlotRenderLayer(avatarRenderer, context));
			}
		});
	}
}
