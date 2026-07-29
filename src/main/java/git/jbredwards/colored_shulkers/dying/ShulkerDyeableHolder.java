package git.jbredwards.colored_shulkers.dying;

import git.jbredwards.colored_shulkers.ColoredShulkers;
import git.jbredwards.colored_shulkers.ShulkerUtils;
import git.jbredwards.colored_shulkers.config.ColoredShulkersCfg;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
public interface ShulkerDyeableHolder
{
    @Nullable
    EnumDyeColor getColor();
    void setColor(@Nullable final EnumDyeColor color);

    boolean isRainbow();
    void setRainbow();

    @Nonnull
    AxisAlignedBB getBoundingBox();

    @Nonnull
    static ShulkerDyeableHolder block(@Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final IBlockState shulkerBox) {
        return new ShulkerDyeableHolder() {
            @Nullable
            @Override
            public EnumDyeColor getColor() {
                return ShulkerUtils.SHULKER_BOXES.inverse().get(shulkerBox.getBlock());
            }

            @Override
            public void setColor(@Nullable final EnumDyeColor color) {
                set(ShulkerUtils.SHULKER_BOXES.get(color));
            }

            @Override
            public boolean isRainbow() {
                return shulkerBox.getBlock() == ColoredShulkers.RAINBOW_SHULKER_BOX;
            }

            @Override
            public void setRainbow() {
                set(ColoredShulkers.RAINBOW_SHULKER_BOX);
            }

            void set(@Nonnull final Block newBlock) {
                if(!world.isRemote) {
                    @Nonnull final IBlockState newState = newBlock.getStateFromMeta(shulkerBox.getBlock().getMetaFromState(shulkerBox));
                    @Nonnull final TileEntityShulkerBox newTile = (TileEntityShulkerBox)newBlock.createTileEntity(world, newState);
                    // Copy data from old tile to new tile.
                    @Nullable final TileEntity oldTile = world.getTileEntity(pos);
                    if(oldTile instanceof TileEntityShulkerBox) {
                        newTile.loadFromNbt(((TileEntityShulkerBox)oldTile).saveToNbt(new NBTTagCompound()));
                        ((TileEntityShulkerBox)oldTile).clear();
                    }
                    // Set new box in world.
                    world.setBlockState(pos, newState);
                    world.setTileEntity(pos, newTile);
                }
            }

            @Nonnull
            @Override
            public AxisAlignedBB getBoundingBox() {
                return shulkerBox.getBoundingBox(world, pos).offset(pos);
            }
        };
    }

    @Nonnull
    static ShulkerDyeableHolder entity(@Nonnull final EntityShulker shulker) {
        return new ShulkerDyeableHolder() {
            @Nullable
            @Override
            public EnumDyeColor getColor() {
                return ShulkerUtils.getColor(shulker).orElse(null);
            }

            @Override
            public void setColor(@Nullable final EnumDyeColor color) {
                if(shulker.world.isRemote) return;
                ShulkerUtils.setColor(shulker, color);
                postSet();
            }

            @Override
            public boolean isRainbow() {
                return ShulkerUtils.isRainbow(shulker);
            }

            @Override
            public void setRainbow() {
                if(shulker.world.isRemote) return;
                ShulkerUtils.setRainbow(shulker);
                postSet();
            }

            void postSet() {
                // Disable shell drops if shell was changed.
                shulker.getEntityData().setBoolean(ShulkerUtils.DROPS_TAG, ColoredShulkersCfg.inWorldDyingNoDrops);
                // Close shulker.
                shulker.updateArmorModifier(0);
            }

            @Nonnull
            @Override
            public AxisAlignedBB getBoundingBox() {
                return shulker.getEntityBoundingBox();
            }
        };
    }
}
