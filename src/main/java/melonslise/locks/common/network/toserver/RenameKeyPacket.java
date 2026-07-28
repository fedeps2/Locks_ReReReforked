package melonslise.locks.common.network.toserver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import melonslise.locks.Locks;
import melonslise.locks.common.item.KeyItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record RenameKeyPacket(String name) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<RenameKeyPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Locks.ID, "rename_key"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(RenameKeyPacket packet, ServerPlayer player) {
		ItemStack stack = player.getMainHandItem();
		if (!(stack.getItem() instanceof KeyItem))
			return;
		if (packet.name().isEmpty()) {
			stack.remove(DataComponents.CUSTOM_NAME);
		} else {
			String name = packet.name().length() > 50 ? packet.name().substring(0, 50) : packet.name();
			stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
		}
	}

	public static final Codec<RenameKeyPacket> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					Codec.STRING.fieldOf("name").forGetter(RenameKeyPacket::name)
			).apply(instance, RenameKeyPacket::new)
	);

	public static final StreamCodec<ByteBuf, RenameKeyPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, RenameKeyPacket::name,
			RenameKeyPacket::new
	);
}
