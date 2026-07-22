package git.jbredwards.colored_shulkers.compat;

import com.zeitheron.baublesb.BaubleShulkerBoxes;
import com.zeitheron.baublesb.boxes.VanillaShulkerBox;
import com.zeitheron.baublesb.cap.BaubleProvider;
import git.jbredwards.colored_shulkers.ColoredShulkers;
import git.jbredwards.colored_shulkers.registry.RainbowShulkerBox;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
public final class ShulkerBaublesHandler
{
    public static void preInit() {
        MinecraftForge.EVENT_BUS.register(ShulkerBaublesHandler.class);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    static void attachCapability(@Nonnull final AttachCapabilitiesEvent<ItemStack> event) {
        if(!event.getObject().isEmpty()) {
            @Nullable final Block block = Block.getBlockFromItem(event.getObject().getItem());
            if(block != null && (block == ColoredShulkers.PURPLE_SHULKER_BOX || block == ColoredShulkers.RAINBOW_SHULKER_BOX)) {
                event.addCapability(BaubleShulkerBoxes.BAUBLE_CAP_PATH, new BaubleProvider(new VanillaShulkerBox(event.getObject()) {
                    @Override
                    public void onPlayerBaubleRender(@Nonnull final ItemStack stack, @Nonnull final EntityPlayer player, @Nonnull final RenderType type, final float partialTicks) {
                        RainbowShulkerBox.setRGB(block, 1);
                        super.onPlayerBaubleRender(stack, player, type, partialTicks);
                        GL11.glColor4f(1, 1, 1, 1);
                    }
                }));
            }
        }
    }
}
