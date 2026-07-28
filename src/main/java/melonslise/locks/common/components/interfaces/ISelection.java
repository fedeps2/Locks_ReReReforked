package melonslise.locks.common.components.interfaces;

import net.minecraft.core.BlockPos;

public interface ISelection {
    BlockPos get();

    void set(BlockPos pos);
}
