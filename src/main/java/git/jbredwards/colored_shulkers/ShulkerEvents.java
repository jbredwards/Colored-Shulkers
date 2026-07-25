package git.jbredwards.colored_shulkers;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import git.jbredwards.colored_shulkers.config.ColoredShulkersCfg;
import git.jbredwards.colored_shulkers.dying.ShulkerDyeableHolder;
import git.jbredwards.colored_shulkers.dying.ShulkerDying;
import git.jbredwards.colored_shulkers.registry.RainbowShulkerBox;
import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.util.*;

/**
 *
 * @author jbred
 *
 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
final class ShulkerEvents
{
    @SubscribeEvent(priority = EventPriority.LOW)
    static void dropColoredShulkerShells(@Nonnull final LootTableLoadEvent event) {
        if(LootTableList.ENTITIES_SHULKER.equals(event.getName())) {
            ObfuscationReflectionHelper.<List<LootPool>, LootTable>getPrivateValue(LootTable.class, event.getTable(), "field_186466_c").forEach(pool -> {
                ObfuscationReflectionHelper.<List<LootEntry>, LootPool>getPrivateValue(LootPool.class, pool, "field_186453_a").forEach(entry -> {
                    if(entry instanceof LootEntryItem && ((LootEntryItem)entry).item == Items.SHULKER_SHELL) {
                        ((LootEntryItem)entry).functions = ArrayUtils.add(((LootEntryItem)entry).functions, new LootFunction(new LootCondition[0]) {
                            @Nonnull
                            @Override
                            public ItemStack apply(@Nonnull final ItemStack stack, @Nonnull final Random rand, @Nonnull final LootContext context) {
                                if(stack.getItem() == Items.SHULKER_SHELL && context.getLootedEntity() instanceof EntityShulker) {
                                    @Nonnull final EntityShulker shulker = (EntityShulker)context.getLootedEntity();

                                    // Disable drops.
                                    if(shulker.getEntityData().getBoolean(ShulkerUtils.DROPS_TAG)) return ItemStack.EMPTY;
                                    else if(!ColoredShulkersCfg.enableDrops) return stack;

                                    // Inject new shulker shell drops.
                                    else if(ShulkerUtils.isRainbow(shulker)) return new ItemStack(ColoredShulkers.SHELL, stack.getCount(), ShulkerUtils.RAINBOW_META);
                                    @Nonnull final Optional<EnumDyeColor> color = ShulkerUtils.getColor(shulker);
                                    if(color.isPresent()) return ShulkerUtils.shellFromColor(color.get(), stack.getCount());
                                }

                                return stack;
                            }
                        });
                    }
                });
            });
        }
    }

    @SubscribeEvent
    static void shulkerDying(@Nonnull final PlayerInteractEvent.RightClickBlock event) {
        if(ColoredShulkersCfg.inWorldDying) {
            @Nonnull final IBlockState state = event.getWorld().getBlockState(event.getPos());
            if(state.getBlock() instanceof BlockShulkerBox) {
                @Nonnull final ShulkerDyeableHolder holder = ShulkerDyeableHolder.block(event.getWorld(), event.getPos(), state);
                if(ShulkerDying.attemptDye(holder, event.getEntityPlayer(), event.getHand(), false)) {
                    event.setCancellationResult(EnumActionResult.SUCCESS);
                    event.setCanceled(true);
                }
                // If the off-hand item can dye the shulker and the main-hand cannot, pass the event to off-hand.
                else if(event.getHand() == EnumHand.MAIN_HAND && ShulkerDying.attemptDye(holder, event.getEntityPlayer(), EnumHand.OFF_HAND, true)) {
                    event.setCancellationResult(EnumActionResult.PASS);
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    static void shulkerDying(@Nonnull final PlayerInteractEvent.EntityInteract event) {
        if(event.getTarget() instanceof EntityShulker && ShulkerDying.attemptDye(ShulkerDyeableHolder.entity((EntityShulker)event.getTarget()), event.getEntityPlayer(), event.getHand(), false)) {
            event.setCancellationResult(EnumActionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    static void shulkerEgg(@Nonnull final LivingSpawnEvent.SpecialSpawn event) {
        @Nonnull final ColoredShulkersCfg.EnableType cfg = event.getSpawner() == null ? ColoredShulkersCfg.enableWorld : ColoredShulkersCfg.enableSpawner;
        if(event.getEntity() instanceof EntityShulker) ShulkerUtils.setRandomColor((EntityShulker)event.getEntity(), event.getWorld().rand, cfg);
    }

    @SubscribeEvent
    static void shulkerSync(@Nonnull final PlayerEvent.StartTracking event) {
        if(event.getTarget() instanceof EntityShulker) {
            @Nonnull final RainbowShulkerBox.Sync message = new RainbowShulkerBox.Sync();
            message.id = event.getTarget().getEntityId();
            message.rainbow = ShulkerUtils.isRainbow((EntityShulker)event.getTarget());
            message.purple = event.getTarget().getEntityData().getBoolean(ShulkerUtils.PURPLE_TAG);
            if(message.rainbow || message.purple) ColoredShulkers.WRAPPER.sendTo(message, (EntityPlayerMP)event.getEntityPlayer());
        }
    }

    @Nonnull
    static Set<EnumDyeColor> shellColors() {
        @Nonnull final List<EnumDyeColor> colors = new ArrayList<>();
        colors.add(null);
        colors.addAll(Arrays.asList(EnumDyeColor.values()));
        return Collections.unmodifiableSet(new LinkedHashSet<>(colors));
    }

    @Nonnull
    static BiMap<EnumDyeColor, Block> shulkerBoxes() {
        @Nonnull final BiMap<EnumDyeColor, Block> map = HashBiMap.create();
        map.put(null, Blocks.PURPLE_SHULKER_BOX);
        for(@Nonnull final EnumDyeColor color : EnumDyeColor.values()) map.put(color, BlockShulkerBox.getBlockByColor(color));
        return Maps.unmodifiableBiMap(map);
    }
}
