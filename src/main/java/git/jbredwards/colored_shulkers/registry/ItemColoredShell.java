package git.jbredwards.colored_shulkers.registry;

import git.jbredwards.colored_shulkers.ColoredShulkers;
import git.jbredwards.colored_shulkers.Tags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.quark.api.ICustomEnchantColor;

import javax.annotation.Nonnull;
import java.awt.*;

/**
 *
 * @author jbred
 *
 */
@Optional.Interface(modid = "quark", iface = "vazkii.quark.api.ICustomEnchantColor")
public class ItemColoredShell extends Item implements ICustomEnchantColor
{
    public ItemColoredShell() { setHasSubtypes(true).setCreativeTab(CreativeTabs.MATERIALS); }

    @Nonnull
    public static ItemStack create(@Nonnull final EnumDyeColor color, final int count) {
        final int meta = color.getDyeDamage();
        return new ItemStack(ColoredShulkers.SHELL, count, meta >= EnumDyeColor.PURPLE.getDyeDamage() ? meta - 1 : meta);
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    public static String localizeColor(@Nonnull final EnumDyeColor color) {
        return I18n.format("color." + Tags.MOD_ID + '.' + color.getTranslationKey(),
                I18n.format("item.fireworksCharge." + color.getTranslationKey()));
    }

    @Override
    public void getSubItems(@Nonnull final CreativeTabs tab, @Nonnull final NonNullList<ItemStack> items) {
        if(isInCreativeTab(tab)) {
            for(int meta = 0; meta < 15; meta++) items.add(new ItemStack(this, 1, meta));
            if(ColoredShulkers.QUARK) items.add(new ItemStack(this, 1, 15));
        }
    }

    // ----------

    @SideOnly(Side.CLIENT)
    @Optional.Method(modid = "quark")
    @Override
    public boolean hasEffect(@Nonnull final ItemStack stack) {
        return stack.getMetadata() == 15 || super.hasEffect(stack);
    }

    @Optional.Method(modid = "quark")
    @Override
    public int getEnchantEffectColor(@Nonnull final ItemStack stack) {
        return Color.HSBtoRGB((Minecraft.getSystemTime() + Minecraft.getMinecraft().getRenderPartialTicks()) * 0.005f, 1, 0.6f);
    }
}
