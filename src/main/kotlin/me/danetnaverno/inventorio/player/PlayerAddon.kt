package me.danetnaverno.inventorio.player

import me.danetnaverno.inventorio.client.config.InventorioConfigData
import me.danetnaverno.inventorio.packet.InventorioNetworking
import me.danetnaverno.inventorio.util.*
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.FireworkRocketEntity
import net.minecraft.item.FireworkItem
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.util.Hand

class PlayerAddon private constructor(val player: PlayerEntity)
{
    var quickBarMode = QuickBarMode.NOT_SELECTED
    var utilityBeltMode = UtilityBeltMode.FILTERED
    private var ignoredScreenHandlers = listOf<Class<out ScreenHandler>>()

    val inventoryAddon get() = (player.inventory as InventoryDuck).addon
    val handlerAddon get() = (player.playerScreenHandler as HandlerDuck).addon

    fun trySetRestrictionModesC2S(quickBarMode: QuickBarMode, utilityBarMode: UtilityBeltMode): Boolean
    {
        if (inventoryAddon.physicalQuickQar.any { it.isNotEmpty })
            return false
        this.quickBarMode = quickBarMode
        utilityBeltMode = utilityBarMode
        if (player.world.isClient)
            InventorioNetworking.C2SSendPlayerSettings()
        return true
    }

    fun trySetRestrictionModesS2C(quickBarMode: QuickBarMode, utilityBarMode: UtilityBeltMode): Boolean
    {
        if (inventoryAddon.physicalQuickQar.any { it.isNotEmpty })
            return false
        this.quickBarMode = quickBarMode
        utilityBeltMode = utilityBarMode
        return true
    }

    @Suppress("UNCHECKED_CAST")
    fun setAllIgnoredScreenHandlers(ignoredScreenHandlers: List<String>)
    {
        this.ignoredScreenHandlers = ignoredScreenHandlers.mapNotNull {
            try
            {
                Class.forName(it) as Class<out ScreenHandler>
            }
            catch (e: Throwable)
            {
                null
            }
        }
    }

    fun addScreenHandlerToIgnored(screenHandler: ScreenHandler)
    {
        ignoredScreenHandlers = ignoredScreenHandlers + screenHandler.javaClass
        if (FabricLoader.getInstance().environmentType == EnvType.CLIENT)
        {
            InventorioConfigData.config().ignoredScreens += screenHandler.javaClass.name
            InventorioConfigData.holder().save()
            InventorioNetworking.C2SSendIgnoredScreenHandlers()
        }
    }

    fun isScreenHandlerIgnored(screenHandler: ScreenHandler): Boolean
    {
        return ignoredScreenHandlers.contains(screenHandler.javaClass)
    }

    fun setQuickBar(newItems: MutableList<ItemStack>)
    {
        val qbStacks = inventoryAddon.shortcutQuickBar.stacks
        for(i in 0 until Math.min(qbStacks.size, newItems.size))
            qbStacks[i] = newItems[i]
        if (player.world.isClient)
            InventorioNetworking.C2SSendNewQuickBar(newItems)
    }

    fun fireRocketFromInventory()
    {
        for (section in inventoryAddon.combinedInventory)
            for (itemStack in section)
                if (itemStack.item is FireworkItem)
                {
                    //todo exclude explosives
                    if (player.isFallFlying)
                    {
                        if (player.world.isClient)
                            InventorioNetworking.C2SFireRocket()
                        else
                            player.world.spawnEntity(FireworkRocketEntity(player.world, itemStack, player))
                        player.swingHand(Hand.MAIN_HAND)
                        itemStack.decrement(1)
                        inventoryAddon.mainHandDisplayTool = itemStack
                        return
                    }
                }
    }

    @Environment(EnvType.CLIENT)
    object Client
    {
        val local get() = get(MinecraftClient.getInstance().player!!)
        var selectedQuickBarSection = -1
        @JvmField var triesToUseUtility = false
    }

    companion object
    {
        fun create(player: PlayerEntity): PlayerAddon
        {
            return PlayerAddon(player)
        }

        @JvmStatic
        operator fun get(player: PlayerEntity): PlayerAddon
        {
            return (player as PlayerDuck).addon
        }
    }
}