package melonslise.locks.mixin;

import melonslise.locks.common.init.LocksComponents;
import melonslise.locks.common.network.toclient.AddLockableToChunkPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(ChunkMap.class)
public class ChunkManagerMixin
{
	@Inject(at = @At("TAIL"), method = "onChunkReadyToSend(Lnet/minecraft/world/level/chunk/LevelChunk;)V")
	private void playerLoadedChunk(LevelChunk levelChunk, CallbackInfo ci)
	{
		ChunkPos chunkPos = levelChunk.getPos();
		Iterator<ServerPlayer> var3 = ((ChunkMap)(Object)this).playerMap.getAllPlayers().iterator();
		var3.forEachRemaining(player ->
				levelChunk.getData(LocksComponents.LOCKABLE_STORAGE).get().values()
				.forEach(lkb -> PacketDistributor.sendToPlayer(player, new AddLockableToChunkPacket(lkb, levelChunk)))
		);
	}
}
