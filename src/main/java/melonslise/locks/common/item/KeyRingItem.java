package melonslise.locks.common.item;

import melonslise.locks.common.components.ItemHandler;
import melonslise.locks.common.components.interfaces.IItemHandler;
import melonslise.locks.common.container.KeyRingContainer;
import melonslise.locks.common.init.LocksComponents;
import melonslise.locks.common.init.LocksSoundEvents;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class KeyRingItem extends Item implements ICurioItem
{
	public final int rows;

	public KeyRingItem(int rows, Properties props)
	{
		super(props.stacksTo(1));
		this.rows = rows;
	}

	public int getSlotCount() {
		return this.rows * 9;
	}

	@Override
	public boolean canEquip(SlotContext slotContext, ItemStack stack) {
		return slotContext.identifier().equals("ring");
	}

	public static boolean containsId(ItemStack stack, int id)
	{
		ItemHandler inv = stack.get(LocksComponents.ITEM_HANDLER.get());
		if (inv == null) return false;
		for(int a = 0; a < inv.getSlots(); ++a)
			if(!inv.getStackInSlot(a).isEmpty() && Objects.equals(LockingItem.getId(inv.getStackInSlot(a)), id))
				return true;
		return false;
	}

	private ItemHandler getOrCreateHandler(ItemStack stack) {
		ItemHandler handler = stack.get(LocksComponents.ITEM_HANDLER.get());
		if (handler == null || handler.getSlots() == 0) {
			handler = ItemHandler.ofSize(getSlotCount());
			stack.set(LocksComponents.ITEM_HANDLER.get(), handler);
		}
		return handler;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
	{
		ItemStack stack = player.getItemInHand(hand);
		KeyRingItem self = (KeyRingItem) stack.getItem();
		self.getOrCreateHandler(stack);
		if(player instanceof ServerPlayer serverPlayer){
			serverPlayer.openMenu(new KeyRingContainer.Provider(hand, stack),
				KeyRingContainer.getExtraData(hand));
		}
		return new InteractionResultHolder<>(InteractionResult.PASS, stack);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx)
	{
		Level world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		ItemStack stack = ctx.getItemInHand();
		KeyRingItem self = (KeyRingItem) stack.getItem();
		ItemHandler inv = self.getOrCreateHandler(stack);
		List<Lockable> intersect = LocksUtil.intersecting(world, pos).collect(Collectors.toList());
		if(intersect.isEmpty())
			return InteractionResult.PASS;
		for(int a = 0; a < inv.getSlots(); ++a)
		{
			ItemStack key = inv.getStackInSlot(a);
			if (key.isEmpty())
				continue;
			Integer id = LockingItem.getId(key);
			if (id == null)
				continue;
			List<Lockable> match = intersect.stream().filter(lkb -> lkb.lock.lockRecord.id() == id).collect(Collectors.toList());
			if(match.isEmpty())
				continue;
			world.playSound(ctx.getPlayer(), pos, LocksSoundEvents.LOCK_OPEN.get(), SoundSource.BLOCKS, 1f, 1f);
			if(world.isClientSide)
				return InteractionResult.SUCCESS;
			for(Lockable lkb : match)
				lkb.lock.setLocked(!lkb.lock.isLocked());
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.SUCCESS;
	}
}
