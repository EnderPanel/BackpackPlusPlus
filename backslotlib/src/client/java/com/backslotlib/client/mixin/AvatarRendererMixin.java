package com.backslotlib.client.mixin;

import com.backslotlib.api.BackSlotApi;
import com.backslotlib.client.api.BackSlotClientApi;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
	private void backslotlib$extractBackSlotState(Avatar avatar, AvatarRenderState state, float partialTick, CallbackInfo ci) {
		ItemStack worn = BackSlotApi.getVisible(avatar);
		state.setData(BackSlotClientApi.BACK_SLOT_STATE_KEY, worn.isEmpty() ? ItemStack.EMPTY : worn.copy());
	}
}
