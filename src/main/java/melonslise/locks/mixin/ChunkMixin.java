package melonslise.locks.mixin;

import melonslise.locks.common.components.interfaces.ILockableHandler;
import melonslise.locks.common.components.interfaces.ILockableStorage;
import melonslise.locks.common.init.LocksComponents;
import melonslise.locks.common.network.toclient.AddLockableToChunkPacket;
import melonslise.locks.common.util.ILockableProvider;
import melonslise.locks.common.util.Lockable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunk.class)
public class ChunkMixin
{
	@Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ProtoChunk;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;)V")
	private void init(ServerLevel world, ProtoChunk pChunk, LevelChunk.PostLoadProcessor pPostLoad, CallbackInfo ci)
	{
		LevelChunk ch = (LevelChunk) (Object) this;
		ILockableStorage st = ch.getData(LocksComponents.LOCKABLE_STORAGE);
		ILockableHandler handler = LocksComponents.getLockableHandler(world);
		for(Lockable lkb : ((ILockableProvider) pChunk).getLockables())
		{
			if(lkb.tr==null){
				continue;
			}

			st.add(lkb);
			handler.getLoaded().put(lkb.id, lkb);
			lkb.addObserver(handler);
			world.getServer().getPlayerList().players.forEach(player -> {
				PacketDistributor.sendToPlayer(player, new AddLockableToChunkPacket(lkb, ch));
			});
		}

		// Rebuild the in-memory per-chunk index for lockables persisted in the level data
		// (covers chunks loaded from disk after a restart or unload, where the worldgen list is empty)
		int cx0 = ch.getPos().getMinBlockX();
		int cz0 = ch.getPos().getMinBlockZ();
		for(Lockable lkb : handler.getLoaded().values())
		{
			if(st.get().containsKey(lkb.id) || lkb.tr == null)
				continue;
			if(!lkb.bb.intersects(cx0, Integer.MIN_VALUE, cz0, cx0 + 16, Integer.MAX_VALUE, cz0 + 16))
				continue;
			st.add(lkb);
			world.getServer().getPlayerList().players.forEach(player -> {
				PacketDistributor.sendToPlayer(player, new AddLockableToChunkPacket(lkb, ch));
			});
		}
	}
}
