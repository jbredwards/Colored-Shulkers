package git.jbredwards.colored_shulkers.compat;

import appeng.api.AEApi;
import appeng.api.features.IGrinderRegistry;
import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import cofh.thermalexpansion.util.managers.machine.PulverizerManager;
import com.rwtema.extrautils2.api.machine.RecipeBuilder;
import com.rwtema.extrautils2.api.machine.XUMachineCrusher;
import de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI;
import epicsquid.roots.init.ModRecipes;
import git.jbredwards.colored_shulkers.ShulkerUtils;
import git.jbredwards.colored_shulkers.Tags;
import git.jbredwards.colored_shulkers.config.ColoredShulkersCfg;
import git.jbredwards.colored_shulkers.dying.ShulkerDying;
import ic2.api.recipe.Recipes;
import mekanism.api.MekanismAPI;
import nc.recipe.NCRecipes;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreIngredient;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public final class GrindingRecipes
{
    public static void registerAll() {
        if(ColoredShulkersCfg.shellToDyeRecipes > 0) ShulkerDying.SHELL_TO_DYE.forEach(GrindingRecipes::register);
    }

    private static void register(@Nonnull final EnumDyeColor color, @Nonnull final ItemStack dye) {
        if(AA) registerAA(color, ItemHandlerHelper.copyStackWithSize(dye, ColoredShulkersCfg.shellToDyeRecipes));
        if(AE) registerAE(color, ItemHandlerHelper.copyStackWithSize(dye, ColoredShulkersCfg.shellToDyeRecipes));
        if(IC) registerIC(color, ItemHandlerHelper.copyStackWithSize(dye, ColoredShulkersCfg.shellToDyeRecipes));
        if(IE) registerIE(color, ItemHandlerHelper.copyStackWithSize(dye, ColoredShulkersCfg.shellToDyeRecipes));
        if(ME) registerME(color, ItemHandlerHelper.copyStackWithSize(dye, ColoredShulkersCfg.shellToDyeRecipes));
        if(NC) registerNC(color, ItemHandlerHelper.copyStackWithSize(dye, ColoredShulkersCfg.shellToDyeRecipes));
        if(R3) registerR3(color, ItemHandlerHelper.copyStackWithSize(dye, ColoredShulkersCfg.shellToDyeRecipes));
        if(TE) registerTE(color, ItemHandlerHelper.copyStackWithSize(dye, ColoredShulkersCfg.shellToDyeRecipes));
        if(XU) registerXU(color, ItemHandlerHelper.copyStackWithSize(dye, ColoredShulkersCfg.shellToDyeRecipes));
    }

    private static final boolean AA = Loader.isModLoaded("actuallyadditions");
    private static void registerAA(@Nonnull final EnumDyeColor color, @Nonnull final ItemStack dye) {
        ActuallyAdditionsAPI.addCrusherRecipe(new OreIngredient(ShulkerDying.shellFromColor(color)), dye, ItemStack.EMPTY, 0);
    }

    private static final boolean AE = Loader.isModLoaded("appliedenergistics2");
    private static void registerAE(@Nonnull final EnumDyeColor color, @Nonnull final ItemStack dye) {
        @Nonnull final IGrinderRegistry registry = AEApi.instance().registries().grinder();
        registry.addRecipe(registry.builder().withInput(ShulkerUtils.shellFromColor(color, 1)).withOutput(dye).build());
    }

    private static final boolean IC = Loader.isModLoaded("ic2");
    private static void registerIC(@Nonnull final EnumDyeColor color, @Nonnull final ItemStack dye) {
        Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict(ShulkerDying.shellFromColor(color)), null, false, dye);
    }

    private static final boolean IE = Loader.isModLoaded("immersiveengineering");
    private static void registerIE(@Nonnull final EnumDyeColor color, @Nonnull final ItemStack dye) {
        CrusherRecipe.addRecipe(dye, ShulkerDying.shellFromColor(color), 3200);
    }

    private static final boolean ME = Loader.isModLoaded("mekanism");
    private static void registerME(@Nonnull final EnumDyeColor color, @Nonnull final ItemStack dye) {
        MekanismAPI.recipeHelper().addCrusherRecipe(ShulkerUtils.shellFromColor(color, 1), dye);
    }

    private static final boolean NC = Loader.isModLoaded("nuclearcraft");
    private static void registerNC(@Nonnull final EnumDyeColor color, @Nonnull final ItemStack dye) {
        NCRecipes.manufactory.addRecipe(ShulkerDying.shellFromColor(color), dye, 1, 1);
    }

    private static final boolean R3 = Loader.isModLoaded("roots");
    private static void registerR3(@Nonnull final EnumDyeColor color, @Nonnull final ItemStack dye) {
        ModRecipes.getMortarRecipeList(color.getName(), dye, new OreIngredient(ShulkerDying.shellFromColor(color))).values().forEach(recipe -> {
            recipe.setRegistryName(new ResourceLocation(Tags.MOD_ID, "shell_to_dye/" + recipe.getRegistryName().getPath()));
            ModRecipes.addMortarRecipe(recipe);
        });
    }

    private static final boolean TE = Loader.isModLoaded("thermalexpansion");
    private static void registerTE(@Nonnull final EnumDyeColor color, @Nonnull final ItemStack dye) {
        PulverizerManager.addRecipe(4000, ShulkerUtils.shellFromColor(color, 1), dye);
    }

    private static final boolean XU = Loader.isModLoaded("extrautils2");
    private static void registerXU(@Nonnull final EnumDyeColor color, @Nonnull final ItemStack dye) {
        XUMachineCrusher.INSTANCE.recipes_registry.addRecipe(RecipeBuilder.newbuilder(XUMachineCrusher.INSTANCE)
                .setItemInput(XUMachineCrusher.INPUT, ShulkerDying.shellFromColor(color), 1)
                .setItemOutput(XUMachineCrusher.OUTPUT, dye)
                .setEnergy(4000)
                .setProcessingTime(200)
                .build());
    }
}
