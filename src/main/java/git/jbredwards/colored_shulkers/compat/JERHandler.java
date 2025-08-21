package git.jbredwards.colored_shulkers.compat;

import git.jbredwards.colored_shulkers.ColoredShulkers;
import git.jbredwards.colored_shulkers.ShulkerUtils;
import jeresources.api.drop.LootDrop;
import jeresources.compatibility.CompatBase;
import jeresources.compatibility.JERAPI;
import jeresources.util.LootTableHelper;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public final class JERHandler
{
    public static void postInit() {
        @Nonnull final LootTable defaultLootTable = LootTableHelper.getManager().getLootTableFromLocation(LootTableList.ENTITIES_SHULKER);
        for(@Nonnull final int[] meta = new int[1]; meta[0] < 15; meta[0]++) {
            @Nonnull final EntityShulker shulker = new EntityShulker(CompatBase.getWorld());

            ShulkerUtils.setColor(shulker, ShulkerUtils.byShellDamage(meta[0]));
            JERAPI.getInstance().getMobRegistry().register(shulker, LootTableHelper.toDrops(defaultLootTable).stream()
                    .peek(drop -> {
                        if(ColoredShulkers.Cfg.enableDrops && drop.item.getItem() == Items.SHULKER_SHELL)
                            drop.item = new ItemStack(ColoredShulkers.SHELL, drop.item.getCount(), meta[0]);
                    })
                    .toArray(LootDrop[]::new));
        }
    }
}
