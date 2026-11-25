package com.backpackplusplus;

import com.backpackplusplus.item.BackpackItem;
import com.backpackplusplus.network.ModNetworking;
import com.backpackplusplus.registry.ModDataComponents;
import com.backpackplusplus.registry.ModItems;
import com.backpackplusplus.registry.ModMenus;
import com.backpackplusplus.upgrade.BackpackUpgradeHandler;
import com.backslotlib.api.BackSlotApi;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackpackMod implements ModInitializer {
	public static final String MOD_ID = "backpackplusplus";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModDataComponents.initialize();
		ModItems.initialize();
		BackSlotApi.registerPredicate(stack -> stack.getItem() instanceof BackpackItem);
		BackSlotApi.registerPrivateComponent(ModDataComponents.BACKPACK_UPGRADES);
		BackSlotApi.registerPrivateComponent(ModDataComponents.SMELT_CHARGE);
		BackSlotApi.registerPrivateComponent(ModDataComponents.BACKPACK_ID);
		ModMenus.initialize();
		ModNetworking.initialize();
		BackpackUpgradeHandler.initialize();
		LOGGER.info("Backpack++ initialized");
	}
}
