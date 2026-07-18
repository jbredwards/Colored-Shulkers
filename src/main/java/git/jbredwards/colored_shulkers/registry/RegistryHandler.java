package git.jbredwards.colored_shulkers.registry;

import git.jbredwards.colored_shulkers.ColoredShulkers;
import git.jbredwards.colored_shulkers.ShulkerEvents;
import git.jbredwards.colored_shulkers.ShulkerUtils;
import git.jbredwards.colored_shulkers.Tags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;

/**
 *
 * @author jbred
 *
 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
final class RegistryHandler
{
    @Nonnull
    static final String[] DYES = {
            "Black", "Red", "Green", "Brown",
            "Blue", "Purple", "Cyan", "LightGray",
            "Gray", "Pink", "Lime", "Yellow",
            "LightBlue", "Magenta", "Orange", "White"
    };

    @Nonnull
    static String dyeFromColor(@Nonnull final EnumDyeColor color) {
        return DYES[color.getDyeDamage()];
    }

    @SubscribeEvent
    static void registerRecipes(@Nonnull final RegistryEvent.Register<IRecipe> event) {
        // New shulker box recipes.
        event.getRegistry().register(new ShapedOreRecipe(null, ColoredShulkers.RAINBOW_SHULKER_BOX,
                "S", "C", "S", 'S', "shellShulkerRainbow", 'C', "chestWood").setRegistryName("box/rainbow"));
        for(@Nonnull final EnumDyeColor color : EnumDyeColor.values()) {
            if(color != EnumDyeColor.PURPLE) event.getRegistry().register(new ShapedOreRecipe(null,
                    BlockShulkerBox.getBlockByColor(color), "S", "C", "S",
                    'S', "shellShulker" + dyeFromColor(color), 'C', "chestWood")
                    .setRegistryName("box/" + color));
        }

        // Update old shulker box recipe to accept purple shulker shell oredict.
        @Nullable final IRecipe shulkerBoxRecipe = event.getRegistry().getValue(new ResourceLocation("purple_shulker_box"));
        if(shulkerBoxRecipe instanceof ShapedRecipes) ((ShapedRecipes)shulkerBoxRecipe).recipeItems.replaceAll(ingredient -> {
            if(ingredient.test(new ItemStack(Items.SHULKER_SHELL))) return new OreIngredient("shellShulkerPurple");
            else return ingredient;
        });

        // Shells to dyes (BOP).
        if(Loader.isModLoaded("biomesoplenty")) {
            Optional.ofNullable(Item.getByNameOrId("biomesoplenty:black_dye")).ifPresent(item -> ShulkerEvents.SHELL_TO_DYE.putIfAbsent(EnumDyeColor.BLACK, new ItemStack(item, 8)));
            Optional.ofNullable(Item.getByNameOrId("biomesoplenty:blue_dye")).ifPresent(item -> ShulkerEvents.SHELL_TO_DYE.putIfAbsent(EnumDyeColor.BLUE, new ItemStack(item, 8)));
            Optional.ofNullable(Item.getByNameOrId("biomesoplenty:brown_dye")).ifPresent(item -> ShulkerEvents.SHELL_TO_DYE.putIfAbsent(EnumDyeColor.BROWN, new ItemStack(item, 8)));
            Optional.ofNullable(Item.getByNameOrId("biomesoplenty:white_dye")).ifPresent(item -> ShulkerEvents.SHELL_TO_DYE.putIfAbsent(EnumDyeColor.WHITE, new ItemStack(item, 8)));
        }

        // Shells to dyes (FutureMC).
        else Optional.ofNullable(Item.getByNameOrId("futuremc:dye")).ifPresent(item -> {
            ShulkerEvents.SHELL_TO_DYE.putIfAbsent(EnumDyeColor.BLACK, new ItemStack(item, 8, 3));
            ShulkerEvents.SHELL_TO_DYE.putIfAbsent(EnumDyeColor.BLUE, new ItemStack(item, 8, 1));
            ShulkerEvents.SHELL_TO_DYE.putIfAbsent(EnumDyeColor.BROWN, new ItemStack(item, 8, 2));
            ShulkerEvents.SHELL_TO_DYE.putIfAbsent(EnumDyeColor.WHITE, new ItemStack(item, 8));
        });

        // Shells to dyes.
        ShulkerEvents.SHELL_TO_DYE.forEach((color, dye) ->
                event.getRegistry().register(new ShapelessOreRecipe(null, dye,
                        ShulkerUtils.shellFromColor(color, 1))
                        .setRegistryName("shell_to_dye/" + color)));

        // Shell dying.
        event.getRegistry().registerAll(Arrays.stream(EnumDyeColor.values())
                .map(color -> {
                    @Nonnull final ItemStack result = ShulkerUtils.shellFromColor(color, 1);
                    @Nonnull final ItemStack[] shells = Arrays.stream(EnumDyeColor.values())
                            .filter(c -> !c.equals(color))
                            .map(c -> ShulkerUtils.shellFromColor(c, 1))
                            .toArray(ItemStack[]::new);
                    return new ShapelessOreRecipe(null, result,
                            "dye" + dyeFromColor(color), Ingredient.fromStacks(shells))
                            .setRegistryName("shell_from_dye/" + color);
                })
                .toArray(IRecipe[]::new));
    }

    @SubscribeEvent
    static void registerBlocks(@Nonnull final RegistryEvent.Register<Block> event) {
        GameRegistry.registerTileEntity(RainbowShulkerBox.Tile.class, new ResourceLocation(Tags.MOD_ID, "tile"));
        event.getRegistry().register(ColoredShulkers.RAINBOW_SHULKER_BOX = RainbowShulkerBox.Tile.asBlock()
                .setHardness(2).setTranslationKey(Tags.MOD_ID + ".rainbow_shulker_box").setRegistryName("rainbow_shulker_box"));
    }

    @SubscribeEvent
    static void registerItems(@Nonnull final RegistryEvent.Register<Item> event) {
        event.getRegistry().register(ColoredShulkers.SHELL = new ItemColoredShell().setRegistryName("shell").setTranslationKey("shulkerShell"));
        event.getRegistry().register(RainbowShulkerBox.Tile.asItem().setRegistryName("rainbow_shulker_box"));

        OreDictionary.registerOre("shellShulker", Items.SHULKER_SHELL);
        OreDictionary.registerOre("shellShulker", new ItemStack(ColoredShulkers.SHELL, 1, OreDictionary.WILDCARD_VALUE));

        for(@Nonnull final EnumDyeColor color : EnumDyeColor.values()) OreDictionary.registerOre("shellShulker" + dyeFromColor(color), ShulkerUtils.shellFromColor(color, 1));
        OreDictionary.registerOre("shellShulkerRainbow", new ItemStack(ColoredShulkers.SHELL, 1, 15));
    }

    @SubscribeEvent
    static void registerSounds(@Nonnull final RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(ColoredShulkers.SHULKER_DYED = new SoundEvent(new ResourceLocation(Tags.MOD_ID, "entity.shulker.dyed")).setRegistryName("entity.shulker.dyed"),
                                        ColoredShulkers.SHULKER_ENCHANT = new SoundEvent(new ResourceLocation(Tags.MOD_ID, "entity.shulker.enchant")).setRegistryName("entity.shulker.enchant"));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    static void registerBakedModels(@Nonnull final ModelBakeEvent event) {
        @Nonnull final ModelResourceLocation location = new ModelResourceLocation(ColoredShulkers.RAINBOW_SHULKER_BOX.delegate.name(), "normal");
        @Nonnull final IBakedModel soulSand = event.getModelManager().getModel(new ModelResourceLocation("soul_sand"));
        event.getModelRegistry().putObject(location, new BuiltInModel(soulSand.getItemCameraTransforms(), ItemOverrideList.NONE) {
            @Nonnull
            @Override
            public TextureAtlasSprite getParticleTexture() {
                return soulSand.getParticleTexture();
            }
        });
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    static void registerModels(@Nonnull final ModelRegistryEvent event) {
        ModelLoader.setCustomStateMapper(ColoredShulkers.RAINBOW_SHULKER_BOX, new StateMap.Builder().ignore(BlockShulkerBox.FACING).build());
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ColoredShulkers.RAINBOW_SHULKER_BOX), 0, new ModelResourceLocation(
                ColoredShulkers.RAINBOW_SHULKER_BOX.delegate.name(), "normal"));
        for(int meta = 0; meta < 16; meta++) ModelLoader.setCustomModelResourceLocation(ColoredShulkers.SHELL, meta, new ModelResourceLocation(
                ColoredShulkers.SHELL.delegate.name(), ItemColoredShell.byShellDamage(meta).map(EnumDyeColor::getName).orElse("rainbow")));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    static void registerColors(@Nonnull final ColorHandlerEvent.Item event) {
        event.getItemColors().registerItemColorHandler((stack, tintIndex) -> tintIndex == 0 && stack.getMetadata() == 15 ? RainbowShulkerBox.getRGB() : -1, ColoredShulkers.SHELL);
    }
}
