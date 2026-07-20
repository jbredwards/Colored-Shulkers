package git.jbredwards.colored_shulkers.registry;

import git.jbredwards.colored_shulkers.ColoredShulkers;
import git.jbredwards.colored_shulkers.Tags;
import git.jbredwards.colored_shulkers.config.ColoredShulkersCfg;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
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
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
        if(isInCreativeTab(tab)) for(int meta = 0; meta < 16; meta++) items.add(new ItemStack(this, 1, meta));
    }

    @Nonnull
    public static Optional<EnumDyeColor> byShellDamage(final int meta) {
        if(meta >= 15) return Optional.empty(); // Invalid shell.
        return Optional.of(EnumDyeColor.byMetadata(meta >= EnumDyeColor.PURPLE.getMetadata() ? meta + 1 : meta));
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    public static String localizeColor(@Nonnull final EnumDyeColor color) {
        return I18n.format("color." + Tags.MOD_ID + '.' + color.getTranslationKey(), I18n.format("item.fireworksCharge." + color.getTranslationKey()));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(@Nonnull final ItemStack stack, @Nullable final World worldIn, @Nonnull final List<String> tooltip, @Nonnull final ITooltipFlag flagIn) {
        if(stack.getMetadata() == 15) {
            tooltip.add(1, rainbowFormat[RainbowShulkerBox.getTicks(0.00075) % rainbowFormat.length] + I18n.format("color." + Tags.MOD_ID + ".rainbow"));
            if(ColoredShulkersCfg.rainbowShellBreaking) tooltip.add(1, I18n.format("tooltip." + Tags.MOD_ID + ".rainbow_shulker_shell." + (GuiScreen.isShiftKeyDown() ? "shown" : "hidden")));
        }
    }

    @Nonnull
    @Override
    public EnumRarity getRarity(@Nonnull final ItemStack stack) {
        return stack.getMetadata() == 15 ? EnumRarity.RARE : super.getRarity(stack);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull final World worldIn, @Nonnull final EntityPlayer playerIn, @Nonnull final EnumHand handIn) {
        if(ColoredShulkersCfg.rainbowShellBreaking) {
            @Nonnull final ItemStack held = playerIn.getHeldItem(handIn);
            if(!held.isEmpty() && held.getMetadata() == 15) {
                if(worldIn instanceof WorldServer) {
                    final int consumed = playerIn.isSneaking() ? Math.min(16, held.getCount()) : 1;
                    worldIn.getLootTableManager().getLootTableFromLocation(ColoredShulkers.RAINBOW_SHELL_TABLE).generateLootForPools(worldIn.rand,
                    new LootContext.Builder((WorldServer)worldIn).withPlayer(playerIn).withLuck(consumed - 1).build()).forEach(shell -> {
                        if(playerIn instanceof FakePlayer) ItemHandlerHelper.giveItemToPlayer(playerIn, shell);
                        else playerIn.dropItem(shell, false, false);
                    });

                    if(!(playerIn instanceof FakePlayer)) for(int i = ColoredShulkersCfg.rainbowShellXP * consumed; i > 0;) {
                        final int split = EntityXPOrb.getXPSplit(i);
                        worldIn.spawnEntity(new EntityXPOrb(worldIn, playerIn.posX, playerIn.posY + playerIn.getEyeHeight(), playerIn.posZ, split));
                        i -= split;
                    }

                    worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, ColoredShulkers.RAINBOW_SHELL_USE, playerIn.getSoundCategory(), 1, 1);
                    held.shrink(consumed);
                }

                return ActionResult.newResult(EnumActionResult.SUCCESS, held);
            }
        }

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }
}
