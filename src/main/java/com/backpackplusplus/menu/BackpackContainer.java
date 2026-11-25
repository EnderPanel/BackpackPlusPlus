package com.backpackplusplus.menu;

import com.backpackplusplus.BackpackTier;
import com.backpackplusplus.registry.ModDataComponents;
import com.backslotlib.api.BackSlotApi;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class BackpackContainer extends SimpleContainer {
	private final Player player;
	private final BackpackLocator locator;
	private final DataComponentType<ItemContainerContents> componentType;
	private final UUID backpackId;
	private boolean loading;

	public BackpackContainer(
		Player player,
		BackpackLocator locator,
		int size,
		DataComponentType<ItemContainerContents> componentType
	) {
		super(size);
		this.player = player;
		this.locator = locator;
		this.componentType = componentType;
		this.backpackId = locator.get(player).get(ModDataComponents.BACKPACK_ID);
		loadFromStack();
	}

	public ItemStack backpack() {
		ItemStack stack = locator.get(player);
		return Objects.equals(stack.get(ModDataComponents.BACKPACK_ID), backpackId) ? stack : ItemStack.EMPTY;
	}

	public void loadFromStack() {
		ItemStack stack = backpack();
		if (stack.isEmpty()) {
			return;
		}
		loading = true;
		try {
			NonNullList<ItemStack> items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
			ItemContainerContents contents = stack.getOrDefault(componentType, ItemContainerContents.EMPTY);
			contents.copyInto(items);
			for (int i = 0; i < getContainerSize(); i++) {
				setItem(i, items.get(i));
			}
		} finally {
			loading = false;
		}
	}

	public void saveToStack() {
		if (loading || player.level().isClientSide()) {
			return;
		}
		ItemStack stack = backpack();
		if (stack.isEmpty()) {
			return;
		}
		NonNullList<ItemStack> items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
		for (int i = 0; i < getContainerSize(); i++) {
			items.set(i, getItem(i));
		}
		stack.set(componentType, ItemContainerContents.fromItems(items));
		if (locator.backSlot()) {
			BackSlotApi.sync(player);
		}
	}

	@Override
	public void setChanged() {
		super.setChanged();
		saveToStack();
	}

	@Override
	public void stopOpen(ContainerUser user) {
		super.stopOpen(user);
		saveToStack();
	}

	public record BackpackLocator(boolean backSlot, int inventorySlot, int tierOrdinal) {
		public static final StreamCodec<RegistryFriendlyByteBuf, BackpackLocator> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL,
			BackpackLocator::backSlot,
			ByteBufCodecs.VAR_INT,
			BackpackLocator::inventorySlot,
			ByteBufCodecs.VAR_INT,
			BackpackLocator::tierOrdinal,
			BackpackLocator::new
		);

		public BackpackTier tier() {
			BackpackTier[] values = BackpackTier.values();
			return values[Math.clamp(tierOrdinal, 0, values.length - 1)];
		}

		public ItemStack get(Player player) {
			if (backSlot) {
				return BackSlotApi.get(player);
			}
			Inventory inv = player.getInventory();
			if (inventorySlot >= 0 && inventorySlot < inv.getContainerSize()) {
				return inv.getItem(inventorySlot);
			}
			return ItemStack.EMPTY;
		}

		public boolean isSameInventorySlot(Inventory inv, int slotIndex) {
			return !backSlot && slotIndex == inventorySlot;
		}
	}
}
