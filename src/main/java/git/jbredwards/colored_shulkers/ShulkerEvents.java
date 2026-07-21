package git.jbredwards.colored_shulkers;

import com.google.common.collect.ImmutableMap;
import git.jbredwards.colored_shulkers.config.ColoredShulkersCfg;
import git.jbredwards.colored_shulkers.registry.RainbowShulkerBox;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumParticleTypes;
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
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.DyeUtils;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

/**
 *
 * @author jbred
 *
 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ShulkerEvents
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
    static void shulkerDying(@Nonnull final PlayerInteractEvent.EntityInteract event) {
        if(ColoredShulkersCfg.inWorldDying && event.getTarget() instanceof EntityShulker) {
            @Nonnull final ItemStack held = event.getItemStack();
            if(!held.isEmpty()) {
                @Nullable final Function<ItemStack, ShulkerType> mappedColor = SHELL_COLOR_GETTER.get(held.getItem());
                @Nullable ShulkerType shellColor = mappedColor != null ? mappedColor.apply(held) : null;

                // Support all dye items (if enabled).
                if(mappedColor == null && ColoredShulkersCfg.inWorldDyingWithDyes) {
                    @Nonnull final Optional<EnumDyeColor> dyeColor = DyeUtils.colorFromStack(held);
                    if(dyeColor.isPresent()) shellColor = ShulkerType.color(dyeColor.get());
                }

                // Perform shulker coloring.
                if(shellColor != null && shellColor.canApply((EntityShulker)event.getTarget())) {
                    if(!event.getWorld().isRemote && !event.getEntityPlayer().isCreative()) {
                        if(held.getItem().isDamageable()) held.damageItem(1, event.getEntityPlayer());
                        else {
                            ItemHandlerHelper.giveItemToPlayer(event.getEntityPlayer(), held.getItem() == Items.POTIONITEM ? new ItemStack(Items.GLASS_BOTTLE) : held.getItem().getContainerItem(held));
                            held.shrink(1);
                        }
                    }

                    shellColor.apply((EntityShulker)event.getTarget());
                    event.setCancellationResult(EnumActionResult.SUCCESS);
                    event.setCanceled(true);

                    // Disable shell drops if shell was changed.
                    event.getTarget().getEntityData().setBoolean(ShulkerUtils.DROPS_TAG, ColoredShulkersCfg.inWorldDyingNoDrops);

                    // Close shulker and play FX.
                    ((EntityShulker)event.getTarget()).updateArmorModifier(0);
                    event.getEntityPlayer().swingArm(event.getHand());
                    if(!ColoredShulkersCfg.inWorldDyingFX) return;
                    event.getTarget().playSound(ColoredShulkers.SHULKER_DYED, 1, 1);
                    event.getWorld().spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, event.getTarget().posX, event.getTarget().posY + event.getTarget().height / 2, event.getTarget().posZ, 0, 0, 0);
                }
            }
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
    static List<EnumDyeColor> shellColors() {
        @Nonnull final List<EnumDyeColor> colors = new ArrayList<>();
        colors.add(null);
        colors.addAll(Arrays.asList(EnumDyeColor.values()));
        return colors;
    }

    /**
     * Allows modpack developers to have advanced control over the items able to dye shulkers.
     * <p>By default, only shulker shells can be used to dye shulkers.</p>
     */
    @Nonnull
    public static final Map<Item, Function<ItemStack, ShulkerType>> SHELL_COLOR_GETTER = new HashMap<>();
    public interface ShulkerType
    {
        boolean canApply(@Nonnull final EntityShulker shulker);
        void apply(@Nonnull final EntityShulker shulker);

        @Nonnull
        static ShulkerType color(@Nullable final EnumDyeColor color) {
            return new ShulkerType() {
                @Override
                public boolean canApply(@Nonnull final EntityShulker shulker) {
                    return ShulkerUtils.isRainbow(shulker) || ShulkerUtils.getColor(shulker).orElse(null) != color;
                }

                @Override
                public void apply(@Nonnull final EntityShulker shulker) {
                    ShulkerUtils.setColor(shulker, color);
                }
            };
        }

        @Nonnull
        static ShulkerType rainbow() {
            return new ShulkerType() {
                @Override
                public boolean canApply(@Nonnull final EntityShulker shulker) {
                    return !ShulkerUtils.isRainbow(shulker);
                }

                @Override
                public void apply(@Nonnull final EntityShulker shulker) {
                    ShulkerUtils.setRainbow(shulker);
                }
            };
        }
    }

    /**
     * The list of dye items that can be made from colored shulker shells.
     * <p>Public so we can support generic modded dye items, like the BOP dyes.</p>
     */
    @Nonnull
    public static final Map<EnumDyeColor, ItemStack> SHELL_TO_DYE = new HashMap<>(ImmutableMap.<EnumDyeColor, ItemStack>builder()
            .put(EnumDyeColor.ORANGE, new ItemStack(Items.DYE, 1, EnumDyeColor.ORANGE.getDyeDamage()))
            .put(EnumDyeColor.MAGENTA, new ItemStack(Items.DYE, 1, EnumDyeColor.MAGENTA.getDyeDamage()))
            .put(EnumDyeColor.LIGHT_BLUE, new ItemStack(Items.DYE, 1, EnumDyeColor.LIGHT_BLUE.getDyeDamage()))
            .put(EnumDyeColor.YELLOW, new ItemStack(Items.DYE, 1, EnumDyeColor.YELLOW.getDyeDamage()))
            .put(EnumDyeColor.LIME, new ItemStack(Items.DYE, 1, EnumDyeColor.LIME.getDyeDamage()))
            .put(EnumDyeColor.PINK, new ItemStack(Items.DYE, 1, EnumDyeColor.PINK.getDyeDamage()))
            .put(EnumDyeColor.GRAY, new ItemStack(Items.DYE, 1, EnumDyeColor.GRAY.getDyeDamage()))
            .put(EnumDyeColor.SILVER, new ItemStack(Items.DYE, 1, EnumDyeColor.SILVER.getDyeDamage()))
            .put(EnumDyeColor.CYAN, new ItemStack(Items.DYE, 1, EnumDyeColor.CYAN.getDyeDamage()))
            .put(EnumDyeColor.PURPLE, new ItemStack(Items.DYE, 1, EnumDyeColor.PURPLE.getDyeDamage()))
            .put(EnumDyeColor.GREEN, new ItemStack(Items.DYE, 1, EnumDyeColor.GREEN.getDyeDamage()))
            .put(EnumDyeColor.RED, new ItemStack(Items.DYE, 1, EnumDyeColor.RED.getDyeDamage()))
            .build());
}
