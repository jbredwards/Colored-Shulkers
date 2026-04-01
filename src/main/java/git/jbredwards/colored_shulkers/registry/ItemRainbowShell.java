package git.jbredwards.colored_shulkers.registry;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.arl.util.ClientTicker;
import vazkii.quark.api.ICustomEnchantColor;

import javax.annotation.Nonnull;
import java.awt.*;

/**
 *
 * @author jbred
 *
 */
@Optional.Interface(modid = "quark", iface = "vazkii.quark.api.ICustomEnchantColor")
public class ItemRainbowShell extends Item implements ICustomEnchantColor
{
    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasEffect(@Nonnull final ItemStack stack) { return true; }

    @Optional.Method(modid = "quark")
    @Override
    public int getEnchantEffectColor(@Nonnull final ItemStack stack) {
        return Color.HSBtoRGB(ClientTicker.total * 0.005f, 1, 0.6f);
    }
}
