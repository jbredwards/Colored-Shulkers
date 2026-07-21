package git.jbredwards.colored_shulkers.compat;

import git.jbredwards.colored_shulkers.ColoredShulkers;
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
    public static void registerOres(@Nonnull final String[] dyes) {
        if(BookshelfConfig.oreDictShulker) {
            for(int i = 0; i < dyes.length; i++) OreDictionary.registerOre(
                    OreDictUtils.SHULKER_BOX + dyes[i],
                    BlockShulkerBox.getBlockByColor(EnumDyeColor.byDyeDamage(i)));

            OreDictionary.registerOre(OreDictUtils.CHEST, ColoredShulkers.PURPLE_SHULKER_BOX);
            OreDictionary.registerOre(OreDictUtils.CHEST, ColoredShulkers.RAINBOW_SHULKER_BOX);
            OreDictionary.registerOre(OreDictUtils.SHULKER_BOX, ColoredShulkers.PURPLE_SHULKER_BOX);
            OreDictionary.registerOre(OreDictUtils.SHULKER_BOX, ColoredShulkers.RAINBOW_SHULKER_BOX);
            OreDictionary.registerOre(OreDictUtils.SHULKER_BOX + "Colorless", Blocks.PURPLE_SHULKER_BOX);
            OreDictionary.registerOre(OreDictUtils.SHULKER_BOX + "Rainbow", ColoredShulkers.RAINBOW_SHULKER_BOX);
        }
    }
}
