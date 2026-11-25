package com.backslotlib.client.render;

import com.backslotlib.client.api.BackSlotClientApi;
import com.backslotlib.client.api.BackSlotClientApi.BackSlotRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.item.ItemStack;

public class BackSlotRenderLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
	private final List<BackSlotRenderer> renderers;

	public BackSlotRenderLayer(
		RenderLayerParent<AvatarRenderState, PlayerModel> parent,
		EntityRendererProvider.Context context
	) {
		super(parent);
		this.renderers = BackSlotClientApi.createRenderersForContext(context);
	}

	public static void renderColoredCutout(
		net.minecraft.client.model.Model<? super AvatarRenderState> model,
		net.minecraft.resources.Identifier texture,
		PoseStack poseStack,
		SubmitNodeCollector collector,
		int packedLight,
		AvatarRenderState state,
		int argbColor,
		int order
	) {
		RenderLayer.renderColoredCutoutModel(model, texture, poseStack, collector, packedLight, state, argbColor, order);
	}

	@Override
	public void submit(
		PoseStack poseStack,
		SubmitNodeCollector collector,
		int packedLight,
		AvatarRenderState state,
		float yRot,
		float xRot
	) {
		if (state.isInvisible) {
			return;
		}
		ItemStack stack = BackSlotClientApi.getRenderStack(state);
		if (stack.isEmpty()) {
			return;
		}
		PlayerModel parentModel = getParentModel();
		for (BackSlotRenderer renderer : this.renderers) {
			renderer.render(poseStack, collector, packedLight, state, parentModel, stack, yRot, xRot);
		}
	}
}
