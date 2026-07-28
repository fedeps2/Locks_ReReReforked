package melonslise.locks;

import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.config.LocksServerConfig;
import melonslise.locks.common.event.LocksEvents;
import melonslise.locks.common.init.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.CuriosCapability;

@Mod(Locks.ID)
public final class Locks
{
	public static final String ID = "locks";

	public static final Logger LOGGER = LogManager.getLogger();

	public Locks(IEventBus modBus, ModContainer container) {
		LocksComponents.register(modBus);
		LocksItems.register(modBus);
		LocksSoundEvents.register(modBus);
		LocksContainerTypes.register(modBus);
		LocksRecipeSerializers.register(modBus);
		LocksFeatures.register(modBus);
		LocksEnchantments.register(modBus);
		LocksEvents.register(modBus);
		LocksNetwork.register(modBus);

		container.registerConfig(ModConfig.Type.COMMON, LocksConfig.SPEC);
		container.registerConfig(ModConfig.Type.SERVER, LocksServerConfig.SPEC);

		modBus.addListener(RegisterCapabilitiesEvent.class, event -> {
			event.registerItem(CuriosCapability.ITEM,
				(stack, ctx) -> (top.theillusivec4.curios.api.type.capability.ICurio) stack.getItem(),
				LocksItems.KEY_RING.get(),
				LocksItems.KEY_RING_LARGE.get());
		});
	}
}
