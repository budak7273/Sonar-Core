package sonar.core.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.inventory.slots.SlotLarge;

public abstract class ContainerLargeInventory extends ContainerSync {

	ILargeInventory entity;

	public ContainerLargeInventory(TileEntity tile) {
		super(tile);
		entity = (ILargeInventory) tile;
	}

	/** a rewrite of the mergeItemStack which accommodates for a {@link ILargeInventory} */
	protected boolean mergeSpecial(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
		boolean flag = false;
		int i = startIndex;

		if (reverseDirection) {
			i = endIndex - 1;
		}

		if (stack.isStackable()) {
			while (stack.stackSize > 0 && (!reverseDirection && i < endIndex || reverseDirection && i >= startIndex)) {
				Slot slot = (Slot) this.inventorySlots.get(i);
				ItemStack itemstack = slot.getStack();
				StoredItemStack stored = entity.getTileInv().buildItemStack(entity.getTileInv().slots[i]);

				if (itemstack != null && itemstack.getItem() == stack.getItem() && (!stack.getHasSubtypes() || stack.getMetadata() == itemstack.getMetadata()) && ItemStack.areItemStackTagsEqual(stack, itemstack)) {
					int j = itemstack.stackSize + stack.stackSize;
					int maxSize = /* slot instanceof SlotLarge ? entity.getTileInv().max : */stack.getMaxStackSize();
					if (j <= maxSize) {
						stack.stackSize = 0;
						if (slot instanceof SlotLarge) {
							entity.getTileInv().slots[i] = entity.getTileInv().buildArrayList(stored.setStackSize(j));
						} else {
							itemstack.stackSize = j;
						}
						slot.onSlotChanged();
						flag = true;
					} else if (itemstack.stackSize < maxSize) {
						stack.stackSize -= maxSize - itemstack.stackSize;
						if (slot instanceof SlotLarge) {
							stored.add(new StoredItemStack(itemstack.copy()).setStackSize(maxSize));
							entity.getTileInv().slots[i] = entity.getTileInv().buildArrayList(stored);
						} else {
							itemstack.stackSize = maxSize;
						}
						slot.onSlotChanged();
						flag = true;
					}
				}

				if (reverseDirection) {
					--i;
				} else {
					++i;
				}
			}
		}

		if (stack.stackSize > 0) {
			if (reverseDirection) {
				i = endIndex - 1;
			} else {
				i = startIndex;
			}

			while (!reverseDirection && i < endIndex || reverseDirection && i >= startIndex) {
				Slot slot1 = (Slot) this.inventorySlots.get(i);
				ItemStack itemstack1 = slot1.getStack();
				if (slot1.isItemValid(stack)) {
					if (slot1 instanceof SlotLarge) {
						StoredItemStack target = entity.getTileInv().buildItemStack(entity.getTileInv().slots[i]);
						if (target != null) {
							target = target.copy();
							itemstack1 = target.getFullStack();
						} else {
							target = new StoredItemStack(stack).setStackSize(0);
							itemstack1 = null;
						}
						int max = target.getItemStack().getMaxStackSize() * entity.getTileInv().numStacks;
						if (target.stored < max) {
							int toAdd = (int) Math.min(max - target.stored, stack.stackSize);
							target.add(new StoredItemStack(stack.copy()).setStackSize(toAdd));

							entity.getTileInv().slots[i] = entity.getTileInv().buildArrayList(target);
							stack.stackSize -= toAdd;
							if (stack.stackSize == 0) {
								flag = true;
								break;
							}
						}

					} else if (itemstack1 == null) {
						slot1.putStack(stack.copy());
						slot1.onSlotChanged();
						stack.stackSize = 0;
						flag = true;
						break;
					}
				}
				if (reverseDirection) {
					--i;
				} else {
					++i;
				}
			}
		}

		return flag;
	}

	/** special implementation which accommodates for a {@link ILargeInventory} */
	public ItemStack slotClick(int slotID, int var, int button, EntityPlayer player) {
		if (!(slotID < entity.getTileInv().size) || button == 1) {
			return super.slotClick(slotID, var, button, player);
		}
		if (slotID >= 0) {
			StoredItemStack clicked = entity.getTileInv().buildItemStack(entity.getTileInv().slots[slotID]);
			if ((var == 0 || var == 1) && button == 0) {
				ItemStack held = player.inventory.getItemStack();
				if (held == null && clicked != null && clicked.getItemStack() != null) {
					int toRemove = (int) Math.min(clicked.getItemStack().getMaxStackSize(), clicked.stored);
					if (var == 1 && toRemove != 1) {
						toRemove = (int) Math.ceil(toRemove / 2);
					}
					if (toRemove != 0) {
						ItemStack stack = clicked.copy().setStackSize(toRemove).getFullStack();
						clicked.remove(stack);
						if (clicked.stored == 0) {
							entity.getTileInv().slots[slotID] = null;
						}
						player.inventory.setItemStack(stack);
						entity.getTileInv().slots[slotID] = entity.getTileInv().buildArrayList(clicked);
						return null;
					}
				} else if (held != null) {
					if (clicked == null || clicked.getItemStack() == null || clicked.getStackSize() == 0) {
						if (entity.getTileInv().isItemValidForSlot(slotID * entity.getTileInv().numStacks, held)) {
							entity.getTileInv().slots[slotID] = entity.getTileInv().buildArrayList(new StoredItemStack(held));
							player.inventory.setItemStack(null);
							return null;
						}
					} else if (clicked != null && clicked.getItemStack() != null) {
						if (clicked.equalStack(held)) {
							int maxAdd = (int) Math.min((held.getMaxStackSize() * entity.getTileInv().numStacks) - clicked.getStackSize(), held.stackSize);
							if (maxAdd > 0) {
								clicked.add(new StoredItemStack(held).setStackSize(maxAdd));
								held.stackSize -= maxAdd;
								if (held.stackSize == 0) {
									player.inventory.setItemStack(null);
								}
								entity.getTileInv().slots[slotID] = entity.getTileInv().buildArrayList(clicked);
								return null;
							}
						}
					}
				}
			}
		}
		return null;
	}

}