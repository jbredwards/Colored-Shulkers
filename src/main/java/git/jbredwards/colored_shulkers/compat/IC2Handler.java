package git.jbredwards.colored_shulkers.compat;

import com.google.common.base.MoreObjects;
import git.jbredwards.colored_shulkers.dying.ShulkerDyeableAction;
import git.jbredwards.colored_shulkers.dying.ShulkerDyeableHolder;
import ic2.api.classic.audio.IAudioManager;
import ic2.core.IC2;
import ic2.core.audio.PositionSpec;
import ic2.core.item.tool.ItemToolPainter;
import ic2.core.platform.registry.Ic2Sounds;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * @author jbred
 *
 */
public final class IC2Handler
{
    @Nonnull private static final Function<ItemStack, EnumDyeColor> colorManager;
    @Nonnull private static final TriConsumer<ItemStack, EntityPlayer, EnumHand> damageManager;
    @Nonnull private static final Consumer<EntityPlayer> soundManager;

    static {
        @Nonnull TriConsumer<ItemStack, EntityPlayer, EnumHand> damageFunc;
        @Nonnull Function<ItemStack, EnumDyeColor> colorFunc;
        @Nonnull Consumer<EntityPlayer> soundFunc;

        // IC2 Classic:
        try {
            colorFunc = stack -> EnumDyeColor.byDyeDamage(stack.getMetadata());
            @Nonnull final Method damagePainter = ItemToolPainter.class.getDeclaredMethod("damagePainter", ItemStack.class, EntityPlayer.class, EnumHand.class);
            damageFunc = (stack, player, hand) -> { try { damagePainter.invoke(null, stack, player, hand); } catch(@Nonnull final Throwable t) { throw new RuntimeException(t); } };
            @Nonnull final IAudioManager audioManager = (IAudioManager)IC2.audioManager;
            soundFunc = player -> audioManager.playOnce(player, ic2.api.classic.audio.PositionSpec.Hand, Ic2Sounds.painterUse, true, 4);
        }
        // IC2:
        catch(@Nonnull final Throwable t) {
            colorFunc = stack -> ((ItemToolPainter)stack.getItem()).getColor(stack).mcColor;
            damageFunc = (stack, player, hand) -> ((ItemToolPainter)stack.getItem()).damagePainter(player, hand, ((ItemToolPainter)stack.getItem()).getColor(stack));
            soundFunc = player -> IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/Painter.ogg", true, IC2.audioManager.getDefaultVolume());
        }

        colorManager = colorFunc;
        damageManager = damageFunc;
        soundManager = soundFunc;
    }

    public static void init() {
        ShulkerDyeableAction.SHELL_COLOR_GETTER.put(MoreObjects.firstNonNull(Item.getByNameOrId("ic2:itempainters"), Item.getByNameOrId("ic2:painter")), stack -> {
            @Nonnull final EnumDyeColor color = colorManager.apply(stack);
            return new ShulkerDyeableAction() {
                @Override
                public void performShrink(@Nonnull final ItemStack stack, @Nonnull final EntityPlayer player, @Nonnull final EnumHand hand) {
                    damageManager.accept(stack, player, hand);
                }

                @Override
                public void playFX(@Nonnull final EntityPlayer player, @Nonnull final AxisAlignedBB box) {
                    ShulkerDyeableAction.super.playFX(player, box);
                    soundManager.accept(player);
                }

                @Override
                public boolean canApply(@Nonnull final ShulkerDyeableHolder shulker) {
                    return shulker.isRainbow() || shulker.getColor() != color;
                }

                @Override
                public void apply(@Nonnull final ShulkerDyeableHolder shulker) {
                    shulker.setColor(color);
                }
            };
        });
    }
}
