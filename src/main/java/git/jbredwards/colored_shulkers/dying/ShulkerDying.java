package git.jbredwards.colored_shulkers.dying;

import com.google.common.collect.ImmutableMap;
import git.jbredwards.colored_shulkers.config.ColoredShulkersCfg;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.DyeUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 *
 * @author jbred
 *
 */
public final class ShulkerDying
{
    /**
     * The list of dye items that can be made from colored shulker shells.
     * <p>Public so we can support generic modded dye items, like the BOP dyes.</p>
     */
    @Nonnull
    public static final Map<EnumDyeColor, ItemStack> SHELL_TO_DYE = new HashMap<>(ImmutableMap.<EnumDyeColor, ItemStack>builder()
            .put(EnumDyeColor.ORANGE, new ItemStack(Items.DYE, 1, EnumDyeColor.ORANGE.getDyeDamage()))
            .put(EnumDyeColor.MAGENTA, new ItemStack(Items.DYE, 1, EnumDyeColor.MAGENTA.getDyeDamage()))
            .put(EnumDyeColor.LIGHT_BLUE, new ItemStack(Items.DYE, 1, EnumDyeColor.LIGHT_BLUE.getDyeDamage()))
            .put(EnumDyeColor.YELLOW, new ItemStack(Items.DYE, 1, EnumDyeColor.YELLOW.getDyeDamage()))
            .put(EnumDyeColor.LIME, new ItemStack(Items.DYE, 1, EnumDyeColor.LIME.getDyeDamage()))
            .put(EnumDyeColor.PINK, new ItemStack(Items.DYE, 1, EnumDyeColor.PINK.getDyeDamage()))
            .put(EnumDyeColor.GRAY, new ItemStack(Items.DYE, 1, EnumDyeColor.GRAY.getDyeDamage()))
            .put(EnumDyeColor.SILVER, new ItemStack(Items.DYE, 1, EnumDyeColor.SILVER.getDyeDamage()))
            .put(EnumDyeColor.CYAN, new ItemStack(Items.DYE, 1, EnumDyeColor.CYAN.getDyeDamage()))
            .put(EnumDyeColor.PURPLE, new ItemStack(Items.DYE, 1, EnumDyeColor.PURPLE.getDyeDamage()))
            .put(EnumDyeColor.GREEN, new ItemStack(Items.DYE, 1, EnumDyeColor.GREEN.getDyeDamage()))
            .put(EnumDyeColor.RED, new ItemStack(Items.DYE, 1, EnumDyeColor.RED.getDyeDamage()))
            .build());

    /**
     * Attempts to dye the provided shulker holder with the player's current held item.
     */
    public static boolean attemptDye(@Nonnull final ShulkerDyeableHolder holder, @Nonnull final EntityPlayer player, @Nonnull final EnumHand hand, final boolean simulate) {
        if(!ColoredShulkersCfg.inWorldDying) return false;
        @Nonnull final ItemStack held = player.getHeldItem(hand);
        if(held.isEmpty()) return false;

        @Nullable final Function<ItemStack, ShulkerDyeableAction> mappedColor = ShulkerDyeableAction.SHELL_COLOR_GETTER.get(held.getItem());
        @Nullable ShulkerDyeableAction shellColor = mappedColor != null ? mappedColor.apply(held) : null;

        // Support all dye items (if enabled).
        if(mappedColor == null && ColoredShulkersCfg.inWorldDyingWithDyes) {
            @Nonnull final Optional<EnumDyeColor> dyeColor = DyeUtils.colorFromStack(held);
            if(dyeColor.isPresent()) shellColor = ShulkerDyeableAction.color(dyeColor.get());
        }

        // Support all water containers.
        if(mappedColor == null) {
            @Nullable final IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(held);
            if(fluidHandler != null) {
                @Nullable final FluidStack simulated = fluidHandler.drain(new FluidStack(FluidRegistry.WATER, Integer.MAX_VALUE), false);
                if(simulated != null && simulated.amount >= 250) shellColor = ShulkerDyeableAction.washing();
            }
        }

        // Perform shulker coloring.
        if(shellColor != null && shellColor.canApply(holder)) {
            if(!simulate) {
                if(!player.world.isRemote && !player.isCreative()) shellColor.performShrink(held, player, hand);
                shellColor.apply(holder);

                player.swingArm(hand);
                shellColor.playFX(player.world, holder.getBoundingBox());
            }

            return true;
        }

        return false;
    }

    @Nonnull
    public static String dyeFromColor(@Nonnull final EnumDyeColor color) {
        return DYES[color.getDyeDamage()];
    }

    @Nonnull
    public static String shellFromColor(@Nonnull final EnumDyeColor color) {
        return "shulkerShell" + dyeFromColor(color);
    }

    static void shrinkAndGive(@Nonnull final EntityPlayer player, @Nonnull final EnumHand hand, @Nonnull final ItemStack held, @Nonnull final ItemStack toGive) {
        held.shrink(1);
        if(held.isEmpty()) player.setHeldItem(hand, toGive);
        else ItemHandlerHelper.giveItemToPlayer(player, toGive);
    }

    @Nonnull
    private static final String[] DYES = { "Black", "Red", "Green", "Brown", "Blue", "Purple", "Cyan", "LightGray", "Gray", "Pink", "Lime", "Yellow", "LightBlue", "Magenta", "Orange", "White" };
}
