package melonslise.locks.common.init;

import melonslise.locks.common.network.toclient.*;
import melonslise.locks.common.network.toserver.RenameKeyPacket;
import melonslise.locks.common.network.toserver.TryPinPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class LocksNetwork
{
	private LocksNetwork() {}

	public static void register(IEventBus modBus)
	{
		modBus.addListener(RegisterPayloadHandlersEvent.class, event -> {
			PayloadRegistrar registrar = event.registrar("1");

			registrar.playToClient(
				AddLockablePacket.TYPE,
				AddLockablePacket.STREAM_CODEC,
				(payload, context) -> context.enqueueWork(() ->
					AddLockablePacket.handle(payload, (LocalPlayer) context.player())
				)
			);

			registrar.playToClient(
				AddLockableToChunkPacket.TYPE,
				AddLockableToChunkPacket.STREAM_CODEC,
				(payload, context) -> context.enqueueWork(() ->
					AddLockableToChunkPacket.handle(payload, (LocalPlayer) context.player())
				)
			);

			registrar.playToClient(
				RemoveLockablePacket.TYPE,
				RemoveLockablePacket.STREAM_CODEC,
				(payload, context) -> context.enqueueWork(() ->
					RemoveLockablePacket.handle(payload, (LocalPlayer) context.player())
				)
			);

			registrar.playToClient(
				TryPinResultPacket.TYPE,
				TryPinResultPacket.STREAM_CODEC,
				(payload, context) -> context.enqueueWork(() ->
					TryPinResultPacket.handle(payload, (LocalPlayer) context.player())
				)
			);

			registrar.playToClient(
				UpdateLockablePacket.TYPE,
				UpdateLockablePacket.STREAM_CODEC,
				(payload, context) -> context.enqueueWork(() ->
					UpdateLockablePacket.handle(payload, (LocalPlayer) context.player())
				)
			);

			registrar.playToServer(
				TryPinPacket.TYPE,
				TryPinPacket.STREAM_CODEC,
				(payload, context) -> context.enqueueWork(() ->
					TryPinPacket.handle(payload, (ServerPlayer) context.player())
				)
			);

			registrar.playToServer(
				RenameKeyPacket.TYPE,
				RenameKeyPacket.STREAM_CODEC,
				(payload, context) -> context.enqueueWork(() ->
					RenameKeyPacket.handle(payload, (ServerPlayer) context.player())
				)
			);
		});
	}
}
