package git.jbredwards.colored_shulkers.registry;

import git.jbredwards.colored_shulkers.ColoredShulkers;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelShulker;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntityShulkerBoxRenderer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.awt.*;

/**
 *
 * @author jbred
 *
 */
public final class RainbowShulkerBox
{
    @SideOnly(Side.CLIENT)
    public static int getRGB() {
        return Color.HSBtoRGB(getTicks(0.05) / 255f, 0.8f, 1);
    }

    @SideOnly(Side.CLIENT)
    public static int getTicks(final double factor) {
        return (int)((long)(Minecraft.getSystemTime() * factor) & 255);
    }

    @SideOnly(Side.CLIENT)
    public static class TEISR extends TileEntityItemStackRenderer
    {
        @Nonnull
        private static final Tile DUMMY = new Tile();

        @Override
        public void renderByItem(@Nonnull final ItemStack stack, final float partialTicks) {
            TileEntityRendererDispatcher.instance.render(DUMMY, 0, 0, 0, 0, partialTicks);
        }
    }

    @SideOnly(Side.CLIENT)
    public static class TESR extends TileEntityShulkerBoxRenderer
    {
        public TESR(@Nonnull final ModelShulker modelIn) {
            super(modelIn);
        }

        @Override
        public void render(@Nonnull final TileEntityShulkerBox te, final double x, final double y, final double z, final float partialTicks, final int destroyStage, final float alpha) {
            if(destroyStage < 0) {
                // Set the "last color state" so it's the same as the main renderer when that sets its color.
                GlStateManager.color(1, 1, 1, alpha);
                // Set the rendered color.
                final int rgb = getRGB();
                GL11.glColor4f((rgb >> 16 & 255) / 255f, (rgb >> 8 & 255) / 255f, (rgb & 255) / 255f, alpha);
            }

            super.render(te, x, y, z, partialTicks, destroyStage, alpha);
            GL11.glColor4f(1, 1, 1, 1);
        }
    }

    // Mapped to the above TESR.
    public static class Tile extends TileEntityShulkerBox
    {
        public Tile() {
            super(EnumDyeColor.WHITE);
        }

        @Nonnull
        static BlockShulkerBox asBlock() {
            return new BlockShulkerBox(EnumDyeColor.WHITE) {
                @Nonnull
                @Override
                public TileEntity createNewTileEntity(@Nonnull final World worldIn, final int meta) {
                    return new Tile();
                }
            };
        }

        @Nonnull
        static ItemShulkerBox asItem() {
            return new ItemShulkerBox(ColoredShulkers.RAINBOW_SHULKER_BOX) {
                @Nonnull
                @Override
                public EnumRarity getRarity(@Nonnull final ItemStack stack) {
                    return EnumRarity.RARE;
                }
            };
        }
    }
}
