package git.jbredwards.colored_shulkers.dying;

import git.jbredwards.colored_shulkers.ColoredShulkers;
import git.jbredwards.colored_shulkers.config.ColoredShulkersCfg;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author jbred
 *
 */
public interface ShulkerDyeableAction
{
    @Nonnull
    Map<Item, Function<ItemStack, ShulkerDyeableAction>> SHELL_COLOR_GETTER = new HashMap<>();

    boolean canApply(@Nonnull final ShulkerDyeableHolder shulker);
    void apply(@Nonnull final ShulkerDyeableHolder shulker);

    default void performShrink(@Nonnull final ItemStack stack, @Nonnull final EntityPlayer player, @Nonnull final EnumHand hand) {
        if(stack.getItem().isDamageable()) stack.damageItem(1, player);
        else ShulkerDying.shrinkAndGive(player, hand, stack, stack.getItem().getContainerItem(stack));
    }

    default void playFX(@Nonnull final World world, @Nonnull final AxisAlignedBB box) {
        if(ColoredShulkersCfg.inWorldDyingFX) {
            final double x = box.minX + (box.maxX - box.minX) / 2;
            final double y = box.minY + (box.maxY - box.minY) / 2;
            final double z = box.minZ + (box.maxZ - box.minZ) / 2;
            world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, x, y, z, 0, 0, 0);
            world.playSound(null, x, y, z, ColoredShulkers.SHULKER_DYED, SoundCategory.BLOCKS, 1, 1);
        }
    }

    @Nonnull
    static ShulkerDyeableAction color(@Nullable final EnumDyeColor color) {
        return new ShulkerDyeableAction() {
            @Override
            public boolean canApply(@Nonnull final ShulkerDyeableHolder shulker) {
                return shulker.isRainbow() || shulker.getColor() != color;
            }

            @Override
            public void apply(@Nonnull final ShulkerDyeableHolder shulker) {
                shulker.setColor(color);
            }
        };
    }

    @Nonnull
    static ShulkerDyeableAction rainbow() {
        return new ShulkerDyeableAction() {
            @Override
            public boolean canApply(@Nonnull final ShulkerDyeableHolder shulker) {
                return !shulker.isRainbow();
            }

            @Override
            public void apply(@Nonnull final ShulkerDyeableHolder shulker) {
                shulker.setRainbow();
            }
        };
    }

    @Nonnull
    static ShulkerDyeableAction washing() {
        return new ShulkerDyeableAction() {
            @Override
            public boolean canApply(@Nonnull final ShulkerDyeableHolder shulker) {
                return shulker.isRainbow() || shulker.getColor() != null;
            }

            @Override
            public void apply(@Nonnull final ShulkerDyeableHolder shulker) {
                shulker.setColor(null);
            }

            @Override
            public void performShrink(@Nonnull final ItemStack stack, @Nonnull final EntityPlayer player, @Nonnull final EnumHand hand) {
                if(stack.getItem() == Items.POTIONITEM) {
                    ShulkerDying.shrinkAndGive(player, hand, stack, new ItemStack(Items.GLASS_BOTTLE));
                    return;
                }

                @Nullable final IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack);
                if(handler == null) {
                    ShulkerDyeableAction.super.performShrink(stack, player, hand);
                    return;
                }

                @Nullable final FluidStack simulated = handler.drain(new FluidStack(FluidRegistry.WATER, Integer.MAX_VALUE), false);
                if(simulated != null && simulated.amount >= 250) {
                    if(handler.drain(new FluidStack(simulated, 250), true) == null) handler.drain(simulated, true);
                    ShulkerDying.shrinkAndGive(player, hand, stack, ItemHandlerHelper.copyStackWithSize(handler.getContainer(), 1));
                }
            }

            @Override
            public void playFX(@Nonnull final World world, @Nonnull final AxisAlignedBB box) {
                if(world.isRemote) for(int i = 0; i < 16; i++) {
                    world.spawnParticle(EnumParticleTypes.WATER_SPLASH, MathHelper.nextDouble(world.rand, box.minX, box.maxX), box.maxY, MathHelper.nextDouble(world.rand, box.minZ, box.maxZ), 0, 0, 0);
                }

                world.playSound(null, box.minX + (box.maxX - box.minX) / 2, box.minY + (box.maxY - box.minY) / 2, box.minZ + (box.maxZ - box.minZ) / 2, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 1, 1);
            }
        };
    }
}
