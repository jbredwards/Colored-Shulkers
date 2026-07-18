package git.jbredwards.colored_shulkers.registry;

import git.jbredwards.colored_shulkers.Tags;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author jbred
 *
 */
public class ItemColoredShell extends Item
{
    @Nonnull
    private static final TextFormatting[] rainbowFormat = new TextFormatting[] { TextFormatting.RED, TextFormatting.GOLD, TextFormatting.YELLOW, TextFormatting.GREEN, TextFormatting.BLUE, TextFormatting.DARK_PURPLE, TextFormatting.LIGHT_PURPLE };
    public ItemColoredShell() { setHasSubtypes(true).setCreativeTab(CreativeTabs.MATERIALS); }

    @Override
    public void getSubItems(@Nonnull final CreativeTabs tab, @Nonnull final NonNullList<ItemStack> items) {
        if(isInCreativeTab(tab)) for(int meta = 0; meta < 16; meta++) items.add(new ItemStack(this, 1, meta));
    }

    @Nonnull
    public static Optional<EnumDyeColor> byShellDamage(final int meta) {
        if(meta >= 15) return Optional.empty(); // Invalid shell.
        return Optional.of(EnumDyeColor.byMetadata(meta >= EnumDyeColor.PURPLE.getMetadata() ? meta + 1 : meta));
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    public static String localizeColor(@Nonnull final EnumDyeColor color) {
        return I18n.format("color." + Tags.MOD_ID + '.' + color.getTranslationKey(), I18n.format("item.fireworksCharge." + color.getTranslationKey()));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(@Nonnull final ItemStack stack, @Nullable final World worldIn, @Nonnull final List<String> tooltip, @Nonnull final ITooltipFlag flagIn) {
        if(stack.getMetadata() == 15) {
            tooltip.add(1, rainbowFormat[RainbowShulkerBox.getTicks(0.00075) % rainbowFormat.length] + I18n.format("color." + Tags.MOD_ID + ".rainbow"));
            /*final int time = RainbowShulkerBoxTESR.getTicks(0.005);

            @Nonnull final String text = I18n.format("color." + Tags.MOD_ID + ".rainbow");
            @Nonnull final StringBuilder builder = new StringBuilder();

            for(int i = 0; i < text.length(); i++) {
                builder.append(rainbowFormat[(time + i) % rainbowFormat.length]);
                builder.append(text.charAt(i));
            }

            builder.append(TextFormatting.RESET);
            tooltip.add(1, builder.toString());*/
        }
    }

    @Nonnull
    @Override
    public EnumRarity getRarity(@Nonnull final ItemStack stack) {
        return stack.getMetadata() == 15 ? EnumRarity.RARE : super.getRarity(stack);
    }
}
