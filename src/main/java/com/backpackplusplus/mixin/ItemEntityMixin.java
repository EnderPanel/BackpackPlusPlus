package com.backpackplusplus.mixin;

import com.backpackplusplus.upgrade.BackpackUpgradeHandler;
import java.util.UUID;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
	@Shadow
	private int pickupDelay;

	@Shadow
	private UUID target;

	@Shadow
	public abstract ItemStack getItem();

	@Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
	private void backpackplusplus$autoPickup(Player player, CallbackInfo ci) {
		ItemEntity self = (ItemEntity) (Object) this;
		if (self.level().isClientSide()) {
			return;
		}
		if (this.pickupDelay != 0) {
			return;
		}
		if (this.target != null && !this.target.equals(player.getUUID())) {
			return;
		}

		ItemStack stack = this.getItem();
		int originalCount = stack.getCount();
		if (originalCount <= 0) {
			return;
		}

		Item item = stack.getItem();
		if (BackpackUpgradeHandler.tryAutoPickup(player, stack)) {
			int taken = originalCount - stack.getCount();
			if (taken > 0) {
				player.take(self, taken);
				player.awardStat(Stats.ITEM_PICKED_UP.get(item), taken);
				player.onItemPickup(self);
			}
			if (stack.isEmpty()) {
				self.discard();
				ci.cancel();
			}
		}
	}
}
