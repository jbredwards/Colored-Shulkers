package git.jbredwards.colored_shulkers;

import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.item.EnumDyeColor;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public interface ShulkerUtils
{
    @Nonnull
    static EnumDyeColor byShellDamage(final int meta) {
        return EnumDyeColor.byDyeDamage(meta >= EnumDyeColor.PURPLE.getDyeDamage() ? meta + 1 : meta);
    }

    @Nonnull
    static EnumDyeColor getColor(@Nonnull final EntityShulker shulker) {
        return EnumDyeColor.byMetadata(shulker.getDataManager().get(EntityShulker.COLOR));
    }

    static void setColor(@Nonnull final EntityShulker shulker, @Nonnull final EnumDyeColor color) {
        shulker.getDataManager().set(EntityShulker.COLOR, (byte)color.getMetadata());
    }
}
