package melonslise.locks.common.item;

import melonslise.locks.Locks;
import melonslise.locks.common.init.LocksSoundEvents;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.stream.Collectors;

public class KeyItem extends LockingItem
{
	public KeyItem(Properties props)
	{
		super(props);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, List<Component> lines, TooltipFlag flag)
	{
		super.appendHoverText(stack, tooltipContext, lines, flag);
		lines.add(Component.translatable(Locks.ID + ".tooltip.rename_key").withStyle(ChatFormatting.GRAY));
	}

	// TODO Sound pitch
	@Override
	public InteractionResult useOn(UseOnContext ctx)
	{
		Level world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		int id = getOrSetId(ctx.getItemInHand());
		List<Lockable> match = LocksUtil.intersecting(world, pos).filter(lkb -> lkb.lock.lockRecord.id() == id).collect(Collectors.toList());
		if(match.isEmpty())
			return InteractionResult.PASS;
		world.playSound(ctx.getPlayer(), pos, LocksSoundEvents.LOCK_OPEN.get(), SoundSource.BLOCKS, 1f, 1f);
		if(world.isClientSide)
			return InteractionResult.SUCCESS;
		for(Lockable lkb : match)
			lkb.lock.setLocked(!lkb.lock.isLocked());
		return InteractionResult.SUCCESS;
	}
}