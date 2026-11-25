package com.backpackplusplus.client.render;

import com.backpackplusplus.BackpackMod;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;

public class BackpackModel extends EntityModel<AvatarRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
		Identifier.fromNamespaceAndPath(BackpackMod.MOD_ID, "backpack"),
		"main"
	);

	public BackpackModel(ModelPart root) {
		super(root);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		root.addOrReplaceChild(
			"main_bag",
			CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-3.5F, 1.5F, 2.0F, 7.0F, 8.0F, 4.0F, CubeDeformation.NONE),
			PartPose.ZERO
		);

		root.addOrReplaceChild(
			"top_flap",
			CubeListBuilder.create()
				.texOffs(0, 13)
				.addBox(-4.0F, 0.75F, 1.65F, 8.0F, 3.0F, 5.0F, CubeDeformation.NONE),
			PartPose.ZERO
		);

		root.addOrReplaceChild(
			"front_pocket",
			CubeListBuilder.create()
				.texOffs(0, 22)
				.addBox(-2.5F, 5.75F, 5.25F, 5.0F, 3.0F, 2.0F, CubeDeformation.NONE),
			PartPose.ZERO
		);

		root.addOrReplaceChild(
			"left_strap",
			CubeListBuilder.create()
				.texOffs(28, 0)
				.addBox(2.25F, -0.15F, -2.35F, 1.0F, 8.0F, 5.0F, CubeDeformation.NONE),
			PartPose.ZERO
		);
		root.addOrReplaceChild(
			"right_strap",
			CubeListBuilder.create()
				.texOffs(28, 14)
				.addBox(-3.25F, -0.15F, -2.35F, 1.0F, 8.0F, 5.0F, CubeDeformation.NONE),
			PartPose.ZERO
		);

		root.addOrReplaceChild(
			"buckle",
			CubeListBuilder.create()
				.texOffs(0, 29)
				.addBox(-1.0F, 3.0F, 6.0F, 2.0F, 2.0F, 1.0F, CubeDeformation.NONE),
			PartPose.ZERO
		);

		return LayerDefinition.create(mesh, 64, 64);
	}
}
