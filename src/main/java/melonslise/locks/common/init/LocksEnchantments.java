package melonslise.locks.common.init;

import melonslise.locks.Locks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public final class LocksEnchantments
{
	public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(Registries.ENCHANTMENT, Locks.ID);

	public static final List<String> LOCK_ITEM_NAMES = List.of(
			"key_blank", "wood_lock", "iron_lock", "steel_lock", "gold_lock", "diamond_lock");

	private static HolderSet<Item> getLockItemHolders() {
		return HolderSet.direct(LOCK_ITEM_NAMES.stream().map(name ->
			(Holder<Item>) net.minecraft.core.registries.BuiltInRegistries.ITEM.getHolderOrThrow(
				ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Locks.ID, name)))
		).toList());
	}

	public static final DeferredHolder<Enchantment, Enchantment> SHOCKING = ENCHANTMENTS.register("shocking", () ->
		Enchantment.enchantment(Enchantment.definition(
				getLockItemHolders(), 1, 5,
				new Enchantment.Cost(7, 10),
				new Enchantment.Cost(22, 10),
				1, EquipmentSlotGroup.ANY))
			.build(ResourceLocation.fromNamespaceAndPath(Locks.ID, "shocking")));

	public static final DeferredHolder<Enchantment, Enchantment> STURDY = ENCHANTMENTS.register("sturdy", () ->
		Enchantment.enchantment(Enchantment.definition(
				getLockItemHolders(), 1, 3,
				new Enchantment.Cost(5, 15),
				new Enchantment.Cost(50, 0),
				1, EquipmentSlotGroup.ANY))
			.build(ResourceLocation.fromNamespaceAndPath(Locks.ID, "sturdy")));

	public static final DeferredHolder<Enchantment, Enchantment> COMPLEXITY = ENCHANTMENTS.register("complexity", () ->
		Enchantment.enchantment(Enchantment.definition(
				getLockItemHolders(), 1, 3,
				new Enchantment.Cost(7, 10),
				new Enchantment.Cost(22, 10),
				1, EquipmentSlotGroup.ANY))
			.build(ResourceLocation.fromNamespaceAndPath(Locks.ID, "complexity")));

	public static void register(IEventBus modBus) {
		ENCHANTMENTS.register(modBus);
	}
}
