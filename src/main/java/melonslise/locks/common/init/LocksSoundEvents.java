package melonslise.locks.common.init;

import melonslise.locks.Locks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class LocksSoundEvents
{
	public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Locks.ID);

	public static final Supplier<SoundEvent> KEY_RING = add("key_ring");
	public static final Supplier<SoundEvent> LOCK_CLOSE = add("lock.close");
	public static final Supplier<SoundEvent> LOCK_OPEN = add("lock.open");
	public static final Supplier<SoundEvent> LOCK_RATTLE = add("lock.rattle");
	public static final Supplier<SoundEvent> PIN_FAIL = add("pin.fail");
	public static final Supplier<SoundEvent> PIN_MATCH = add("pin.match");
	public static final Supplier<SoundEvent> SHOCK = add("shock");

	private LocksSoundEvents() {}

	public static Supplier<SoundEvent> add(String name)
	{
		return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Locks.ID, name)));
	}

	public static void register(IEventBus modBus) {
		SOUND_EVENTS.register(modBus);
	}
}
