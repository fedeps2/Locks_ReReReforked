package melonslise.locks.common.event;

import melonslise.locks.Locks;
import melonslise.locks.common.components.interfaces.ILockableHandler;
import melonslise.locks.common.config.LocksClientConfig;
import melonslise.locks.common.config.LocksServerConfig;
import melonslise.locks.common.container.LockPickingContainer;
import melonslise.locks.common.init.LocksComponents;
import melonslise.locks.common.init.LocksEnchantments;
import melonslise.locks.common.init.LocksItemTags;
import melonslise.locks.common.init.LocksItems;
import melonslise.locks.common.init.LocksSoundEvents;
import melonslise.locks.common.init.LocksVillagerTrades;
import melonslise.locks.common.item.KeyRingItem;
import melonslise.locks.common.item.LockPickItem;
import melonslise.locks.common.item.LockingItem;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksPredicates;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

public final class LocksEvents
{
	public static final Component LOCKED_MESSAGE = Component.translatable(Locks.ID + ".status.locked");

	private LocksEvents() {}

	private static void onLootTableLoad(LootTableLoadEvent event)
	{
		if (!event.getName().getPath().startsWith("chests"))
			return;
	}

	private static boolean checkCuriosKeyrings(Player player, int lockId) {
		java.util.Optional<ICuriosItemHandler> curiosHandler = CuriosApi.getCuriosInventory(player);
		if (curiosHandler.isEmpty()) return false;
		Map<String, ICurioStacksHandler> curios = curiosHandler.get().getCurios();
		for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
			ICurioStacksHandler stacks = entry.getValue();
			for (int i = 0; i < stacks.getSlots(); i++) {
				ItemStack stack = stacks.getStacks().getStackInSlot(i);
				if (stack.getItem() instanceof KeyRingItem && KeyRingItem.containsId(stack, lockId))
					return true;
			}
		}
		return false;
	}

	private static boolean keyMatches(ItemStack stack, Lockable lkb)
	{
		Item item = stack.getItem();
		if (item == LocksItems.MASTER_KEY.get())
			return true;
		if (stack.is(LocksItemTags.KEYS))
		{
			Integer id = LockingItem.getId(stack);
			return id != null && id == lkb.lock.lockRecord.id();
		}
		return item instanceof KeyRingItem && KeyRingItem.containsId(stack, lkb.lock.lockRecord.id());
	}

	private static InteractionResult handleRightClick(Player player, Level world, InteractionHand hand, BlockHitResult result)
	{
		BlockPos pos = result.getBlockPos();
		ILockableHandler handler = LocksComponents.getLockableHandler(world);
		Lockable[] intersect = handler.getInChunk(pos).values().stream().filter(lkb -> lkb.bb.intersects(pos)).toArray(Lockable[]::new);
		if(intersect.length == 0)
			return InteractionResult.PASS;
		if(hand != InteractionHand.MAIN_HAND)
		{
			return InteractionResult.FAIL;
		}
		ItemStack stack = player.getItemInHand(hand);

		// Shift + click with a matching key toggles the lock without opening the block, and keeps it that way
		// (autoRelock is cleared so a chest can be left unlocked for others to open)
		if (player.isShiftKeyDown())
		{
			Lockable[] matches = Arrays.stream(intersect).filter(lkb -> keyMatches(stack, lkb)).toArray(Lockable[]::new);
			if (matches.length > 0)
			{
				player.swing(InteractionHand.MAIN_HAND);
				boolean wasLocked = matches[0].lock.isLocked();
				for (Lockable lkb : matches)
				{
					if (!world.isClientSide)
						lkb.autoRelock = false;
					lkb.lock.setLocked(!lkb.lock.isLocked());
				}
				world.playSound(player, pos, wasLocked ? LocksSoundEvents.LOCK_OPEN.get() : LocksSoundEvents.LOCK_CLOSE.get(), SoundSource.BLOCKS, 1f, 1f);
				return InteractionResult.CONSUME;
			}
		}
		Optional<Lockable> locked = Arrays.stream(intersect).filter(LocksPredicates.LOCKED).findFirst();
		if(locked.isPresent())
		{
			Lockable lkb = locked.get();
			Item item = stack.getItem();
			boolean isLockPick = stack.is(LocksItemTags.LOCK_PICKS);
			Integer handKeyId = stack.is(LocksItemTags.KEYS) ? LockingItem.getId(stack) : null;
			boolean isMatchingKeyInHand = item == LocksItems.MASTER_KEY.get() || (handKeyId != null && handKeyId == lkb.lock.lockRecord.id()) || (item instanceof KeyRingItem && KeyRingItem.containsId(stack, lkb.lock.lockRecord.id()));
			boolean isMatchingKeyInCurios = !isMatchingKeyInHand && checkCuriosKeyrings(player, lkb.lock.lockRecord.id());
			boolean isMatchingKey = isMatchingKeyInHand || isMatchingKeyInCurios;

			if (!isLockPick && !isMatchingKey)
			{
				lkb.swing(20);
				world.playSound(player, pos, LocksSoundEvents.LOCK_RATTLE.get(), SoundSource.BLOCKS, 1f, 1f);
				player.swing(InteractionHand.MAIN_HAND);
			}

			if (isMatchingKeyInCurios) {
				world.playSound(player, pos, LocksSoundEvents.LOCK_OPEN.get(), SoundSource.BLOCKS, 1f, 1f);
				if (!world.isClientSide) {
					for (Lockable lkb2 : intersect) {
						if (lkb2.lock.lockRecord.id() == lkb.lock.lockRecord.id()) {
							lkb2.lock.setLocked(!lkb2.lock.isLocked());
						}
					}
				}
				return InteractionResult.CONSUME;
			}

			if (isMatchingKeyInHand) {
				player.swing(InteractionHand.MAIN_HAND);
				if (world.getBlockEntity(pos) instanceof BaseContainerBlockEntity) {
					world.playSound(player, pos, LocksSoundEvents.LOCK_OPEN.get(), SoundSource.BLOCKS, 1f, 1f);
					for (Lockable lkb2 : intersect) {
						if (lkb2.lock.lockRecord.id() == lkb.lock.lockRecord.id() && lkb2.lock.isLocked()) {
							lkb2.lock.setLocked(false);
							if (!world.isClientSide) {
								lkb2.autoRelock = true;
							}
						}
					}
				}
				return InteractionResult.PASS;
			}

			// The complexity enchantment gates which lock picks can even start the minigame
			if (isLockPick && !LockPickItem.canPick(stack,
					LocksEnchantments.getEnchantmentLevel(world.registryAccess(), lkb.stack, LocksEnchantments.COMPLEXITY_KEY)))
			{
				if (world.isClientSide)
					player.displayClientMessage(LockPickItem.TOO_COMPLEX_MESSAGE, true);
				return InteractionResult.FAIL;
			}

			if (world.isClientSide) {
				if (LocksClientConfig.DEAF_MODE.get()) {
					player.displayClientMessage(LOCKED_MESSAGE, true);
				}
				if (isLockPick) {
					return InteractionResult.CONSUME;
				}
				return InteractionResult.FAIL;
			}

			if (isLockPick) {
				((ServerPlayer) player).openMenu(new LockPickingContainer.Provider(hand, lkb),
					LockPickingContainer.getExtraData(hand, lkb));
				return InteractionResult.CONSUME;
			}
			return InteractionResult.FAIL;
		}
		if(LocksServerConfig.ALLOW_REMOVING_LOCKS.get() && player.isShiftKeyDown() && stack.isEmpty())
		{
			Lockable[] match = Arrays.stream(intersect).filter(LocksPredicates.NOT_LOCKED).toArray(Lockable[]::new);
			if(match.length == 0)
				return InteractionResult.PASS;
			world.playSound(player, pos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 0.8f, 0.8f + world.random.nextFloat() * 0.4f);
			player.swing(InteractionHand.MAIN_HAND);
			if(player instanceof ServerPlayer) {
				Locks.LOGGER.info("Removing lockable");
				for (Lockable lkb : match) {
					world.addFreshEntity(new ItemEntity(world, pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, lkb.stack));
					handler.remove(lkb.id);
				}
				return InteractionResult.CONSUME;
			}else {
				return InteractionResult.PASS;
			}
		}
		return InteractionResult.PASS;
	}

	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
	{
		InteractionResult result = handleRightClick(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
		if (result != InteractionResult.PASS) {
			event.setCancellationResult(result);
			event.setCanceled(true);
		}
	}

	public static boolean canBreakLockable(Level world, Player player, BlockPos pos)
	{
		return (LocksServerConfig.PROTECT_LOCKABLES.get() &&
				!player.isCreative() &&
				LocksUtil.lockedAndRelated(world, pos));
	}

	public static void onBlockBreak(BlockEvent.BreakEvent event)
	{
		if (canBreakLockable((net.minecraft.world.level.Level) event.getLevel(), event.getPlayer(), event.getPos())) {
			event.setCanceled(true);
		}
	}

	public static void register(IEventBus modBus)
	{
		NeoForge.EVENT_BUS.addListener(LocksEvents::onLootTableLoad);
		NeoForge.EVENT_BUS.addListener(LocksEvents::onBlockBreak);
		NeoForge.EVENT_BUS.addListener(LocksEvents::onRightClickBlock);
		NeoForge.EVENT_BUS.addListener(LocksVillagerTrades::onVillagerTrades);
		NeoForge.EVENT_BUS.addListener(LocksVillagerTrades::onWanderingTrades);
	}
}
