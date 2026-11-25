package com.backslotlib.slot;

import com.backslotlib.api.BackSlotApi;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BackSlotContainer implements Container {
	private final Player player;

	public BackSlotContainer(Player player) {
		this.player = player;
	}

	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return BackSlotApi.get(player).isEmpty();
	}

	@Override
	public ItemStack getItem(int slot) {
		return slot == 0 ? BackSlotApi.get(player) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		if (slot != 0 || amount <= 0) {
			return ItemStack.EMPTY;
		}
		ItemStack current = BackSlotApi.get(player);
		if (current.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack copy = current.copy();
		ItemStack split = copy.split(amount);
		BackSlotApi.set(player, copy.isEmpty() ? ItemStack.EMPTY : copy);
		return split;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		if (slot != 0) {
			return ItemStack.EMPTY;
		}
		ItemStack current = BackSlotApi.get(player);
		if (current.isEmpty()) {
			return ItemStack.EMPTY;
		}
		BackSlotApi.set(player, ItemStack.EMPTY);
		return current;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (slot == 0) {
			BackSlotApi.set(player, stack);
		}
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public void setChanged() {
		BackSlotApi.sync(player);
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public void clearContent() {
		BackSlotApi.set(player, ItemStack.EMPTY);
	}
}
