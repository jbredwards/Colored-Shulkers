package git.jbredwards.colored_shulkers.registry;

import git.jbredwards.colored_shulkers.ColoredShulkers;
import git.jbredwards.colored_shulkers.ShulkerUtils;
import git.jbredwards.colored_shulkers.Tags;
import git.jbredwards.colored_shulkers.compat.BookshelfHandler;
import git.jbredwards.colored_shulkers.compat.GrindingRecipes;
import git.jbredwards.colored_shulkers.config.ColoredShulkersCfg;
import git.jbredwards.colored_shulkers.dying.ShulkerDying;
import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
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
    @SubscribeEvent
    static void registerRecipes(@Nonnull final RegistryEvent.Register<IRecipe> event) {
        // Colored shells to colorless.
        event.getRegistry().registerAll(new ColorlessBoxRecipe().setRegistryName("colorless_box"), new ShapelessOreRecipe(null, Items.SHULKER_SHELL,
                Ingredient.fromStacks(Arrays.stream(EnumDyeColor.values()).map(color -> ShulkerUtils.shellFromColor(color, 1)).toArray(ItemStack[]::new))).setRegistryName("colorless_shell"));

        // New shulker box recipes.
        event.getRegistry().register(new ShapedOreRecipe(null, ColoredShulkers.RAINBOW_SHULKER_BOX, "S", "C", "S", 'S', "shulkerShellRainbow", 'C', "chestWood").setRegistryName("box/rainbow"));
        for(@Nonnull final EnumDyeColor color : EnumDyeColor.values()) event.getRegistry().register(new ShapedOreRecipe(null, BlockShulkerBox.getBlockByColor(color),
                "S", "C", "S", 'S', ShulkerDying.shellFromColor(color), 'C', "chestWood").setRegistryName("box/" + color));

        // Update old shulker box recipe to accept colorless shulker shell oredict.
        @Nullable final IRecipe shulkerBoxRecipe = event.getRegistry().getValue(new ResourceLocation("purple_shulker_box"));
        if(shulkerBoxRecipe instanceof ShapedRecipes) ((ShapedRecipes)shulkerBoxRecipe).recipeItems.replaceAll(ingredient -> {
            if(ingredient.test(new ItemStack(Items.SHULKER_SHELL))) return new OreIngredient("shulkerShellColorless");
            else return ingredient;
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void registerShellDyeRecipes(@Nonnull final RegistryEvent.Register<IRecipe> event) {
        // Shells to dyes.
        extraShellsToDyes();
        if(ColoredShulkersCfg.shellToDyeRecipes > 0) {
            @Nullable final Item pestleAndMortar = Item.getByNameOrId("botania:pestleandmortar");
            @Nonnull final Ingredient bowl = pestleAndMortar != null ? Ingredient.fromItems(pestleAndMortar, Items.BOWL) : Ingredient.fromItem(Items.BOWL);
            ShulkerDying.SHELL_TO_DYE.forEach((color, dye) -> {
                if(dye == null || dye.isEmpty()) return; // Should never pass, but let's be safe.
                GrindingRecipes.register(color, dye);
                event.getRegistry().register(new ShapelessOreRecipe(null,
                ItemHandlerHelper.copyStackWithSize(dye, Math.min(64, ColoredShulkersCfg.shellToDyeRecipes)), bowl, ShulkerDying.shellFromColor(color)) {
                    @Nonnull
                    @Override
                    public NonNullList<ItemStack> getRemainingItems(@Nonnull final InventoryCrafting inv) {
                        @Nonnull final NonNullList<ItemStack> ret = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
                        for(int i = 0; i < ret.size(); i++) {
                            @Nonnull final ItemStack remaining = inv.getStackInSlot(i);
                            if(remaining.getItem() != Items.SHULKER_SHELL && remaining.getItem() != ColoredShulkers.SHELL) ret.set(i, ItemHandlerHelper.copyStackWithSize(remaining, 1));
                        }

                        return ret;
                    }
                }.setRegistryName("shell_to_dye/" + color));
            });
        }
        // Shell dying.
        if(ColoredShulkersCfg.shellDyingRecipes) event.getRegistry().registerAll(Arrays.stream(EnumDyeColor.values())
                .map(color -> {
                    @Nonnull final ItemStack result = ShulkerUtils.shellFromColor(color, 1);
                    @Nonnull final ItemStack[] shells = ShulkerUtils.SHULKER_BOXES.keySet().stream()
                            .filter(c -> !color.equals(c))
                            .map(c -> ShulkerUtils.shellFromColor(c, 1))
                            .toArray(ItemStack[]::new);
                    return new ShapelessOreRecipe(null, result,
                            "dye" + ShulkerDying.dyeFromColor(color), Ingredient.fromStacks(shells))
                            .setRegistryName("shell_from_dye/" + color);
                })
                .toArray(IRecipe[]::new));
    }

    @SubscribeEvent
    static void registerBlocks(@Nonnull final RegistryEvent.Register<Block> event) {
        GameRegistry.registerTileEntity(RainbowShulkerBox.Tile.class, new ResourceLocation(Tags.MOD_ID, "tile"));
        event.getRegistry().register(ColoredShulkers.PURPLE_SHULKER_BOX.setCreativeTab(RainbowShulkerBox.TAB)
                .setHardness(2).setTranslationKey(Blocks.PURPLE_SHULKER_BOX.getTranslationKey().substring(5)).setRegistryName("purple_shulker_box"));
        event.getRegistry().register(ColoredShulkers.RAINBOW_SHULKER_BOX = RainbowShulkerBox.Tile.asBlock().setCreativeTab(RainbowShulkerBox.TAB)
                .setHardness(2).setTranslationKey(Tags.MOD_ID + ".rainbow_shulker_box").setRegistryName("rainbow_shulker_box"));
        Blocks.PURPLE_SHULKER_BOX.setTranslationKey(Tags.MOD_ID + ".colorless_shulker_box");
    }

    @SubscribeEvent
    static void registerItems(@Nonnull final RegistryEvent.Register<Item> event) {
        event.getRegistry().register(ColoredShulkers.SHELL = new ItemColoredShell().setCreativeTab(RainbowShulkerBox.TAB).setRegistryName("shell").setTranslationKey("shulkerShell"));
        event.getRegistry().register(RainbowShulkerBox.Tile.asItem().setRegistryName("rainbow_shulker_box"));
        event.getRegistry().register(new ItemShulkerBox(ColoredShulkers.PURPLE_SHULKER_BOX).setRegistryName("purple_shulker_box"));

        OreDictionary.registerOre("shulkerShell", Items.SHULKER_SHELL);
        OreDictionary.registerOre("shulkerShellColorless", Items.SHULKER_SHELL);
        OreDictionary.registerOre("shulkerShell", new ItemStack(ColoredShulkers.SHELL, 1, OreDictionary.WILDCARD_VALUE));

        for(@Nonnull final EnumDyeColor color : EnumDyeColor.values()) OreDictionary.registerOre(ShulkerDying.shellFromColor(color), ShulkerUtils.shellFromColor(color, 1));
        OreDictionary.registerOre("shulkerShellRainbow", new ItemStack(ColoredShulkers.SHELL, 1, ShulkerUtils.RAINBOW_META));
        if(Loader.isModLoaded("bookshelf")) BookshelfHandler.registerOres();
    }

    @SubscribeEvent
    static void registerSounds(@Nonnull final RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(ColoredShulkers.COLORED_SHELL_USE = new SoundEvent(new ResourceLocation(Tags.MOD_ID, "item.colored_shell.use")).setRegistryName("item.colored_shell.use"),
                                        ColoredShulkers.RAINBOW_SHELL_USE = new SoundEvent(new ResourceLocation(Tags.MOD_ID, "item.rainbow_shell.use")).setRegistryName("item.rainbow_shell.use"),
                                        ColoredShulkers.SHULKER_DYED = new SoundEvent(new ResourceLocation(Tags.MOD_ID, "entity.shulker.dyed")).setRegistryName("entity.shulker.dyed"),
                                        ColoredShulkers.SHULKER_ENCHANT = new SoundEvent(new ResourceLocation(Tags.MOD_ID, "entity.shulker.enchant")).setRegistryName("entity.shulker.enchant"),
                                        ColoredShulkers.SHULKER_WASH = new SoundEvent(new ResourceLocation(Tags.MOD_ID, "entity.shulker.wash")).setRegistryName("entity.shulker.wash"));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    static void registerBakedModels(@Nonnull final ModelBakeEvent event) {
        // TODO: Is there a way through blockstate json to specify "builtin/entity" using forge_marker and provide it a particle texture?
        @Nonnull final ModelResourceLocation rainbow = new ModelResourceLocation(ColoredShulkers.RAINBOW_SHULKER_BOX.delegate.name(), null);
        @Nonnull final IBakedModel soulSand = event.getModelManager().getModel(new ModelResourceLocation("soul_sand"));
        event.getModelRegistry().putObject(rainbow, new BuiltInModel(soulSand.getItemCameraTransforms(), ItemOverrideList.NONE) {
            @Nonnull
            @Override
            public TextureAtlasSprite getParticleTexture() {
                return soulSand.getParticleTexture();
            }
        });
        @Nonnull final ModelResourceLocation purple = new ModelResourceLocation(ColoredShulkers.PURPLE_SHULKER_BOX.delegate.name(), null);
        @Nonnull final TextureAtlasSprite purpleParticle = event.getModelManager().getBlockModelShapes().getTexture(Blocks.WHITE_SHULKER_BOX.getDefaultState());
        event.getModelRegistry().putObject(purple, new BuiltInModel(soulSand.getItemCameraTransforms(), ItemOverrideList.NONE) {
            @Nonnull
            @Override
            public TextureAtlasSprite getParticleTexture() {
                return purpleParticle;
            }
        });
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    static void registerModels(@Nonnull final ModelRegistryEvent event) {
        ModelLoader.setCustomStateMapper(ColoredShulkers.PURPLE_SHULKER_BOX, new StateMap.Builder().ignore(BlockShulkerBox.FACING).build());
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ColoredShulkers.PURPLE_SHULKER_BOX), 0, new ModelResourceLocation(
                ColoredShulkers.PURPLE_SHULKER_BOX.delegate.name(), null));
        ModelLoader.setCustomStateMapper(ColoredShulkers.RAINBOW_SHULKER_BOX, new StateMap.Builder().ignore(BlockShulkerBox.FACING).build());
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ColoredShulkers.RAINBOW_SHULKER_BOX), 0, new ModelResourceLocation(
                ColoredShulkers.RAINBOW_SHULKER_BOX.delegate.name(), null));
        for(int meta = 0; meta <= ShulkerUtils.RAINBOW_META; meta++) ModelLoader.setCustomModelResourceLocation(ColoredShulkers.SHELL, meta, new ModelResourceLocation(
                ColoredShulkers.SHELL.delegate.name(), ItemColoredShell.byShellDamage(meta).map(EnumDyeColor::getName).orElse("rainbow")));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    static void registerColors(@Nonnull final ColorHandlerEvent.Item event) {
        event.getItemColors().registerItemColorHandler((stack, tintIndex) -> tintIndex == 0 && stack.getMetadata() == ShulkerUtils.RAINBOW_META ? RainbowShulkerBox.getRGB() : -1, ColoredShulkers.SHELL);
        event.getBlockColors().registerBlockColorHandler((state, world, pos, tintIndex) -> tintIndex == 0 ? EnumDyeColor.PURPLE.getColorValue() : -1, ColoredShulkers.PURPLE_SHULKER_BOX);
    }

    private static void extraShellsToDyes() {
        // Shells to dyes (BOP).
        if(Loader.isModLoaded("biomesoplenty")) {
            Optional.ofNullable(Item.getByNameOrId("biomesoplenty:black_dye")).ifPresent(item -> ShulkerDying.SHELL_TO_DYE.putIfAbsent(EnumDyeColor.BLACK, new ItemStack(item, 8)));
            Optional.ofNullable(Item.getByNameOrId("biomesoplenty:blue_dye")).ifPresent(item -> ShulkerDying.SHELL_TO_DYE.putIfAbsent(EnumDyeColor.BLUE, new ItemStack(item, 8)));
            Optional.ofNullable(Item.getByNameOrId("biomesoplenty:brown_dye")).ifPresent(item -> ShulkerDying.SHELL_TO_DYE.putIfAbsent(EnumDyeColor.BROWN, new ItemStack(item, 8)));
            Optional.ofNullable(Item.getByNameOrId("biomesoplenty:white_dye")).ifPresent(item -> ShulkerDying.SHELL_TO_DYE.putIfAbsent(EnumDyeColor.WHITE, new ItemStack(item, 8)));
        }
        else {
            // Shells to dyes (FutureMC).
            Optional.ofNullable(Item.getByNameOrId("futuremc:dye")).ifPresent(item -> {
                ShulkerDying.SHELL_TO_DYE.putIfAbsent(EnumDyeColor.BLACK, new ItemStack(item, 1, 3));
                ShulkerDying.SHELL_TO_DYE.putIfAbsent(EnumDyeColor.BLUE, new ItemStack(item, 1, 1));
                ShulkerDying.SHELL_TO_DYE.putIfAbsent(EnumDyeColor.BROWN, new ItemStack(item, 1, 2));
                ShulkerDying.SHELL_TO_DYE.putIfAbsent(EnumDyeColor.WHITE, new ItemStack(item, 1, 0));
            });
            // Shells to dyes (Quark).
            Optional.ofNullable(Item.getByNameOrId("quark:root_dye")).ifPresent(item -> {
                ShulkerDying.SHELL_TO_DYE.putIfAbsent(EnumDyeColor.BLACK, new ItemStack(item, 1, 1));
                ShulkerDying.SHELL_TO_DYE.putIfAbsent(EnumDyeColor.BLUE, new ItemStack(item, 1, 0));
                ShulkerDying.SHELL_TO_DYE.putIfAbsent(EnumDyeColor.WHITE, new ItemStack(item, 1, 2));
            });
            // Shells to dyes (AA).
            Optional.ofNullable(Item.getByNameOrId("actuallyadditions:item_misc")).ifPresent(item ->
                ShulkerDying.SHELL_TO_DYE.putIfAbsent(EnumDyeColor.BLACK, new ItemStack(item, 1, 17))
            );
            // Shells to dyes (Natura).
            Optional.ofNullable(Item.getByNameOrId("natura:materials")).ifPresent(item ->
                ShulkerDying.SHELL_TO_DYE.putIfAbsent(EnumDyeColor.BLUE, new ItemStack(item, 1, 8))
            );
            // Shells to dyes (XU).
            Optional.ofNullable(Item.getByNameOrId("extrautils2:ingredients")).ifPresent(item ->
                ShulkerDying.SHELL_TO_DYE.putIfAbsent(EnumDyeColor.BLUE, new ItemStack(item, 1, 14))
            );
        }
    }
}
