package git.jbredwards.colored_shulkers;

import git.jbredwards.colored_shulkers.registry.ItemColoredShell;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Random;

/**
 *
 * @author jbred
 *
 */
public interface ShulkerUtils
{
    // ----
    // ITEM
    // ----

    @Nonnull
    static Optional<EnumDyeColor> colorFromShell(@Nonnull final ItemStack stack) {
        if(stack.getItem() == Items.SHULKER_SHELL) return Optional.of(EnumDyeColor.PURPLE);
        else if(stack.getItem() == ColoredShulkers.SHELL) return ItemColoredShell.byShellDamage(stack.getMetadata());
        // Invalid shell.
        return Optional.empty();
    }

    @Nonnull
    static ItemStack shellFromColor(@Nonnull final EnumDyeColor color, final int count) {
        if(color == EnumDyeColor.PURPLE) return new ItemStack(Items.SHULKER_SHELL, count);
        final int meta = color.getMetadata();
        return new ItemStack(ColoredShulkers.SHELL, count, meta > EnumDyeColor.PURPLE.getMetadata() ? meta - 1 : meta);
    }

    // ------
    // ENTITY
    // ------

    @Nonnull
    String DROPS_TAG = Tags.MOD_ID + ":drops", RAINBOW_TAG = Tags.MOD_ID + ":rainbow";

    @Nonnull
    static EnumDyeColor getColor(@Nonnull final EntityShulker shulker) {
        return EnumDyeColor.byMetadata(shulker.getDataManager().get(EntityShulker.COLOR));
    }

    static void setColor(@Nonnull final EntityShulker shulker, @Nonnull final EnumDyeColor color) {
        shulker.getDataManager().set(EntityShulker.COLOR, (byte)color.getMetadata());
        shulker.getEntityData().setBoolean(RAINBOW_TAG, false);
    }

    static boolean isRainbow(@Nonnull final EntityShulker shulker) {
        return shulker.getEntityData().getBoolean(RAINBOW_TAG);
    }

    static void setRainbow(@Nonnull final EntityShulker shulker) {
        shulker.getDataManager().set(EntityShulker.COLOR, (byte)0);
        shulker.getEntityData().setBoolean(RAINBOW_TAG, true);
    }

    static void setRandomColor(@Nonnull final EntityShulker shulker, @Nonnull final Random rand, @Nonnull final ColoredShulkers.Cfg.EnableType cfg) {
        if(cfg != ColoredShulkers.Cfg.EnableType.DISABLED) {
            if(cfg == ColoredShulkers.Cfg.EnableType.ENABLED && rand.nextDouble() < ColoredShulkers.Cfg.shulkerChanceRainbow) setRainbow(shulker);
            else setColor(shulker, WeightedRandom.getRandomItem(rand, ColoredShulkers.Cfg.WEIGHTS).color);
        }
    }
}
