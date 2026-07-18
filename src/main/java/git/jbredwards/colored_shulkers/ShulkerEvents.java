package git.jbredwards.colored_shulkers;

import com.google.common.collect.ImmutableMap;
import git.jbredwards.colored_shulkers.registry.ItemColoredShell;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
                                if(ColoredShulkers.Cfg.enableDrops && stack.getItem() == Items.SHULKER_SHELL && context.getLootedEntity() instanceof EntityShulker) {
                                    if(ShulkerUtils.isRainbow((EntityShulker)context.getLootedEntity())) return new ItemStack(ColoredShulkers.SHELL, stack.getCount(), 15);

                                    @Nonnull final EnumDyeColor color = ShulkerUtils.getColor((EntityShulker)context.getLootedEntity());
                                    if(color != EnumDyeColor.PURPLE) return ShulkerUtils.shellFromColor(color, stack.getCount());
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
        if(event.getTarget() instanceof EntityShulker) {
            @Nonnull final ItemStack held = event.getItemStack();
            if(!held.isEmpty() && SHELL_COLOR_GETTER.containsKey(held.getItem())) {
                @Nonnull final EnumDyeColor shulkerColor = ShulkerUtils.getColor((EntityShulker)event.getTarget());
                @Nullable final EnumDyeColor shellColor = SHELL_COLOR_GETTER.get(held.getItem()).apply(held);

                if(shellColor != null && shulkerColor != shellColor) {
                    if(!event.getWorld().isRemote && !event.getEntityPlayer().isCreative()) held.shrink(1);
                    ShulkerUtils.setColor((EntityShulker)event.getTarget(), shellColor);
                    event.setCancellationResult(EnumActionResult.SUCCESS);
                    event.setCanceled(true);

                    // Close shulker and play FX.
                    ((EntityShulker)event.getTarget()).updateArmorModifier(0);
                    event.getEntityPlayer().swingArm(event.getHand());
                    event.getTarget().playSound(ColoredShulkers.SHULKER_DYED, 1, 1);
                    event.getWorld().spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, event.getTarget().posX, event.getTarget().posY + event.getTarget().height / 2, event.getTarget().posZ, 0, 0, 0);

                    // Debugging
                    // ((QuarkHandler.EnchantCapability)Objects.requireNonNull(event.getTarget().getCapability(IEnchantColorProvider.CAPABILITY, null))).setColor(shellColor.getColorValue());
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    static void shulkerEgg(@Nonnull final LivingSpawnEvent.SpecialSpawn event) {
        if(event.getEntity() instanceof EntityShulker && (event.getSpawner() == null ? ColoredShulkers.Cfg.enableWorld : ColoredShulkers.Cfg.enableSpawner)) {
            ShulkerUtils.setColor((EntityShulker)event.getEntity(), WeightedRandom.getRandomItem(event.getWorld().rand, ColoredShulkers.Cfg.WEIGHTS).color);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOW)
    static void shulkerShellTooltip(@Nonnull final ItemTooltipEvent event) {
        ShulkerUtils.colorFromShell(event.getItemStack()).ifPresent(color -> event.getToolTip().add(1, ItemColoredShell.localizeColor(color)));
    }

    /**
     * Allows modpack developers to have advanced control over the items able to dye shulkers.
     * <p>By default, only shulker shells can be used to dye shulkers.</p>
     */
    @Nonnull
    public static final Map<Item, Function<ItemStack, EnumDyeColor>> SHELL_COLOR_GETTER = new HashMap<>();

    /**
     * The list of dye items that can be made from colored shulker shells.
     * <p>Public so we can support generic modded dye items, like the BOP dyes.</p>
     */
    @Nonnull
    public static final Map<EnumDyeColor, ItemStack> SHELL_TO_DYE = new HashMap<>(ImmutableMap.<EnumDyeColor, ItemStack>builder()
            .put(EnumDyeColor.ORANGE, new ItemStack(Items.DYE, 8, EnumDyeColor.ORANGE.getDyeDamage()))
            .put(EnumDyeColor.MAGENTA, new ItemStack(Items.DYE, 8, EnumDyeColor.MAGENTA.getDyeDamage()))
            .put(EnumDyeColor.LIGHT_BLUE, new ItemStack(Items.DYE, 8, EnumDyeColor.LIGHT_BLUE.getDyeDamage()))
            .put(EnumDyeColor.YELLOW, new ItemStack(Items.DYE, 8, EnumDyeColor.YELLOW.getDyeDamage()))
            .put(EnumDyeColor.LIME, new ItemStack(Items.DYE, 8, EnumDyeColor.LIME.getDyeDamage()))
            .put(EnumDyeColor.PINK, new ItemStack(Items.DYE, 8, EnumDyeColor.PINK.getDyeDamage()))
            .put(EnumDyeColor.GRAY, new ItemStack(Items.DYE, 8, EnumDyeColor.GRAY.getDyeDamage()))
            .put(EnumDyeColor.SILVER, new ItemStack(Items.DYE, 8, EnumDyeColor.SILVER.getDyeDamage()))
            .put(EnumDyeColor.CYAN, new ItemStack(Items.DYE, 8, EnumDyeColor.CYAN.getDyeDamage()))
            .put(EnumDyeColor.PURPLE, new ItemStack(Items.DYE, 8, EnumDyeColor.PURPLE.getDyeDamage()))
            .put(EnumDyeColor.GREEN, new ItemStack(Items.DYE, 8, EnumDyeColor.GREEN.getDyeDamage()))
            .put(EnumDyeColor.RED, new ItemStack(Items.DYE, 8, EnumDyeColor.RED.getDyeDamage()))
            .build());
}
