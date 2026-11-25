package com.backpackplusplus.client.render;

import com.backpackplusplus.BackpackMod;
import com.backpackplusplus.item.BackpackItem;
import com.backslotlib.client.render.BackSlotRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;

public class BackpackLayer {
	private static final Identifier BASE_TEXTURE = Identifier.fromNamespaceAndPath(
		BackpackMod.MOD_ID,
		"textures/entity/backpack_base.png"
	);

	private final BackpackModel model;

	public BackpackLayer(EntityRendererProvider.Context context) {
		this.model = new BackpackModel(context.bakeLayer(BackpackModel.LAYER_LOCATION));
	}

	public void render(
		PoseStack poseStack,
		SubmitNodeCollector collector,
		int packedLight,
		AvatarRenderState state,
		PlayerModel parentModel,
		ItemStack stack,
		float yRot,
		float xRot
	) {
		if (!(stack.getItem() instanceof BackpackItem backpack)) {
			return;
		}

		poseStack.pushPose();
		parentModel.body.translateAndRotate(poseStack);

		if (!state.chestEquipment.isEmpty()) {
			poseStack.translate(0.0F, 0.0F, 0.0625F);
		}

		int dyedArgb = ARGB.opaque(BackpackItem.getColor(stack));
		BackSlotRenderLayer.renderColoredCutout(
			this.model,
			BASE_TEXTURE,
			poseStack,
			collector,
			packedLight,
			state,
			dyedArgb,
			0
		);

		BackSlotRenderLayer.renderColoredCutout(
			this.model,
			backpack.tier().modelOverlayTexture(),
			poseStack,
			collector,
			packedLight,
			state,
			-1,
			1
		);

		poseStack.popPose();
	}
}
