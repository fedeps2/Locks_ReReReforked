package melonslise.locks.common.init;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

import java.util.List;

public class LocksVillagerTrades {

    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.TOOLSMITH) {
            var trades = event.getTrades();

            trades.get(1).add(new VillagerTrades.ItemsForEmeralds(
                    new ItemStack(LocksItems.WOOD_LOCK_PICK.get()), 1, 2, 16, 2, 0.05f));
            trades.get(1).add(new VillagerTrades.ItemsForEmeralds(
                    new ItemStack(LocksItems.WOOD_LOCK_MECHANISM.get()), 2, 1, 12, 1, 0.2f));

            trades.get(2).add(new VillagerTrades.ItemsForEmeralds(
                    new ItemStack(LocksItems.IRON_LOCK_PICK.get()), 2, 2, 16, 5, 0.05f));
            trades.get(2).add(new VillagerTrades.ItemsForEmeralds(
                    new ItemStack(LocksItems.IRON_LOCK_MECHANISM.get()), 3, 1, 12, 5, 0.2f));

            trades.get(3).add(new VillagerTrades.ItemsForEmeralds(
                    new ItemStack(LocksItems.GOLD_LOCK_PICK.get()), 6, 2, 12, 20, 0.05f));
            trades.get(3).add(new VillagerTrades.ItemsForEmeralds(
                    new ItemStack(LocksItems.IRON_LOCK_MECHANISM.get()), 5, 1, 8, 10, 0.2f));

            trades.get(4).add(new VillagerTrades.ItemsForEmeralds(
                    new ItemStack(LocksItems.STEEL_LOCK_PICK.get()), 4, 2, 16, 20, 0.05f));

            trades.get(5).add(new VillagerTrades.ItemsForEmeralds(
                    new ItemStack(LocksItems.DIAMOND_LOCK_PICK.get()), 8, 2, 12, 30, 0.05f));
            trades.get(5).add(new VillagerTrades.ItemsForEmeralds(
                    new ItemStack(LocksItems.STEEL_LOCK_MECHANISM.get()), 8, 1, 8, 30, 0.2f));
        }
    }

    public static void onWanderingTrades(WandererTradesEvent event) {
        List<VillagerTrades.ItemListing> trades = event.getGenericTrades();

        trades.add(new VillagerTrades.ItemsForEmeralds(
                LocksItems.GOLD_LOCK_PICK.get(), 5, 2, 6, 1));
        trades.add(new VillagerTrades.ItemsForEmeralds(
                LocksItems.STEEL_LOCK_PICK.get(), 3, 2, 8, 1));
        trades.add(new VillagerTrades.EnchantedItemForEmeralds(
                LocksItems.STEEL_LOCK.get(), 16, 4, 1));
        trades.add(new VillagerTrades.ItemsForEmeralds(
                LocksItems.STEEL_LOCK_MECHANISM.get(), 6, 1, 4, 1));
        trades.add(new VillagerTrades.EnchantedItemForEmeralds(
                LocksItems.DIAMOND_LOCK.get(), 28, 4, 1));
    }
}
