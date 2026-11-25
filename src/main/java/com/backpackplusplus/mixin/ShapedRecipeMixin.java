package com.backpackplusplus.mixin;

import com.backpackplusplus.item.BackpackItem;
import com.backpackplusplus.registry.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeMixin {
	@Inject(method = "assemble(Lnet/minecraft/world/item/crafting/CraftingInput;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"))
	private void backpackplusplus$preserveBackpackData(CraftingInput input, CallbackInfoReturnable<ItemStack> cir) {
		ItemStack result = cir.getReturnValue();
		if (!(result.getItem() instanceof BackpackItem)) {
			return;
		}
		for (ItemStack ingredient : input.items()) {
			if (ingredient.getItem() instanceof BackpackItem) {
				if (ingredient.has(DataComponents.CONTAINER)) {
					result.set(DataComponents.CONTAINER, ingredient.get(DataComponents.CONTAINER));
				}
				if (ingredient.has(ModDataComponents.BACKPACK_UPGRADES)) {
					result.set(ModDataComponents.BACKPACK_UPGRADES, ingredient.get(ModDataComponents.BACKPACK_UPGRADES));
				}
				if (ingredient.has(ModDataComponents.SMELT_CHARGE)) {
					result.set(ModDataComponents.SMELT_CHARGE, ingredient.get(ModDataComponents.SMELT_CHARGE));
				}
				if (ingredient.has(DataComponents.DYED_COLOR)) {
					result.set(DataComponents.DYED_COLOR, ingredient.get(DataComponents.DYED_COLOR));
				}
				if (ingredient.has(DataComponents.CUSTOM_NAME)) {
					result.set(DataComponents.CUSTOM_NAME, ingredient.get(DataComponents.CUSTOM_NAME));
				}
				break;
			}
		}
	}
}
