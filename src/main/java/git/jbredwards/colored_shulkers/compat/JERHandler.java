package git.jbredwards.colored_shulkers.compat;

import git.jbredwards.colored_shulkers.ColoredShulkers;
import git.jbredwards.colored_shulkers.ShulkerUtils;
import git.jbredwards.colored_shulkers.config.ColoredShulkersCfg;
import git.jbredwards.colored_shulkers.registry.ItemColoredShell;
import io.netty.util.internal.IntegerHolder;
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
        for(@Nonnull final IntegerHolder meta = new IntegerHolder(); meta.value <= ShulkerUtils.RAINBOW_META; meta.value++) {
            @Nonnull final EntityShulker shulker = new EntityShulker(CompatBase.getWorld());

            if(meta.value == ShulkerUtils.RAINBOW_META) ShulkerUtils.setRainbow(shulker);
            else ShulkerUtils.setColor(shulker, ItemColoredShell.byShellDamage(meta.value).orElseThrow(IllegalStateException::new));

            JERAPI.getInstance().getMobRegistry().register(shulker, LootTableHelper.toDrops(defaultLootTable).stream()
                    .peek(drop -> {
                        if(ColoredShulkersCfg.enableDrops && drop.item.getItem() == Items.SHULKER_SHELL)
                            drop.item = new ItemStack(ColoredShulkers.SHELL, drop.item.getCount(), meta.value);
                    })
                    .toArray(LootDrop[]::new));
        }
    }
}
