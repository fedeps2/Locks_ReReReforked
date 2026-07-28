package melonslise.locks.common.init;

import melonslise.locks.Locks;
import melonslise.locks.common.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class LocksItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Locks.ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Locks.ID);

    public static final Supplier<Item> SPRING = ITEMS.register("spring", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> WOOD_LOCK_MECHANISM = ITEMS.register("wood_lock_mechanism", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> IRON_LOCK_MECHANISM = ITEMS.register("iron_lock_mechanism", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> STEEL_LOCK_MECHANISM = ITEMS.register("steel_lock_mechanism", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> KEY_BLANK = ITEMS.register("key_blank", () -> new Item(new Item.Properties()));
    public static final Supplier<LockItem> WOOD_LOCK = ITEMS.register("wood_lock", () -> new LockItem(5, 15, 4, new Item.Properties()));
    public static final Supplier<LockItem> IRON_LOCK = ITEMS.register("iron_lock", () -> new LockItem(7, 14, 12, new Item.Properties()));
    public static final Supplier<LockItem> STEEL_LOCK = ITEMS.register("steel_lock", () -> new LockItem(9, 12, 20, new Item.Properties()));
    public static final Supplier<LockItem> GOLD_LOCK = ITEMS.register("gold_lock", () -> new LockItem(6, 22, 6, new Item.Properties()));
    public static final Supplier<LockItem> DIAMOND_LOCK = ITEMS.register("diamond_lock", () -> new LockItem(11, 10, 100, new Item.Properties()));
    public static final Supplier<KeyItem> KEY = ITEMS.register("key", () -> new KeyItem(new Item.Properties()));
    public static final Supplier<MasterKeyItem> MASTER_KEY = ITEMS.register("master_key", () -> new MasterKeyItem(new Item.Properties()));
    public static final Supplier<KeyRingItem> KEY_RING = ITEMS.register("key_ring", () -> new KeyRingItem(1, new Item.Properties()));
    public static final Supplier<KeyRingItem> KEY_RING_LARGE = ITEMS.register("key_ring_large", () -> new KeyRingItem(2, new Item.Properties()));
    public static final Supplier<LockPickItem> WOOD_LOCK_PICK = ITEMS.register("wood_lock_pick", () -> new LockPickItem(0.2f, new Item.Properties()));
    public static final Supplier<LockPickItem> IRON_LOCK_PICK = ITEMS.register("iron_lock_pick", () -> new LockPickItem(0.35f, new Item.Properties()));
    public static final Supplier<LockPickItem> STEEL_LOCK_PICK = ITEMS.register("steel_lock_pick", () -> new LockPickItem(0.7f, new Item.Properties()));
    public static final Supplier<LockPickItem> GOLD_LOCK_PICK = ITEMS.register("gold_lock_pick", () -> new LockPickItem(0.25f, new Item.Properties()));
    public static final Supplier<LockPickItem> DIAMOND_LOCK_PICK = ITEMS.register("diamond_lock_pick", () -> new LockPickItem(0.85f, new Item.Properties()));

    public static final Supplier<CreativeModeTab> TABS = CREATIVE_TABS.register("main", () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(IRON_LOCK.get()))
            .title(Component.translatable("itemGroup.locks"))
            .displayItems((parameters, output) -> {
                ITEMS.getEntries().forEach(item -> output.accept(item.get()));
            })
            .build());

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        CREATIVE_TABS.register(modBus);
    }
}
