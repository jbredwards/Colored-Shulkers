package git.jbredwards.colored_shulkers.registry;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;

/**
 *
 * @see net.minecraft.item.crafting.ShulkerBoxRecipes
 * @author jbred
 *
 */
public class ColorlessBoxRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe
{
    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public boolean canFit(final int width, final int height) {
        return width > 0 && height > 0;
    }

    @Override
    public boolean matches(@Nonnull final InventoryCrafting inv, @Nonnull final World worldIn) {
        int found = 0;
        for(int i = 0; i < inv.getSizeInventory(); i++) {
            @Nonnull final ItemStack stack = inv.getStackInSlot(i);
            if(!stack.isEmpty() && (!(stack.getItem() instanceof ItemShulkerBox)
            || Block.getBlockFromItem(stack.getItem()) == Blocks.PURPLE_SHULKER_BOX
            || ++found != 1)) return false;
        }

        return found == 1;
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(@Nonnull final InventoryCrafting inv) {
        for(int i = 0; i < inv.getSizeInventory(); i++) {
            @Nonnull final ItemStack stack = inv.getStackInSlot(i);
            if(!stack.isEmpty() && stack.getItem() instanceof ItemShulkerBox) {
                @Nonnull final ItemStack colorless = new ItemStack(Blocks.PURPLE_SHULKER_BOX);
                if(stack.hasTagCompound()) colorless.setTagCompound(stack.getTagCompound().copy());
                return colorless;
            }
        }

        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }
}
