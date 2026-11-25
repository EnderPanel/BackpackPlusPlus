package com.backslotlib.client.api;

import com.backslotlib.BackSlotLib;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class BackSlotClientApi {
	public static final RenderStateDataKey<ItemStack> BACK_SLOT_STATE_KEY = RenderStateDataKey.create(
		() -> BackSlotLib.MOD_ID + ":back_slot_item"
	);

	private static final List<RendererEntry> RENDERERS = new CopyOnWriteArrayList<>();

	private BackSlotClientApi() {
	}

	@FunctionalInterface
	public interface BackSlotRenderer {
		void render(
			PoseStack poseStack,
			SubmitNodeCollector collector,
			int packedLight,
			AvatarRenderState state,
			PlayerModel parentModel,
			ItemStack stack,
			float yRot,
			float xRot
		);
	}

	@FunctionalInterface
	public interface BackSlotRendererFactory {
		BackSlotRenderer create(EntityRendererProvider.Context context);
	}

	private record RendererEntry(Predicate<ItemStack> predicate, BackSlotRendererFactory factory) {
	}

	public static void registerRenderer(Item item, BackSlotRendererFactory factory) {
		registerRenderer(stack -> stack.is(item), factory);
	}

	public static void registerRenderer(Predicate<ItemStack> predicate, BackSlotRendererFactory factory) {
		RENDERERS.add(new RendererEntry(predicate, factory));
	}

	public static ItemStack getRenderStack(AvatarRenderState state) {
		if (state == null) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = state.getDataOrDefault(BACK_SLOT_STATE_KEY, ItemStack.EMPTY);
		return stack != null ? stack : ItemStack.EMPTY;
	}

	public static List<BackSlotRenderer> createRenderersForContext(EntityRendererProvider.Context context) {
		return RENDERERS.stream()
			.<BackSlotRenderer>map(entry -> new PredicateBoundRenderer(entry.predicate(), entry.factory().create(context)))
			.toList();
	}

	private record PredicateBoundRenderer(Predicate<ItemStack> predicate, BackSlotRenderer delegate) implements BackSlotRenderer {
		@Override
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
			if (predicate.test(stack)) {
				delegate.render(poseStack, collector, packedLight, state, parentModel, stack, yRot, xRot);
			}
		}
	}
}
