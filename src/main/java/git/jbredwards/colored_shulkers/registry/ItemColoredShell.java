package git.jbredwards.colored_shulkers.registry;

import git.jbredwards.colored_shulkers.ColoredShulkers;
import git.jbredwards.colored_shulkers.ShulkerUtils;
import git.jbredwards.colored_shulkers.Tags;
import git.jbredwards.colored_shulkers.config.ColoredShulkersCfg;
import git.jbredwards.colored_shulkers.dying.ShulkerDying;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author jbred
 *
 */
public class ItemColoredShell extends Item
{
    @Nonnull
    private static final TextFormatting[] rainbowFormat = new TextFormatting[] { TextFormatting.RED, TextFormatting.GOLD, TextFormatting.YELLOW, TextFormatting.GREEN, TextFormatting.BLUE, TextFormatting.DARK_PURPLE, TextFormatting.LIGHT_PURPLE };
    public ItemColoredShell() { setHasSubtypes(true).setCreativeTab(CreativeTabs.MATERIALS); }

    @Override
    public void getSubItems(@Nonnull final CreativeTabs tab, @Nonnull final NonNullList<ItemStack> items) {
        if(isInCreativeTab(tab)) for(int meta = 0; meta <= ShulkerUtils.RAINBOW_META; meta++) items.add(new ItemStack(this, 1, meta));
    }

    @Nonnull
    public static Optional<EnumDyeColor> byShellDamage(final int meta) {
        if(meta > 15) return Optional.empty(); // Invalid shell.
        return Optional.of(EnumDyeColor.byMetadata(meta));
    }

    @Nonnull
    public static String localizeColor(@Nonnull final String color) {
        return I18n.translateToLocalFormatted("color." + Tags.MOD_ID + '.' + color, I18n.translateToLocal("item.fireworksCharge." + color));
    }

