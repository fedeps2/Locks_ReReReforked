package melonslise.locks.common.init;

import melonslise.locks.Locks;
import melonslise.locks.common.container.KeyRingContainer;
import melonslise.locks.common.container.LockPickingContainer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class LocksContainerTypes
{
	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Locks.ID);

	public static final Supplier<MenuType<LockPickingContainer>> LOCK_PICKING_TYPE = MENUS.register("lock_picking",
			() -> IMenuTypeExtension.create((id, inv, data) -> {
				InteractionHand hand = data.readEnum(InteractionHand.class);
				int lockableId = data.readInt();
				return new LockPickingContainer(id, inv.player, hand,
					LocksComponents.getLockableHandler(inv.player.level()).getLoaded().get(lockableId));
			}));

	public static final Supplier<MenuType<KeyRingContainer>> KEY_RING_TYPE = MENUS.register("key_ring",
			() -> IMenuTypeExtension.create((id, inv, data) -> {
				InteractionHand hand = data.readEnum(InteractionHand.class);
				return new KeyRingContainer(id, inv.player, hand, inv.player.getItemInHand(hand));
			}));

	private LocksContainerTypes() {}

	public static void register(IEventBus modBus) {
		MENUS.register(modBus);
	}
}
