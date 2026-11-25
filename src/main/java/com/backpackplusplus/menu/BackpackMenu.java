package com.backpackplusplus.menu;

import com.backpackplusplus.BackpackTier;
import com.backpackplusplus.item.BackpackItem;
import com.backpackplusplus.item.BackpackUpgradeItem;
import com.backpackplusplus.menu.BackpackContainer.BackpackLocator;
import com.backpackplusplus.registry.ModDataComponents;
import com.backpackplusplus.registry.ModMenus;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BackpackMenu extends AbstractContainerMenu {
	public static final int GUI_WIDTH = 204;
	public static final int STORAGE_PANEL_WIDTH = 176;
	public static final int UPGRADE_SLOT_X = 180;
	public static final int UPGRADE_SLOT_Y = 18;

	private final BackpackLocator locator;
	private final BackpackTier tier;
	private final BackpackContainer storageContainer;
	private final BackpackContainer upgradeContainer;

	public BackpackMenu(int syncId, Inventory playerInv, BackpackLocator locator) {
		super(ModMenus.BACKPACK_MENU, syncId);
		this.locator = locator;
		this.tier = locator.tier();

		this.storageContainer = new BackpackContainer(
			playerInv.player,
			locator,
			tier.storageSlots(),
			DataComponents.CONTAINER
		);
		this.upgradeContainer = new BackpackContainer(
			playerInv.player,
			locator,
			tier.upgradeSlots(),
			ModDataComponents.BACKPACK_UPGRADES
		);

		this.storageContainer.startOpen(playerInv.player);
		this.upgradeContainer.startOpen(playerInv.player);

		for (int row = 0; row < tier.rows(); row++) {
			for (int col = 0; col < BackpackTier.COLUMNS; col++) {
				int index = col + row * BackpackTier.COLUMNS;
				addSlot(new Slot(storageContainer, index, 8 + col * 18, 18 + row * 18) {
					@Override
					public boolean mayPlace(ItemStack stack) {
						return stack.getItem().canFitInsideContainerItems();
					}
				});
			}
		}

		for (int i = 0; i < tier.upgradeSlots(); i++) {
			addSlot(new Slot(upgradeContainer, i, UPGRADE_SLOT_X, UPGRADE_SLOT_Y + i * 18) {
				@Override
				public boolean mayPlace(ItemStack stack) {
					if (!(stack.getItem() instanceof BackpackUpgradeItem candidate)) {
						return false;
					}
					for (int j = 0; j < upgradeContainer.getContainerSize(); j++) {
						ItemStack existing = upgradeContainer.getItem(j);
						if (existing.getItem() instanceof BackpackUpgradeItem installed
							&& installed.upgradeType() == candidate.upgradeType()) {
							return false;
						}
					}
					return true;
				}

				@Override
				public int getMaxStackSize() {
					return 1;
				}
			});
		}

		int invTopY = 32 + tier.rows() * 18;
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				int invSlot = col + row * 9 + 9;
				addSlot(createPlayerSlot(playerInv, invSlot, 8 + col * 18, invTopY + row * 18));
			}
		}
		for (int col = 0; col < 9; col++) {
			addSlot(createPlayerSlot(playerInv, col, 8 + col * 18, invTopY + 58));
		}
	}

	private Slot createPlayerSlot(Inventory playerInv, int invSlot, int x, int y) {
		boolean locked = locator.isSameInventorySlot(playerInv, invSlot);
		return new Slot(playerInv, invSlot, x, y) {
			@Override
			public boolean mayPickup(Player player) {
				return !locked && super.mayPickup(player);
			}

			@Override
			public boolean mayPlace(ItemStack stack) {
				return !locked && super.mayPlace(stack);
			}
		};
	}

	public BackpackTier tier() {
		return tier;
	}

	public BackpackLocator locator() {
		return locator;
	}

	public boolean hasCraftingUpgrade() {
		for (int i = 0; i < upgradeContainer.getContainerSize(); i++) {
			ItemStack stack = upgradeContainer.getItem(i);
			if (stack.getItem() instanceof BackpackUpgradeItem upgrade
				&& upgrade.upgradeType() == BackpackUpgradeItem.UpgradeType.CRAFTING) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void clicked(int slotId, int button, ContainerInput input, Player player) {
		if (input == ContainerInput.SWAP && locator.isSameInventorySlot(player.getInventory(), button)) {
			return;
		}
		super.clicked(slotId, button, input, player);
	}

	@Override
	public boolean stillValid(Player player) {
		ItemStack stack = storageContainer.backpack();
		return stack.getItem() instanceof BackpackItem bp && bp.tier() == tier;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = this.slots.get(index);
		if (!slot.hasItem()) {
			return ItemStack.EMPTY;
		}

		ItemStack stack = slot.getItem();
		ItemStack copy = stack.copy();

		int storageEnd = tier.storageSlots();
		int upgradeEnd = storageEnd + tier.upgradeSlots();
		int playerEnd = this.slots.size();

		if (index < upgradeEnd) {
			if (!moveItemStackTo(stack, upgradeEnd, playerEnd, true)) {
				return ItemStack.EMPTY;
			}
		} else {
			boolean moved = false;
			if (stack.getItem() instanceof BackpackUpgradeItem) {
				moved = moveItemStackTo(stack, storageEnd, upgradeEnd, false);
			}
			if (!moved && !moveItemStackTo(stack, 0, storageEnd, false)) {
				return ItemStack.EMPTY;
			}
		}

		if (stack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}

		if (stack.getCount() == copy.getCount()) {
			return ItemStack.EMPTY;
		}

		slot.onTake(player, stack);
		return copy;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		storageContainer.stopOpen(player);
		upgradeContainer.stopOpen(player);
	}
}
