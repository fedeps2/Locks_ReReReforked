package melonslise.locks.common.components.interfaces;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IItemHandler extends Container {
    List<ItemStack> getItems();
    void setItems(List<ItemStack> items);
    int getSlots();
    ItemStack getStackInSlot(int slot);
}
