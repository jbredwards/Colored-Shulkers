package git.jbredwards.colored_shulkers;

import com.google.common.collect.BiMap;
import git.jbredwards.colored_shulkers.config.ColoredShulkersCfg;
import git.jbredwards.colored_shulkers.registry.ItemColoredShell;
import net.minecraft.block.Block;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

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

    int RAINBOW_META = 16;

    @Nonnull BiMap<EnumDyeColor, Block> SHULKER_BOXES = ShulkerEvents.shulkerBoxes();
    @Nonnull Set<EnumDyeColor> SHELL_COLORS = ShulkerEvents.shellColors();

    @Nonnull
    static Optional<EnumDyeColor> colorFromShell(@Nonnull final ItemStack stack) {
        if(stack.getItem() == ColoredShulkers.SHELL) return ItemColoredShell.byShellDamage(stack.getMetadata());
        // Invalid shell.
        return Optional.empty();
    }

    @Nonnull
    static ItemStack shellFromColor(@Nullable final EnumDyeColor color, final int count) {
        if(color == null) return new ItemStack(Items.SHULKER_SHELL, count);
        else return new ItemStack(ColoredShulkers.SHELL, count, color.getMetadata());
    }

    // ------
    // ENTITY
    // ------

    @Nonnull
    String DROPS_TAG = Tags.MOD_ID + ":drops", PURPLE_TAG = Tags.MOD_ID + ":purple", RAINBOW_TAG = Tags.MOD_ID + ":rainbow";

    @Nonnull
    static Optional<EnumDyeColor> getColor(@Nonnull final EntityShulker shulker) {
        if(shulker.getEntityData().getBoolean(PURPLE_TAG)) return Optional.of(EnumDyeColor.PURPLE);
        @Nonnull final EnumDyeColor color = EnumDyeColor.byMetadata(shulker.getDataManager().get(EntityShulker.COLOR));
        return color == EnumDyeColor.PURPLE ? Optional.empty() : Optional.of(color);
    }

    static void setColor(@Nonnull final EntityShulker shulker, @Nullable final EnumDyeColor color) {
        if(color == EnumDyeColor.PURPLE) {
            shulker.getDataManager().set(EntityShulker.COLOR, (byte)EnumDyeColor.WHITE.getMetadata());
            shulker.getEntityData().setBoolean(PURPLE_TAG, true);
        }
        else {
            shulker.getDataManager().set(EntityShulker.COLOR, (byte)(color != null ? color : EnumDyeColor.PURPLE).getMetadata());
            shulker.getEntityData().setBoolean(PURPLE_TAG, false);
        }
        shulker.getEntityData().setBoolean(RAINBOW_TAG, false);
    }

    static boolean isRainbow(@Nonnull final EntityShulker shulker) {
        return shulker.getEntityData().getBoolean(RAINBOW_TAG);
    }

    static void setRainbow(@Nonnull final EntityShulker shulker) {
        shulker.getDataManager().set(EntityShulker.COLOR, (byte)0);
        shulker.getEntityData().setBoolean(PURPLE_TAG, false);
        shulker.getEntityData().setBoolean(RAINBOW_TAG, true);
    }

    static void setRandomColor(@Nonnull final EntityShulker shulker, @Nonnull final Random rand, @Nonnull final ColoredShulkersCfg.EnableType cfg) {
        if(cfg != ColoredShulkersCfg.EnableType.DISABLED) {
            if(cfg == ColoredShulkersCfg.EnableType.ENABLED && rand.nextDouble() < ColoredShulkersCfg.shulkerChanceRainbow) setRainbow(shulker);
            else setColor(shulker, WeightedRandom.getRandomItem(rand, ColoredShulkersCfg.WEIGHTS).color);
        }
    }
}
