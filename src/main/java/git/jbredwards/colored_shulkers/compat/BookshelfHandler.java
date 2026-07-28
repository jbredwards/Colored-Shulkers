package git.jbredwards.colored_shulkers.compat;

import git.jbredwards.colored_shulkers.ColoredShulkers;
import git.jbredwards.colored_shulkers.dying.ShulkerDying;
import net.darkhax.bookshelf.BookshelfConfig;
import net.darkhax.bookshelf.util.OreDictUtils;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public class BookshelfHandler
{
    public static void registerOres() {
        if(BookshelfConfig.oreDictShulker) {
            for(@Nonnull final EnumDyeColor color : EnumDyeColor.values()) OreDictionary.registerOre(
                    OreDictUtils.SHULKER_BOX + ShulkerDying.dyeFromColor(color), BlockShulkerBox.getBlockByColor(color));

            OreDictionary.registerOre(OreDictUtils.CHEST, ColoredShulkers.PURPLE_SHULKER_BOX);
            OreDictionary.registerOre(OreDictUtils.CHEST, ColoredShulkers.RAINBOW_SHULKER_BOX);
            OreDictionary.registerOre(OreDictUtils.SHULKER_BOX, ColoredShulkers.PURPLE_SHULKER_BOX);
            OreDictionary.registerOre(OreDictUtils.SHULKER_BOX, ColoredShulkers.RAINBOW_SHULKER_BOX);
            OreDictionary.registerOre(OreDictUtils.SHULKER_BOX + "Colorless", Blocks.PURPLE_SHULKER_BOX);
            OreDictionary.registerOre(OreDictUtils.SHULKER_BOX + "Rainbow", ColoredShulkers.RAINBOW_SHULKER_BOX);
        }
    }
}
