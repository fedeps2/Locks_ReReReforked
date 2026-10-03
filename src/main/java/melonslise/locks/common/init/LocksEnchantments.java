package melonslise.locks.common.init;

import melonslise.locks.Locks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.ArrayList;
import java.util.List;

// Enchantments live in the datapack registry (Registries.ENCHANTMENT) in 1.21.1, so they cannot be
// created with a DeferredRegister. They are defined as JSON under data/locks/enchantment/*.json and
// only looked up through the level's registry access here.
public final class LocksEnchantments
{
	private LocksEnchantments() {}

	public static final ResourceKey<Enchantment> SHOCKING_KEY = key("shocking");
	public static final ResourceKey<Enchantment> STURDY_KEY = key("sturdy");
	public static final ResourceKey<Enchantment> COMPLEXITY_KEY = key("complexity");

	private static ResourceKey<Enchantment> key(String name)
	{
		return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(Locks.ID, name));
	}

	// Datapack-registry holders cannot be resolved via Holder.value() at runtime
	// (throws "Registry not present"). Enchantments must be looked up from the level's registry access.
	public static List<Holder<Enchantment>> getEnchantmentHolders(HolderLookup.Provider registries, List<ResourceKey<Enchantment>> keys)
	{
		HolderLookup.RegistryLookup<Enchantment> lookup = registries.lookupOrThrow(Registries.ENCHANTMENT);
		List<Holder<Enchantment>> holders = new ArrayList<>(keys.size());
		for (ResourceKey<Enchantment> key : keys)
			lookup.get(key).ifPresent(holders::add);
		return holders;
	}

	public static int getEnchantmentLevel(HolderLookup.Provider registries, ItemStack stack, ResourceKey<Enchantment> key)
	{
		return registries.lookupOrThrow(Registries.ENCHANTMENT).get(key)
				.map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack))
				.orElse(0);
	}
}
