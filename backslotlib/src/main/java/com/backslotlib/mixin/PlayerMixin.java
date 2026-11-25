package com.backslotlib.mixin;

import com.backslotlib.api.BackSlotApi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {
	@Inject(method = "dropEquipment", at = @At("TAIL"))
	private void backslotlib$dropBackSlotOnDeath(ServerLevel level, CallbackInfo ci) {
		if (level.getGameRules().get(GameRules.KEEP_INVENTORY)) {
			return;
		}
		Player self = (Player) (Object) this;
		ItemStack worn = BackSlotApi.get(self);
		if (worn.isEmpty()) {
			return;
		}
		BackSlotApi.set(self, ItemStack.EMPTY);
		if (!EnchantmentHelper.has(worn, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
			self.drop(worn, true, Prediction.SERVER_ONLY);
		}
	}
}
