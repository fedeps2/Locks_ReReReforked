package melonslise.locks.common.components;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import melonslise.locks.Locks;
import melonslise.locks.common.components.interfaces.ILockableStorage;
import melonslise.locks.common.util.Lockable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.List;

public class LockableStorage implements ILockableStorage
{
	public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Locks.ID, "lockable_storage");

	public Int2ObjectMap<Lockable> lockables = new Int2ObjectLinkedOpenHashMap<Lockable>();

	public LockableStorage() {}

	public LockableStorage(ChunkAccess chunk)
	{
	}

	@Override
	public Int2ObjectMap<Lockable> get()
	{
		return this.lockables;
	}

	@Override
	public void add(Lockable lkb)
	{
		this.lockables.put(lkb.id, lkb);
	}

	@Override
	public void remove(int id)
	{
		this.lockables.remove(id);
	}
}
