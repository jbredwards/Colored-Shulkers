package git.jbredwards.colored_shulkers.config;

import git.jbredwards.colored_shulkers.Tags;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.WeightedRandom;
import net.minecraftforge.common.config.Config;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
@Config(modid = Tags.MOD_ID)
public final class ColoredShulkersCfg
{
    public enum EnableType
    {
        ENABLED,
        COLOR_ONLY,
        DISABLED
    }

    @Config.LangKey("cfg." + Tags.MOD_ID + ".enableDrops")
    public static boolean enableDrops = true;

    @Config.LangKey("cfg." + Tags.MOD_ID + ".enableEndCity")
    @Nonnull public static EnableType enableEndCity = EnableType.ENABLED;

    @Config.LangKey("cfg." + Tags.MOD_ID + ".enableSpawner")
    @Nonnull public static EnableType enableSpawner = EnableType.DISABLED;

    @Config.LangKey("cfg." + Tags.MOD_ID + ".enableWorld")
    @Nonnull public static EnableType enableWorld = EnableType.DISABLED;

    @Config.LangKey("cfg." + Tags.MOD_ID + ".inWorldDying")
    public static boolean inWorldDying = true;

    @Config.LangKey("cfg." + Tags.MOD_ID + ".inWorldDyingFX")
    public static boolean inWorldDyingFX = false;

    @Config.LangKey("cfg." + Tags.MOD_ID + ".inWorldDyingNoDrops")
    public static boolean inWorldDyingNoDrops = false;

    @Config.LangKey("cfg." + Tags.MOD_ID + ".inWorldDyingWithDyes")
    public static boolean inWorldDyingWithDyes = true;

    @Config.LangKey("cfg." + Tags.MOD_ID + ".rainbowShellBreaking")
    public static boolean rainbowShellBreaking = true;

    @Config.LangKey("cfg." + Tags.MOD_ID + ".rainbowShellXP")
    public static int rainbowShellXP = 55;

    @Config.LangKey("cfg." + Tags.MOD_ID + ".shellColorInTooltip")
    public static boolean shellColorInTooltip = false;

    @Config.RequiresMcRestart
    @Config.LangKey("cfg." + Tags.MOD_ID + ".shellDyingRecipes")
    public static boolean shellDyingRecipes = true;

    @Config.RequiresMcRestart
    @Config.RangeInt(min = 0, max = 64)
    @Config.LangKey("cfg." + Tags.MOD_ID + ".shellToDyeRecipes")
    public static int shellToDyeRecipes = 16;

    @Config.RangeDouble(min = 0, max = 1)
    @Config.LangKey("cfg." + Tags.MOD_ID + ".shulkerChanceRainbow")
    public static double shulkerChanceRainbow = 0.05;

    @Config.LangKey("cfg." + Tags.MOD_ID + ".shulkerWeightColorless")
    public static int shulkerWeightColorless = 1;

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
        @Nullable
        public final EnumDyeColor color;
        public ColorEntry(@Nullable final EnumDyeColor colorIn, final int itemWeightIn) {
            super(itemWeightIn);
            color = colorIn;
        }
    }

    public static void sync() {
        WEIGHTS.clear();
        if(shulkerWeightColorless > 0) WEIGHTS.add(new ColorEntry(null, shulkerWeightColorless));
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
