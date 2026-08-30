package git.jbredwards.colored_shulkers;

import git.jbredwards.colored_shulkers.compat.IC2Handler;
import git.jbredwards.colored_shulkers.compat.JERHandler;
import git.jbredwards.colored_shulkers.compat.ShulkerBaublesHandler;
import git.jbredwards.colored_shulkers.compat.ShulkerDropsTwoHandler;
import git.jbredwards.colored_shulkers.config.ColoredShulkersCfg;
import git.jbredwards.colored_shulkers.dying.ShulkerDyeableAction;
import git.jbredwards.colored_shulkers.registry.RainbowShulkerBox;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelShulker;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 *
 * @author jbred
 *
 */
@Mod.EventBusSubscriber
@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, updateJSON = Tags.UPDATE_JSON,
guiFactory = "git.jbredwards.colored_shulkers.config.ColoredShulkersGuiFactory")
public final class ColoredShulkers
{
    @Nonnull public static final ResourceLocation RAINBOW_SHELL_TABLE = LootTableList.register(new ResourceLocation(Tags.MOD_ID, "rainbow_shell_shatter"));
    @Nonnull public static final SimpleNetworkWrapper WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MOD_ID);

    public static Block PURPLE_SHULKER_BOX, RAINBOW_SHULKER_BOX;
    public static Item SHELL;
    public static SoundEvent COLORED_SHELL_USE, RAINBOW_SHELL_USE, SHULKER_DYED, SHULKER_ENCHANT, SHULKER_WASH;

    @Mod.EventHandler
    static void preInit(@Nonnull final FMLPreInitializationEvent event) {
        if(Loader.isModLoaded("baubleshulkerboxes")) ShulkerBaublesHandler.preInit();
        WRAPPER.registerMessage(RainbowShulkerBox.Sync.class, RainbowShulkerBox.Sync.class, 0, Side.CLIENT);
        dispenseBehavior(Blocks.PURPLE_SHULKER_BOX);
        dispenseBehavior(RAINBOW_SHULKER_BOX);
    }

    @Mod.EventHandler
    static void init(@Nonnull final FMLInitializationEvent event) {
        if(Loader.isModLoaded("ic2")) IC2Handler.init();
        if(Loader.isModLoaded("shulkerdropstwo")) ShulkerDropsTwoHandler.init();
        ShulkerDyeableAction.SHELL_COLOR_GETTER.put(Items.POTIONITEM, stack -> PotionUtils.getPotionFromItem(stack) == PotionTypes.WATER ? ShulkerDyeableAction.washing() : null);
        ShulkerDyeableAction.SHELL_COLOR_GETTER.put(Items.SHULKER_SHELL, stack -> ShulkerDyeableAction.color(null));
        ShulkerDyeableAction.SHELL_COLOR_GETTER.put(SHELL, stack -> {
            if(stack.getMetadata() == ShulkerUtils.RAINBOW_META) return ShulkerDyeableAction.rainbow();
            else return ShulkerUtils.colorFromShell(stack).map(ShulkerDyeableAction::color).orElse(null);
        });
    }

    @SideOnly(Side.CLIENT)
    @Mod.EventHandler
    static void initClient(@Nonnull final FMLInitializationEvent event) {
        Item.getItemFromBlock(PURPLE_SHULKER_BOX).setTileEntityItemStackRenderer(new RainbowShulkerBox.TEISR(false));
        Item.getItemFromBlock(RAINBOW_SHULKER_BOX).setTileEntityItemStackRenderer(new RainbowShulkerBox.TEISR(true));
        ClientRegistry.bindTileEntitySpecialRenderer(RainbowShulkerBox.Tile.class, new RainbowShulkerBox.TESR(new ModelShulker()));
        // Remove "disable" button in mod gui.
        @Nonnull final ModContainer mod = Objects.requireNonNull(Loader.instance().activeModContainer());
        ReflectionHelper.setPrivateValue(FMLModContainer.class, (FMLModContainer)mod, ModContainer.Disableable.NEVER, "disableability");
        // Allow this mod's description and credits to be translated.
        @Nonnull final ModMetadata metadata = mod.getMetadata();
        @Nonnull final String credits = metadata.credits, description = metadata.description;
        ((IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener((ISelectiveResourceReloadListener)(manager, predicate) -> {
            if(predicate.test(VanillaResourceType.LANGUAGES)) {
                metadata.credits = I18n.hasKey("mod." + Tags.MOD_ID + ".credits") ? I18n.format("mod." + Tags.MOD_ID + ".credits").replace("\\n", "\n") : credits;
                metadata.description = I18n.hasKey("mod." + Tags.MOD_ID + ".description") ? I18n.format("mod." + Tags.MOD_ID + ".description") : description;
            }
        });
    }

    @Mod.EventHandler
    static void postInit(@Nonnull final FMLPostInitializationEvent event) {
        if(Loader.isModLoaded("jeresources")) JERHandler.postInit();
        ColoredShulkersCfg.sync();
    }

    @SubscribeEvent
    static void syncConfig(@Nonnull final ConfigChangedEvent.OnConfigChangedEvent event) {
        if(Tags.MOD_ID.equals(event.getModID())) {
            ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
            ColoredShulkersCfg.sync();
        }
    }

    private static void dispenseBehavior(@Nonnull final Block shulkerBox) {
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Item.getItemFromBlock(shulkerBox), BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(Item.getItemFromBlock(Blocks.RED_SHULKER_BOX)));
    }
}
