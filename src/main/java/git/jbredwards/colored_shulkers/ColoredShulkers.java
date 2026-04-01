package git.jbredwards.colored_shulkers;

import git.jbredwards.colored_shulkers.compat.JERHandler;
import git.jbredwards.colored_shulkers.compat.QuarkHandler;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.WeightedRandom;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
@Mod.EventBusSubscriber
@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public final class ColoredShulkers
{
    public static final boolean JER = Loader.isModLoaded("jeresources");
    public static final boolean QUARK = Loader.isModLoaded("quark");

    public static Item SHELL;
    public static SoundEvent SHULKER_DYED, SHULKER_ENCHANT;

    @Mod.EventHandler
    static void init(@Nonnull final FMLInitializationEvent event) {
        // if(QUARK) QuarkHandler.init();
        ShulkerEvents.SHELL_COLOR_GETTER.put(Items.SHULKER_SHELL, stack -> EnumDyeColor.PURPLE);
        ShulkerEvents.SHELL_COLOR_GETTER.put(SHELL, stack -> ShulkerUtils.byShellDamage(stack.getMetadata()).orElse(null));
    }

    @Mod.EventHandler
    static void postInit(@Nonnull final FMLPostInitializationEvent event) {
        if(JER) JERHandler.postInit();
        Cfg.sync();
    }

    @SubscribeEvent
    static void syncConfig(@Nonnull final ConfigChangedEvent.OnConfigChangedEvent event) {
        if(Tags.MOD_ID.equals(event.getModID())) {
            ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
            Cfg.sync();
        }
    }

    @Config(modid = Tags.MOD_ID)
    public static final class Cfg
    {
        @Config.LangKey("cfg." + Tags.MOD_ID + ".enableDrops")
        public static boolean enableDrops = true;

        @Config.LangKey("cfg." + Tags.MOD_ID + ".enableEndCity")
        public static boolean enableEndCity = true;

        @Config.LangKey("cfg." + Tags.MOD_ID + ".enableSpawner")
        public static boolean enableSpawner = false;

        @Config.LangKey("cfg." + Tags.MOD_ID + ".enableWorld")
        public static boolean enableWorld = false;

        @Config.LangKey("cfg." + Tags.MOD_ID + ".shulkerWeightRed")
        public static int shulkerWeightRed = 1;

        @Config.LangKey("cfg." + Tags.MOD_ID + ".shulkerWeightOrange")
        public static int shulkerWeightOrange = 1;

        @Config.LangKey("cfg." + Tags.MOD_ID + ".shulkerWeightYellow")
        public static int shulkerWeightYellow = 1;

        @Config.LangKey("cfg." + Tags.MOD_ID + ".shulkerWeightLime")
        public static int shulkerWeightLime = 1;

        @Config.LangKey("cfg." + Tags.MOD_ID + ".shulkerWeightGreen")
        public static int shulkerWeightGreen = 1;

        @Config.LangKey("cfg." + Tags.MOD_ID + ".shulkerWeightCyan")
        public static int shulkerWeightCyan = 1;

        @Config.LangKey("cfg." + Tags.MOD_ID + ".shulkerWeightLightBlue")
        public static int shulkerWeightLightBlue = 1;

        @Config.LangKey("cfg." + Tags.MOD_ID + ".shulkerWeightBlue")
        public static int shulkerWeightBlue = 1;

        @Config.LangKey("cfg." + Tags.MOD_ID + ".shulkerWeightPurple")
        public static int shulkerWeightPurple = 1;

        @Config.LangKey("cfg." + Tags.MOD_ID + ".shulkerWeightMagenta")
        public static int shulkerWeightMagenta = 1;

        @Config.LangKey("cfg." + Tags.MOD_ID + ".shulkerWeightPink")
        public static int shulkerWeightPink = 1;

        @Config.LangKey("cfg." + Tags.MOD_ID + ".shulkerWeightWhite")
        public static int shulkerWeightWhite = 1;

        @Config.LangKey("cfg." + Tags.MOD_ID + ".shulkerWeightLightGray")
        public static int shulkerWeightLightGray = 1;

        @Config.LangKey("cfg." + Tags.MOD_ID + ".shulkerWeightGray")
        public static int shulkerWeightGray = 1;

        @Config.LangKey("cfg." + Tags.MOD_ID + ".shulkerWeightBlack")
        public static int shulkerWeightBlack = 1;

        @Config.LangKey("cfg." + Tags.MOD_ID + ".shulkerWeightBrown")
        public static int shulkerWeightBrown = 1;

        @Nonnull
        @Config.Ignore
        public static final List<ColorEntry> WEIGHTS = new ArrayList<>();
        public static final class ColorEntry extends WeightedRandom.Item
        {
            @Nonnull
            public final EnumDyeColor color;
            public ColorEntry(@Nonnull final EnumDyeColor colorIn, final int itemWeightIn) {
                super(itemWeightIn);
                color = colorIn;
            }
        }

        static void sync() {
            WEIGHTS.clear();
            if(shulkerWeightRed > 0) WEIGHTS.add(new ColorEntry(EnumDyeColor.RED, shulkerWeightRed));
            if(shulkerWeightOrange > 0) WEIGHTS.add(new ColorEntry(EnumDyeColor.ORANGE, shulkerWeightOrange));
            if(shulkerWeightYellow > 0) WEIGHTS.add(new ColorEntry(EnumDyeColor.YELLOW, shulkerWeightYellow));
            if(shulkerWeightLime > 0) WEIGHTS.add(new ColorEntry(EnumDyeColor.LIME, shulkerWeightLime));
            if(shulkerWeightGreen > 0) WEIGHTS.add(new ColorEntry(EnumDyeColor.GREEN, shulkerWeightGreen));
            if(shulkerWeightCyan > 0) WEIGHTS.add(new ColorEntry(EnumDyeColor.CYAN, shulkerWeightCyan));
            if(shulkerWeightLightBlue > 0) WEIGHTS.add(new ColorEntry(EnumDyeColor.LIGHT_BLUE, shulkerWeightLightBlue));
            if(shulkerWeightBlue > 0) WEIGHTS.add(new ColorEntry(EnumDyeColor.BLUE, shulkerWeightBlue));
            if(shulkerWeightPurple > 0) WEIGHTS.add(new ColorEntry(EnumDyeColor.PURPLE, shulkerWeightPurple));
            if(shulkerWeightMagenta > 0) WEIGHTS.add(new ColorEntry(EnumDyeColor.MAGENTA, shulkerWeightMagenta));
            if(shulkerWeightPink > 0) WEIGHTS.add(new ColorEntry(EnumDyeColor.PINK, shulkerWeightPink));
            if(shulkerWeightWhite > 0) WEIGHTS.add(new ColorEntry(EnumDyeColor.WHITE, shulkerWeightWhite));
            if(shulkerWeightLightGray > 0) WEIGHTS.add(new ColorEntry(EnumDyeColor.SILVER, shulkerWeightLightGray));
            if(shulkerWeightGray > 0) WEIGHTS.add(new ColorEntry(EnumDyeColor.GRAY, shulkerWeightGray));
            if(shulkerWeightBlack > 0) WEIGHTS.add(new ColorEntry(EnumDyeColor.BLACK, shulkerWeightBlack));
            if(shulkerWeightBrown > 0) WEIGHTS.add(new ColorEntry(EnumDyeColor.BROWN, shulkerWeightBrown));
        }
    }
}
