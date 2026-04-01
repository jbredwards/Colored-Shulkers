package git.jbredwards.colored_shulkers;

import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.item.EnumDyeColor;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 *
 * @author jbred
 *
 */
public interface ShulkerUtils
{
    @Nonnull
    static Optional<EnumDyeColor> byShellDamage(final int meta) {
        if(meta >= 15) return Optional.empty(); // Invalid shell.
        else return Optional.of(EnumDyeColor.byDyeDamage(meta >= EnumDyeColor.PURPLE.getDyeDamage() ? meta + 1 : meta));
    }

    @Nonnull
    static EnumDyeColor getColor(@Nonnull final EntityShulker shulker) {
        return EnumDyeColor.byMetadata(shulker.getDataManager().get(EntityShulker.COLOR));
    }

    static void setColor(@Nonnull final EntityShulker shulker, @Nonnull final EnumDyeColor color) {
        shulker.getDataManager().set(EntityShulker.COLOR, (byte)color.getMetadata());
    }
}
