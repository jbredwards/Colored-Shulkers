package git.jbredwards.colored_shulkers.registry;

import git.jbredwards.colored_shulkers.ColoredShulkers;
import git.jbredwards.colored_shulkers.ShulkerUtils;
import git.jbredwards.colored_shulkers.Tags;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelShulker;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntityShulkerBoxRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

/**
 *
 * @author jbred
 *
 */
public final class RainbowShulkerBox
{
    @Nonnull
    public static final CreativeTabs TAB = new CreativeTabs(Tags.MOD_ID + ".tab") {
        @Nonnull
        @SideOnly(Side.CLIENT)
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ColoredShulkers.RAINBOW_SHULKER_BOX);
        }
    };

    @SideOnly(Side.CLIENT)
    public static int getRGB() {
        return Color.HSBtoRGB(getTicks(0.05) / 255f, 0.8f, 1);
    }

    @SideOnly(Side.CLIENT)
    public static int getTicks(final double factor) {
        return (int)((long)(Minecraft.getSystemTime() * factor) & 255);
    }

    @SideOnly(Side.CLIENT)
    public static void setRGB(@Nonnull final Block shulkerBox, final float alpha) {
        // Set the "last color state" so it's the same as the main renderer when that sets its color.
        GlStateManager.color(1, 1, 1, alpha);
        // Set the rendered color.
        final int rgb = shulkerBox == ColoredShulkers.RAINBOW_SHULKER_BOX ? getRGB() : EnumDyeColor.PURPLE.getColorValue();
        GL11.glColor4f((rgb >> 16 & 255) / 255f, (rgb >> 8 & 255) / 255f, (rgb & 255) / 255f, alpha);
    }

    @SideOnly(Side.CLIENT)
    public static class TEISR extends TileEntityItemStackRenderer
    {
        @Nonnull
        private final Tile dummyTile;
        public TEISR(final boolean rainbow) {
            dummyTile = new Tile() {
                @Nonnull
                @Override
                public Block getBlockType() {
                    return rainbow ? ColoredShulkers.RAINBOW_SHULKER_BOX : ColoredShulkers.PURPLE_SHULKER_BOX;
                }
            };
        }

        @Override
        public void renderByItem(@Nonnull final ItemStack stack, final float partialTicks) {
            TileEntityRendererDispatcher.instance.render(dummyTile, 0, 0, 0, 0, partialTicks);
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
            if(destroyStage < 0) setRGB(te.getBlockType(), alpha);
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

    public static class Sync implements IMessage, IMessageHandler<Sync, IMessage>
    {
        public int id;
        public boolean rainbow;
        public boolean purple;

        @Override
        public void fromBytes(@Nonnull final ByteBuf buf) {
            id = buf.readInt();
            rainbow = buf.readBoolean();
            purple = buf.readBoolean();
        }

        @Override
        public void toBytes(@Nonnull final ByteBuf buf) {
            buf.writeInt(id);
            buf.writeBoolean(rainbow);
            buf.writeBoolean(purple);
        }

        @Nullable
        @Override
        public IMessage onMessage(@Nonnull final Sync message, @Nonnull final MessageContext ctx) {
            if(ctx.side.isClient()) Minecraft.getMinecraft().addScheduledTask(() -> {
                @Nullable final World world = Minecraft.getMinecraft().world;
                if(world != null) {
                    @Nullable final Entity entity = world.getEntityByID(message.id);
                    if(entity instanceof EntityShulker) {
                        if(message.rainbow) ShulkerUtils.setRainbow((EntityShulker)entity);
                        else if(message.purple) ShulkerUtils.setColor((EntityShulker)entity, EnumDyeColor.PURPLE);
                    }
                }
            });

            return null;
        }
    }
}
