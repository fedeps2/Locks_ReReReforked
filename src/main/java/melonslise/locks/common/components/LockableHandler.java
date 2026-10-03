package melonslise.locks.common.components;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import melonslise.locks.Locks;
import melonslise.locks.common.components.interfaces.ILockableHandler;
import melonslise.locks.common.components.interfaces.ILockableStorage;
import melonslise.locks.common.config.LocksServerConfig;
import melonslise.locks.common.init.LocksComponents;
import melonslise.locks.common.network.toclient.AddLockablePacket;
import melonslise.locks.common.network.toclient.RemoveLockablePacket;
import melonslise.locks.common.network.toclient.UpdateLockablePacket;
import melonslise.locks.common.util.Lockable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicInteger;

public class LockableHandler implements ILockableHandler, INBTSerializable<CompoundTag> {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Locks.ID, "lockable_handler");

    public final Level world;

    public AtomicInteger lastId = new AtomicInteger();

    public Int2ObjectMap<Lockable> lockables = new Int2ObjectLinkedOpenHashMap<Lockable>();

    public LockableHandler(Level world)
    {
        this.world = world;
    }

    public int nextId()
    {
        return this.lastId.incrementAndGet();
    }

    @Override
    public Int2ObjectMap<Lockable> getLoaded()
    {
        return this.lockables;
    }

    @Override
    public Int2ObjectMap<Lockable> getInChunk(BlockPos pos)
    {
        return this.world.hasChunkAt(pos) ? this.world.getChunkAt(pos).getData(LocksComponents.LOCKABLE_STORAGE).get() : null;
    }

    @Override
    public boolean add(Lockable lkb, Level level)
    {
        if(lkb.bb.volume() > LocksServerConfig.MAX_LOCKABLE_VOLUME.get())
            return false;
        List<ILockableStorage> sts = lkb.bb.containedChunksTo((x, z) ->
        {
            try {
                LevelChunk levelChunk = level.getChunk(x, z);
                ILockableStorage st = levelChunk.getData(LocksComponents.LOCKABLE_STORAGE);
                return st.get().values().stream().anyMatch(lkb1 -> lkb1.bb.intersects(lkb.bb)) ? null : st;
            } catch (Exception e){
                Locks.LOGGER.warn("Chunk not gen");
            }
            return null;
        }, true);
        if(sts == null)
            return false;

        for(int a = 0; a < sts.size(); ++a)
            sts.get(a).add(lkb);
        this.lockables.put(lkb.id, lkb);
        lkb.addObserver(this);
        if(level.isClientSide)
            lkb.swing(10);
        else
        {
            level.getServer().getPlayerList().players.forEach(player -> {
                PacketDistributor.sendToPlayer(player, new AddLockablePacket(lkb.toRecord()));
            });
        }
        return true;
    }

    @Override
    public boolean remove(int id)
    {
        Lockable lkb = this.lockables.get(id);
        if(lkb == this.lockables.defaultReturnValue())
            return false;
        List<LevelChunk> chs = lkb.bb.containedChunksTo((x, z) -> this.world.hasChunk(x, z) ? this.world.getChunk(x, z) : null, true);

        for(int a = 0; a < chs.size(); ++a)
            chs.get(a).getData(LocksComponents.LOCKABLE_STORAGE).remove(id);
        this.lockables.remove(id);
        lkb.deleteObserver(this);
        if(this.world.isClientSide)
            return true;
        world.getServer().getPlayerList().players.forEach(player -> {
            PacketDistributor.sendToPlayer(player, new RemoveLockablePacket(lkb.id));
        });
        return true;
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if(this.world.isClientSide || !(o instanceof Lockable))
            return;
        Lockable lockable = (Lockable) o;
        world.getServer().getPlayerList().players.forEach(player -> {
            PacketDistributor.sendToPlayer(player, new UpdateLockablePacket(lockable));
        });
    }

    public void readFromNbt(CompoundTag compoundTag, HolderLookup.Provider provider) {
        this.lastId.set(compoundTag.getInt("last_id"));
        ListTag lockables = compoundTag.getList("Lockables", Tag.TAG_COMPOUND);
        for(int a = 0; a < lockables.size(); ++a)
        {
            CompoundTag nbt1 = lockables.getCompound(a);
            Lockable lkb = Lockable.fromNbt(provider, nbt1);
            this.lockables.put(lkb.id, lkb);
            lkb.addObserver(this);
        }
    }

    public void writeToNbt(CompoundTag compoundTag, HolderLookup.Provider provider) {
        compoundTag.putInt("last_id", this.lastId.get());
        ListTag list = new ListTag();
        for(Lockable lkb : this.lockables.values())
            list.add(Lockable.toNbt(provider, lkb));
        compoundTag.put("Lockables", list);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        this.writeToNbt(tag, provider);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        this.readFromNbt(nbt, provider);
    }
}
