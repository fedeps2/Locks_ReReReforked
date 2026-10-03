package melonslise.locks.mixin;

import melonslise.locks.common.components.interfaces.ILockableHandler;
import melonslise.locks.common.init.LocksComponents;
import melonslise.locks.common.init.LocksItemTags;
import melonslise.locks.common.init.LocksItems;
import melonslise.locks.common.item.KeyRingItem;
import melonslise.locks.common.item.LockingItem;
import melonslise.locks.common.util.Lockable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContainerOpenersCounter.class)
public class ContainerOpenersCounterMixin
{
	@Shadow
	private int openCount;

	// A stack that can open the given lock: a master key, a key (or a key inside a key ring) with a matching id
	private static boolean isKeyFor(ItemStack stack, int lockId)
	{
		if (stack.isEmpty())
			return false;
		if (stack.getItem() == LocksItems.MASTER_KEY.get())
			return true;
		if (stack.is(LocksItemTags.KEYS))
		{
			Integer id = LockingItem.getId(stack);
			return id != null && id == lockId;
		}
		return stack.getItem() instanceof KeyRingItem && KeyRingItem.containsId(stack, lockId);
	}

	private static boolean containsKeyFor(Container container, int lockId)
	{
		for (int a = 0; a < container.getContainerSize(); ++a)
			if (isKeyFor(container.getItem(a), lockId))
				return true;
		return false;
	}

	private void relockIfNeeded(Level level, BlockPos pos)
	{
		if (level.isClientSide || this.openCount != 0)
			return;
		ILockableHandler handler = LocksComponents.getLockableHandler(level);
		if (handler.getInChunk(pos) == null)
			return;
		Container container = level.getBlockEntity(pos) instanceof Container c ? c : null;
		handler.getInChunk(pos).values().stream()
			.filter(lkb -> lkb.bb.intersects(pos) && lkb.autoRelock)
			.forEach(lkb -> {
				lkb.autoRelock = false;
				// Never trap the only key inside: keep the lock open if a matching key is stored in the container
				if (container != null && containsKeyFor(container, lkb.lock.lockRecord.id()))
					return;
				lkb.lock.setLocked(true);
			});
	}

	@Inject(at = @At("TAIL"), method = "decrementOpeners")
	private void onDecrementOpeners(Player player, Level level, BlockPos pos, BlockState state, CallbackInfo ci)
	{
		this.relockIfNeeded(level, pos);
	}

	@Inject(at = @At("TAIL"), method = "recheckOpeners")
	private void onRecheckOpeners(Level level, BlockPos pos, BlockState state, CallbackInfo ci)
	{
		this.relockIfNeeded(level, pos);
	}
}
