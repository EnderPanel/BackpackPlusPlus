package com.backslotlib;

import com.backslotlib.api.BackSlotApi;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackSlotLib implements ModInitializer {
	public static final String MOD_ID = "backslotlib";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		BackSlotApi.initialize();

		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			if (alive || oldPlayer.level().getGameRules().get(GameRules.KEEP_INVENTORY) || oldPlayer.isSpectator()) {
				ItemStack worn = BackSlotApi.get(oldPlayer);
				if (!worn.isEmpty()) {
					BackSlotApi.set(newPlayer, worn.copy());
				}
			}
		});

		ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			if (entity instanceof ServerPlayer player) {
				BackSlotApi.updateVisible(player);
			}
		});

		LOGGER.info("BackSlot Lib initialized");
	}
}
