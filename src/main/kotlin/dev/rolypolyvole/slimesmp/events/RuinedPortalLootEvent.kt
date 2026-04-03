package dev.rolypolyvole.slimesmp.events

import net.fabricmc.fabric.api.loot.v3.LootTableEvents
import net.fabricmc.fabric.api.loot.v3.LootTableSource
import net.minecraft.core.HolderLookup
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.Items
import net.minecraft.world.level.storage.loot.BuiltInLootTables
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator

class RuinedPortalLootEvent : ModEvent {
    override fun register() {
        val listener = LootTableEvents.Modify(this::onLootTableModify)
        LootTableEvents.MODIFY.register(listener)
    }

    private fun onLootTableModify(key: ResourceKey<LootTable>, tableBuilder: LootTable.Builder, source: LootTableSource, provider: HolderLookup.Provider) {
        if (!source.isBuiltin || key != BuiltInLootTables.RUINED_PORTAL) return

        val poolBuilder = LootPool.lootPool().add(
            LootItem.lootTableItem(Items.QUARTZ)
                .setWeight(40)
                .apply(SetItemCountFunction.setCount(
                    UniformGenerator.between(4.0f, 9.0f)
                ))
        )

        tableBuilder.withPool(poolBuilder)
    }
}