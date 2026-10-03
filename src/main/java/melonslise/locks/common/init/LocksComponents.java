package melonslise.locks.common.init;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import melonslise.locks.Locks;
import melonslise.locks.common.components.ItemHandler;
import melonslise.locks.common.components.LockableHandler;
import melonslise.locks.common.components.LockableStorage;
import melonslise.locks.common.components.Selection;
import melonslise.locks.common.components.interfaces.ILockableHandler;
import net.minecraft.core.registries.Registries;

import java.util.function.Supplier;

public class LocksComponents {

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Locks.ID);

    public static final Supplier<AttachmentType<LockableStorage>> LOCKABLE_STORAGE =
            ATTACHMENT_TYPES.register("lockable_storage",
                    () -> AttachmentType.builder(() -> new LockableStorage()).build());

    public static final Supplier<AttachmentType<LockableHandler>> LOCKABLE_HANDLER =
            ATTACHMENT_TYPES.register("lockable_handler",
                    () -> AttachmentType.serializable(holder -> new LockableHandler((Level) holder)).build());

    public static final Supplier<AttachmentType<Selection>> SELECTION =
            ATTACHMENT_TYPES.register("selection",
                    () -> AttachmentType.builder(() -> new Selection(net.minecraft.core.BlockPos.ZERO))
                            .serialize(Selection.CODEC)
                            .copyOnDeath()
                            .build());

    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Locks.ID);

    public static final Supplier<DataComponentType<ItemHandler>> ITEM_HANDLER =
            DATA_COMPONENT_TYPES.register("item_handler",
                    () -> DataComponentType.<ItemHandler>builder()
                            .persistent(ItemHandler.CODEC)
                            .networkSynchronized(ItemHandler.STREAM_CODEC)
                            .build());

    private static final Map<Level, LockableHandler> lockableHandlers = new ConcurrentHashMap<>();

    public static ILockableHandler getLockableHandler(Level world) {
        return lockableHandlers.computeIfAbsent(world, w -> {
            if (w instanceof ServerLevel serverLevel) {
                return serverLevel.getData(LocksComponents.LOCKABLE_HANDLER.get());
            }
            return new LockableHandler(w);
        });
    }

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
        DATA_COMPONENT_TYPES.register(modBus);
    }
}