    @Nonnull
    @Override
    public String getItemStackDisplayName(@Nonnull final ItemStack stack) {
        @Nullable final String color = stack.getMetadata() == ShulkerUtils.RAINBOW_META ? "rainbow" : ShulkerUtils.colorFromShell(stack).map(EnumDyeColor::getTranslationKey).orElse(null);
        if(color == null || ColoredShulkersCfg.shellColorInTooltip) return super.getItemStackDisplayName(stack);

        @Nonnull final String colorKey = getTranslationKey(stack) + '.' + color + ".name";
        if(I18n.canTranslate(colorKey)) return I18n.translateToLocal(colorKey);

        // Generic name using color lang values.
        return I18n.translateToLocalFormatted(Tags.MOD_ID + '.' + getTranslationKey(stack) + ".name", super.getItemStackDisplayName(stack), localizeColor(color));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(@Nonnull final ItemStack stack, @Nullable final World worldIn, @Nonnull final List<String> tooltip, @Nonnull final ITooltipFlag flagIn) {
        if(stack.getMetadata() == ShulkerUtils.RAINBOW_META) {
            if(ColoredShulkersCfg.rainbowShellBreaking) {
                if(!GuiScreen.isShiftKeyDown()) tooltip.add(I18n.translateToLocal("tooltip." + Tags.MOD_ID + ".hidden"));
                else tooltip.add(I18n.translateToLocal("tooltip." + Tags.MOD_ID + ".rainbow_shulker_shell." + (ColoredShulkersCfg.rainbowShellXP > 0 ? "shownXp" : "shown")));
            }

            if(ColoredShulkersCfg.shellColorInTooltip) tooltip.add(rainbowFormat[RainbowShulkerBox.getTicks(0.00075) % rainbowFormat.length] + localizeColor("rainbow"));
        }
        else ShulkerUtils.colorFromShell(stack).ifPresent(color -> {
            if(ColoredShulkersCfg.shellToDyeBreaking > 0) {
                @Nullable final ItemStack dye = ShulkerDying.SHELL_TO_DYE.get(color);
                if(dye != null && !dye.isEmpty()) {
                    if(!GuiScreen.isShiftKeyDown()) tooltip.add(I18n.translateToLocal("tooltip." + Tags.MOD_ID + ".hidden"));
                    else tooltip.add(I18n.translateToLocalFormatted("tooltip." + Tags.MOD_ID + ".colored_shulker_shell." + (ColoredShulkersCfg.shellToDyeBreakingXP > 0 ? "shownXp" : "shown"),
                            ColoredShulkersCfg.shellToDyeBreaking, dye.getTextComponent().getFormattedText()));
                }
            }

            if(ColoredShulkersCfg.shellColorInTooltip) tooltip.add(localizeColor(color.getTranslationKey()));
        });
    }

    @Nonnull
    @Override
    public EnumRarity getRarity(@Nonnull final ItemStack stack) {
        return stack.getMetadata() == ShulkerUtils.RAINBOW_META ? EnumRarity.RARE : super.getRarity(stack);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull final World worldIn, @Nonnull final EntityPlayer playerIn, @Nonnull final EnumHand handIn) {
        @Nonnull final ItemStack held = playerIn.getHeldItem(handIn);
        if(!held.isEmpty()) {
            // Rainbow shell breaking.
            if(held.getMetadata() == ShulkerUtils.RAINBOW_META) {
                if(ColoredShulkersCfg.rainbowShellBreaking) {
                    if(worldIn instanceof WorldServer) {
                        final int consumed = playerIn.isSneaking() ? Math.min(16, held.getCount()) : 1;
                        drop(worldIn, playerIn, ColoredShulkersCfg.rainbowShellXP * consumed, worldIn.getLootTableManager().getLootTableFromLocation(ColoredShulkers.RAINBOW_SHELL_TABLE)
                                .generateLootForPools(worldIn.rand, new LootContext.Builder((WorldServer)worldIn).withPlayer(playerIn).withLuck(consumed - 1).build()));

                        worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, ColoredShulkers.RAINBOW_SHELL_USE, playerIn.getSoundCategory(), 1, 1);
                        held.shrink(consumed);
                    }

                    return ActionResult.newResult(EnumActionResult.SUCCESS, held);
                }
            }
            // Colored shell breaking.
            else if(ColoredShulkersCfg.shellToDyeBreaking > 0) {
                @Nullable final ItemStack dye = ShulkerUtils.colorFromShell(held).map(ShulkerDying.SHELL_TO_DYE::get).orElse(null);
                if(dye != null && !dye.isEmpty()) {
                    if(!worldIn.isRemote) {
                        final int consumed = playerIn.isSneaking() ? Math.min(16, held.getCount()) : 1;

                        @Nonnull final ItemStack stack = ItemHandlerHelper.copyStackWithSize(dye, ColoredShulkersCfg.shellToDyeBreaking * consumed);
                        @Nonnull final List<ItemStack> stacks = new ArrayList<>();
                        while(!stack.isEmpty()) stacks.add(stack.splitStack(stack.getMaxStackSize()));

                        drop(worldIn, playerIn, ColoredShulkersCfg.shellToDyeBreakingXP * consumed, stacks);
                        worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, ColoredShulkers.COLORED_SHELL_USE, playerIn.getSoundCategory(), 1, 1);
                        held.shrink(consumed);
                    }

                    return ActionResult.newResult(EnumActionResult.SUCCESS, held);
                }
            }
        }

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    private static void drop(@Nonnull final World worldIn, @Nonnull final EntityPlayer playerIn, final int xp, @Nonnull final Iterable<ItemStack> loot) {
        for(@Nonnull final ItemStack shell : loot) {
            if(playerIn instanceof FakePlayer) ItemHandlerHelper.giveItemToPlayer(playerIn, shell);
            else playerIn.dropItem(shell, false, false);
        }

        if(!(playerIn instanceof FakePlayer)) for(int i = xp; i > 0;) {
            final int split = EntityXPOrb.getXPSplit(i);
            worldIn.spawnEntity(new EntityXPOrb(worldIn, playerIn.posX, playerIn.posY + playerIn.getEyeHeight(), playerIn.posZ, split));
            i -= split;
        }
    }
}
