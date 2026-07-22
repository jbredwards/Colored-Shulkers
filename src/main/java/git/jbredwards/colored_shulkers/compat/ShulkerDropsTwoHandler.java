package git.jbredwards.colored_shulkers.compat;

import com.natamus.shulkerdropstwo.config.ModConfig;
import com.natamus.shulkerdropstwo.events.EntityEvent;
import net.minecraft.init.Items;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jbred
 *
 */
public final class ShulkerDropsTwoHandler
{
    public static void init() {
        MinecraftForge.EVENT_BUS.register(ShulkerDropsTwoHandler.class);
        ReflectionHelper.<Map<Object, ?>, EventBus>getPrivateValue(EventBus.class, MinecraftForge.EVENT_BUS, "listeners")
                .keySet().stream().filter(o -> o.getClass() == EntityEvent.class).forEach(MinecraftForge.EVENT_BUS::unregister);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void apply(@Nonnull final LootTableLoadEvent event) {
        if(LootTableList.ENTITIES_SHULKER.equals(event.getName())) {
            ObfuscationReflectionHelper.<List<LootPool>, LootTable>getPrivateValue(LootTable.class, event.getTable(), "field_186466_c").forEach(pool -> {
                @Nonnull final List<LootEntry> lootEntries = ObfuscationReflectionHelper.getPrivateValue(LootPool.class, pool, "field_186453_a");
                @Nullable LootCondition[] poolConditions = null;
                // Apply config to shell entries.
                for(@Nonnull final LootEntry entry : lootEntries) {
                    if(entry instanceof LootEntryItem && ((LootEntryItem)entry).item == Items.SHULKER_SHELL) {
                        ((LootEntryItem)entry).functions = ArrayUtils.add(((LootEntryItem)entry).functions, new SetCount(new LootCondition[0], new RandomValueRange(ModConfig.shulkerDropAmount)));
                        if(ModConfig.alwaysDropShells) {
                            entry.conditions = new LootCondition[0];
                            if(poolConditions == null) {
                                @Nonnull final List<LootCondition> conditions = ObfuscationReflectionHelper.getPrivateValue(LootPool.class, pool, "field_186454_b");
                                poolConditions = conditions.toArray(new LootCondition[0]);
                                conditions.clear();
                            }
                        }
                    }
                }
                // Re-apply pool conditions to non-shell entries.
                if(poolConditions != null) for(@Nonnull final LootEntry entry : lootEntries) {
                    if(!(entry instanceof LootEntryItem) || ((LootEntryItem)entry).item != Items.SHULKER_SHELL) {
                        entry.conditions = ArrayUtils.addAll(entry.conditions, poolConditions);
                    }
                }
            });
        }
    }
}
