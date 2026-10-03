package melonslise.locks.client.init;

import melonslise.locks.Locks;
import melonslise.locks.common.components.ItemHandler;
import melonslise.locks.common.components.interfaces.IItemHandler;
import melonslise.locks.common.init.LocksComponents;
import melonslise.locks.common.init.LocksItems;
import melonslise.locks.common.item.LockItem;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

@net.neoforged.api.distmarker.OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)
public final class LocksItemModelsProperties
{
	private LocksItemModelsProperties() {}

	public static void register()
	{
		ClampedItemPropertyFunction keysGetter = (stack, world, entity, speed) ->
		{
			ItemHandler inv = stack.get(LocksComponents.ITEM_HANDLER.get());
			if (inv == null || inv.getSlots() == 0)
				return 0f;

			int keys = 0;
			for (int a = 0; a < inv.getSlots(); ++a) {
				if (!inv.getStackInSlot(a).isEmpty())
					++keys;
			}
			return keys == 0 ? 0f : keys == 1 ? 0.1f : keys == 2 ? 0.21f : 0.32f;
		};
		ResourceLocation keysId = ResourceLocation.fromNamespaceAndPath(Locks.ID, "keys");
		ItemProperties.register(LocksItems.KEY_RING.get(), keysId, keysGetter);
		ItemProperties.register(LocksItems.KEY_RING_LARGE.get(), keysId, keysGetter);
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Locks.ID, "open");
		ClampedItemPropertyFunction getter = (stack, world, entity, speed) -> LockItem.isOpen(stack) ? 1f : 0f;
		ItemProperties.register(LocksItems.WOOD_LOCK.get(), id, getter);
		ItemProperties.register(LocksItems.IRON_LOCK.get(), id, getter);
		ItemProperties.register(LocksItems.STEEL_LOCK.get(), id, getter);
		ItemProperties.register(LocksItems.GOLD_LOCK.get(), id, getter);
		ItemProperties.register(LocksItems.DIAMOND_LOCK.get(), id, getter);
	}
}
