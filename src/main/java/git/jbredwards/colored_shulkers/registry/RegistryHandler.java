package git.jbredwards.colored_shulkers.registry;

import git.jbredwards.colored_shulkers.ColoredShulkers;
import git.jbredwards.colored_shulkers.ShulkerUtils;
import git.jbredwards.colored_shulkers.Tags;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 *
 * @author jbred
 *
 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
final class RegistryHandler
{
    @SubscribeEvent
    static void registerItems(@Nonnull final RegistryEvent.Register<Item> event) {
        event.getRegistry().register(ColoredShulkers.SHELL = new ItemColoredShell().setRegistryName("shell").setTranslationKey("shulkerShell"));

        OreDictionary.registerOre("shellShulker", Items.SHULKER_SHELL);
        OreDictionary.registerOre("shellShulkerPurple", Items.SHULKER_SHELL);
        OreDictionary.registerOre("shellShulker", new ItemStack(ColoredShulkers.SHELL, 1, OreDictionary.WILDCARD_VALUE));

        @Nonnull final String[] dyes = {"Black", "Red", "Green", "Brown", "Blue", "Cyan", "LightGray", "Gray", "Pink", "Lime", "Yellow", "LightBlue", "Magenta", "Orange", "White"};
        for(int meta = 0; meta < 15; meta++) OreDictionary.registerOre("shellShulker" + dyes[meta], new ItemStack(ColoredShulkers.SHELL, 1, meta));
    }

    @SubscribeEvent
    static void registerRecipes(@Nonnull final RegistryEvent.Register<IRecipe> event) {
        @Nonnull final String[] dyes = {"Black", "Red", "Green", "Brown", "Blue", "Cyan", "LightGray", "Gray", "Pink", "Lime", "Yellow", "LightBlue", "Magenta", "Orange", "White"};
        @Nonnull final Block[] boxes = {
                Blocks.BLACK_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX,
                Blocks.BLUE_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.SILVER_SHULKER_BOX,
                Blocks.GRAY_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX,
                Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.WHITE_SHULKER_BOX
        };

        // New shulker box recipes.
        for(int meta = 0; meta < 15; meta++) {
            event.getRegistry().register(new ShapedOreRecipe(null, boxes[meta], "S", "C", "S", 'S', "shellShulker" + dyes[meta], 'C', "chestWood").setRegistryName(dyes[meta]));
        }

        // Update old shulker box recipe to accept purple shulker shell oredict.
        @Nullable final IRecipe shulkerBoxRecipe = event.getRegistry().getValue(new ResourceLocation("purple_shulker_box"));
        if(shulkerBoxRecipe instanceof ShapedRecipes) ((ShapedRecipes)shulkerBoxRecipe).recipeItems.replaceAll(ingredient -> {
            if(ingredient.test(new ItemStack(Items.SHULKER_SHELL))) return new OreIngredient("shellShulkerPurple");
            else return ingredient;
        });
    }

    @SubscribeEvent
    static void registerSounds(@Nonnull final RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(ColoredShulkers.SHULKER_DYED = new SoundEvent(new ResourceLocation(Tags.MOD_ID, "entity.shulker.dyed")).setRegistryName("entity.shulker.dyed"),
                                        ColoredShulkers.SHULKER_ENCHANT = new SoundEvent(new ResourceLocation(Tags.MOD_ID, "entity.shulker.enchant")).setRegistryName("entity.shulker.enchant"));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    static void registerModels(@Nonnull final ModelRegistryEvent event) {
        ModelLoader.registerItemVariants(ColoredShulkers.SHELL, IntStream.range(0, 16).mapToObj(meta -> new ModelResourceLocation(Objects.requireNonNull(ColoredShulkers.SHELL.getRegistryName()), ShulkerUtils.byShellDamage(meta).map(EnumDyeColor::getName).orElse("invalid"))).toArray(ModelResourceLocation[]::new));
        ModelLoader.setCustomMeshDefinition(ColoredShulkers.SHELL, stack -> new ModelResourceLocation(Objects.requireNonNull(ColoredShulkers.SHELL.getRegistryName()), ShulkerUtils.byShellDamage(stack.getMetadata()).map(EnumDyeColor::getName).orElse("invalid")));
    }
}
