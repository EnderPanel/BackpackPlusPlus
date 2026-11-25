package com.backslotlib.mixin;

import com.backslotlib.api.BackSlotApi;
import java.util.function.Predicate;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public abstract class InventoryMixin {
	@Shadow
	@Final
	public Player player;

	@Inject(method = "clearOrCountMatchingItems", at = @At("RETURN"), cancellable = true)
	private void backslotlib$clearBackSlot(
		Predicate<ItemStack> predicate,
		boolean countOnly,
		int maxCount,
		Container craftSlots,
		CallbackInfoReturnable<Integer> cir
	) {
		int found = cir.getReturnValue();
		ItemStack worn = BackSlotApi.get(this.player).copy();
		int matched = ContainerHelper.clearOrCountMatchingItems(worn, predicate, maxCount - found, countOnly);
		if (matched <= 0) {
			return;
		}
		if (!countOnly) {
			BackSlotApi.set(this.player, worn);
		}
		cir.setReturnValue(found + matched);
	}
}
