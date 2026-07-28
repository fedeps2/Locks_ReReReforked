package melonslise.locks.common.components.interfaces;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import melonslise.locks.common.util.Lockable;

public interface ILockableStorage {
    Int2ObjectMap<Lockable> get();

    void add(Lockable lkb);

    void remove(int id);
}
