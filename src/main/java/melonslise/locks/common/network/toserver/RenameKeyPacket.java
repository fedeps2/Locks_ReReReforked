package melonslise.locks.common.network.toserver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import melonslise.locks.Locks;
import melonslise.locks.common.components.ItemHandler;
import melonslise.locks.common.init.LocksComponents;
import melonslise.locks.common.item.KeyItem;
import melonslise.locks.common.item.KeyRingItem;
import melonslise.locks.common.item.LockingItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record RenameKeyPacket(int slotIndex, int keyId, String name) implements CustomPacketPayload {
	public static final int NO_KEY_ID = Integer.MIN_VALUE;

	public static final CustomPacketPayload.Type<RenameKeyPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Locks.ID, "rename_key"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(RenameKeyPacket packet, ServerPlayer player) {
		String name = sanitize(packet.name());
		if (packet.keyId() != NO_KEY_ID) {
			if (tryRenameById(player, packet.keyId(), name))
				sync(player);
			return;
		}
		if (tryRenameBySlot(player, packet.slotIndex(), name))
			sync(player);
	}

	private static boolean tryRenameById(ServerPlayer player, int keyId, String name) {
		Inventory inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize(); ++i) {
			ItemStack stack = inv.getItem(i);
			if (stack.isEmpty())
				continue;
			if (stack.getItem() instanceof KeyItem) {
				if (Objects.equals(LockingItem.getId(stack), keyId)) {
					applyName(stack, name);
					return true;
				}
			} else if (stack.getItem() instanceof KeyRingItem && tryRenameInKeyRing(stack, keyId, name)) {
				return true;
			}
		}

		Optional<ICuriosItemHandler> curios = CuriosApi.getCuriosInventory(player);
		if (curios.isPresent()) {
			for (Map.Entry<String, ICurioStacksHandler> entry : curios.get().getCurios().entrySet()) {
				ICurioStacksHandler stacks = entry.getValue();
				for (int i = 0; i < stacks.getSlots(); ++i) {
					ItemStack stack = stacks.getStacks().getStackInSlot(i);
					if (stack.getItem() instanceof KeyRingItem && tryRenameInKeyRing(stack, keyId, name))
						return true;
				}
			}
		}
		return false;
	}

	private static boolean tryRenameInKeyRing(ItemStack keyRingStack, int keyId, String name) {
		ItemHandler handler = keyRingStack.get(LocksComponents.ITEM_HANDLER.get());
		if (handler == null)
			return false;
		for (int i = 0; i < handler.getSlots(); ++i) {
			ItemStack nested = handler.getStackInSlot(i);
			if (!nested.isEmpty() && nested.getItem() instanceof KeyItem
					&& Objects.equals(LockingItem.getId(nested), keyId)) {
				applyName(nested, name);
				keyRingStack.set(LocksComponents.ITEM_HANDLER.get(), handler.copy());
				return true;
			}
		}
		return false;
	}

	private static boolean tryRenameBySlot(ServerPlayer player, int idx, String name) {
		AbstractContainerMenu menu = player.containerMenu;
		if (idx < 0 || idx >= menu.slots.size())
			return false;
		Slot slot = menu.getSlot(idx);
		if (!slot.hasItem() || !(slot.getItem().getItem() instanceof KeyItem))
			return false;
		applyName(slot.getItem(), name);
		slot.setChanged();
		return true;
	}

	private static void applyName(ItemStack stack, String name) {
		if (name.isEmpty())
			stack.remove(DataComponents.CUSTOM_NAME);
		else
			stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
	}

	private static void sync(ServerPlayer player) {
		player.containerMenu.broadcastChanges();
		player.inventoryMenu.broadcastChanges();
	}

	private static String sanitize(String input) {
		String cleaned = input.replaceAll("[\\u00A7\\u0000-\\u001F]", "").trim();
		if (cleaned.length() > 50)
			cleaned = cleaned.substring(0, 50);
		return cleaned;
	}

	public static final Codec<RenameKeyPacket> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					Codec.INT.fieldOf("slotIndex").forGetter(RenameKeyPacket::slotIndex),
					Codec.INT.fieldOf("keyId").forGetter(RenameKeyPacket::keyId),
					Codec.STRING.fieldOf("name").forGetter(RenameKeyPacket::name)
			).apply(instance, RenameKeyPacket::new)
	);

	public static final StreamCodec<ByteBuf, RenameKeyPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, RenameKeyPacket::slotIndex,
			ByteBufCodecs.INT, RenameKeyPacket::keyId,
			ByteBufCodecs.STRING_UTF8, RenameKeyPacket::name,
			RenameKeyPacket::new
	);
}
