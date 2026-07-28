package melonslise.locks.common.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import melonslise.locks.common.components.interfaces.IItemHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class ItemHandler implements IItemHandler {

    public static final Codec<ItemHandler> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ItemStack.OPTIONAL_CODEC.listOf().fieldOf("items").forGetter(h -> h.items)
            ).apply(instance, ItemHandler::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemHandler> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ItemHandler decode(RegistryFriendlyByteBuf buf) {
            int size = buf.readVarInt();
            List<ItemStack> items = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                items.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
            }
            return new ItemHandler(items);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ItemHandler handler) {
            buf.writeVarInt(handler.items.size());
            for (ItemStack stack : handler.items) {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
            }
        }
    };

    private NonNullList<ItemStack> items;
    private Consumer<ItemHandler> onChanged;

    public ItemHandler(List<ItemStack> items) {
        NonNullList<ItemStack> mutableList = NonNullList.create();
        mutableList.addAll(items);
        this.items = mutableList;
    }

    public ItemHandler(List<ItemStack> items, Consumer<ItemHandler> onChanged) {
        this(items);
        this.onChanged = onChanged;
    }

    public static ItemHandler ofSize(int size) {
        return new ItemHandler(NonNullList.withSize(size, ItemStack.EMPTY));
    }

    public void setOnChanged(Consumer<ItemHandler> onChanged) {
        this.onChanged = onChanged;
    }

    public List<ItemStack> getItems() {
        return this.items;
    }

    public void readFromNbt(CompoundTag compoundTag, HolderLookup.Provider provider) {
        this.items.clear();
        int i = compoundTag.getInt("size");
        this.items = NonNullList.withSize(i, ItemStack.EMPTY);
        NonNullList<ItemStack> list = NonNullList.create();
        list.addAll(this.items);
        ContainerHelper.loadAllItems(compoundTag, list, provider);
    }

    public void writeToNbt(CompoundTag compoundTag, HolderLookup.Provider provider) {
        compoundTag.putInt("size", this.items.size());
        NonNullList<ItemStack> list = NonNullList.create();
        list.addAll(this.items);
        ContainerHelper.saveAllItems(compoundTag, list, provider);
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    @Override
    public ItemStack getItem(int i) {
        return this.items.get(i);
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        ItemStack itemStack = this.items.get(i);
        itemStack.setCount(itemStack.getCount() - j);
        markChanged();
        return itemStack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        ItemStack removed = this.items.set(i, ItemStack.EMPTY);
        markChanged();
        return removed;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        this.items.set(i, itemStack);
        markChanged();
    }

    @Override
    public void setChanged() {
        markChanged();
    }

    private void markChanged() {
        if (this.onChanged != null) {
            this.onChanged.accept(this);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        this.items.clear();
        markChanged();
    }

    @Override
    public void setItems(List<ItemStack> items) {
        this.items.clear();
        this.items.addAll(NonNullList.withSize(items.size(), ItemStack.EMPTY));
        Iterator<ItemStack> iterator = items.iterator();
        for (int i = 0; i < items.size(); i++) {
            this.items.set(i, iterator.next());
        }
        markChanged();
    }

    @Override
    public int getSlots() {
        return this.items.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.items.get(slot);
    }
}
