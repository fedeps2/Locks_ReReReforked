package melonslise.locks.common.container;

import melonslise.locks.common.components.ItemHandler;
import melonslise.locks.common.components.interfaces.IItemHandler;
import melonslise.locks.common.init.LocksComponents;
import melonslise.locks.common.init.LocksContainerTypes;
import melonslise.locks.common.init.LocksSoundEvents;
import melonslise.locks.common.item.KeyItem;
import melonslise.locks.common.item.KeyRingItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class KeyRingContainer extends AbstractContainerMenu
{
	public static class KeyRingSlot extends Slot
	{
		public final Player player;

		public KeyRingSlot(Player player, IItemHandler inv, int index, int x, int y)
		{
			super(inv, index, x, y);
			this.player = player;
		}

		@Override
		public boolean mayPlace(ItemStack stack)
		{
			return stack.getItem() instanceof KeyItem;
		}

		@Override
		public void set(ItemStack stack)
		{
			super.set(stack);
			if(!this.player.level().isClientSide)
				this.player.level().playSound(null, this.player.getX(), this.player.getY(), this.player.getZ(), LocksSoundEvents.KEY_RING.get(), SoundSource.PLAYERS, 1f, 1f);
		}

		@Override
		public void onTake(Player player, ItemStack stack)
		{
			if(!this.player.level().isClientSide)
				this.player.level().playSound(null, this.player.getX(), this.player.getY(), this.player.getZ(), LocksSoundEvents.KEY_RING.get(), SoundSource.PLAYERS, 1f, 1f);
			super.onTake(player, stack);
		}
	}

	public static class StorageSlot extends Slot
	{
		private final ItemStack protectedKeyRing;

		public StorageSlot(Inventory inventory, int index, int x, int y, ItemStack protectedKeyRing)
		{
			super(inventory, index, x, y);
			this.protectedKeyRing = protectedKeyRing;
		}

		@Override
		public boolean mayPickup(Player player)
		{
			return this.getItem() != this.protectedKeyRing && super.mayPickup(player);
		}

		@Override
		public boolean mayPlace(ItemStack stack)
		{
			return this.getItem() != this.protectedKeyRing && super.mayPlace(stack);
		}
	}

	public final Player player;
	public final ItemStack stack;
	public final IItemHandler inv;
	public final int rows;
	public final InteractionHand hand;

	public KeyRingContainer(int id, Player player, InteractionHand hand, ItemStack stack)
	{
		super(LocksContainerTypes.KEY_RING_TYPE.get(), id);
		this.player = player;
		this.hand = hand;
		this.stack = stack;

		int slotCount = 9;
		if (stack.getItem() instanceof KeyRingItem keyRingItem) {
			slotCount = keyRingItem.getSlotCount();
		}

		ItemHandler rawHandler = stack.get(LocksComponents.ITEM_HANDLER.get());
		if (rawHandler == null || rawHandler.getSlots() == 0) {
			rawHandler = ItemHandler.ofSize(slotCount);
			stack.set(LocksComponents.ITEM_HANDLER.get(), rawHandler);
		}
		rawHandler.setOnChanged(handler -> this.stack.set(LocksComponents.ITEM_HANDLER.get(), handler.copy()));
		this.inv = rawHandler;

		this.rows = inv.getSlots() / 9;
		for(int row = 0; row < rows; ++row)
			for(int col = 0; col < 9; ++col)
				this.addSlot(new KeyRingSlot(player, inv, col + row * 9, 8 + col * 18, 18 + row * 18));

		int offset = (rows - 4) * 18;
		for(int row = 0; row < 3; ++row)
			for (int col = 0; col < 9; ++col)
				this.addSlot(new StorageSlot(player.getInventory(), col + row * 9 + 9, 8 + col * 18, 103 + row * 18 + offset, stack));

		for(int coll = 0; coll < 9; ++coll)
			this.addSlot(new StorageSlot(player.getInventory(), coll, 8 + coll * 18, 161 + offset, stack));
	}

	@Override
	public boolean stillValid(Player player)
	{
		return !this.stack.isEmpty() && player.getItemInHand(this.hand) == this.stack;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index)
	{
		ItemStack result = ItemStack.EMPTY;
		if (!this.stillValid(player))
			return result;
		Slot slot = this.slots.get(index);
		if(slot == null || !slot.hasItem())
			return result;
		ItemStack slotStack = slot.getItem();

		if(slotStack.getItem() instanceof KeyRingItem)
			return ItemStack.EMPTY;

		result = slotStack.copy();
		if(index < this.inv.getSlots())
		{
			if(!this.moveItemStackTo(slotStack, this.inv.getSlots(), this.slots.size(), true))
				return ItemStack.EMPTY;
		}
		else if(!this.moveItemStackTo(slotStack, 0, this.inv.getSlots(), false))
			return ItemStack.EMPTY;
		if(slotStack.isEmpty())
			slot.set(ItemStack.EMPTY);
		else
			slot.setChanged();
		return result;
	}

	public static class Provider implements MenuProvider
	{
		public final ItemStack stack;
		public final InteractionHand hand;

		public Provider(InteractionHand hand, ItemStack stack)
		{
			this.hand = hand;
			this.stack = stack;
		}

		@Nullable
		@Override
		public AbstractContainerMenu createMenu(int containerId, Inventory inv, Player player)
		{
			return new KeyRingContainer(containerId, player, this.hand, this.stack);
		}

		@Override
		public Component getDisplayName()
		{
			return this.stack.getHoverName();
		}
	}

	public static Consumer<RegistryFriendlyByteBuf> getExtraData(InteractionHand hand)
	{
		return buf -> buf.writeEnum(hand);
	}
}
