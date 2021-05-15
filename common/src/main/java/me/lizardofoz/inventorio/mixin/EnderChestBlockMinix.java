package me.lizardofoz.inventorio.mixin;

import me.lizardofoz.inventorio.extra.InventorioServerConfig;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.OptionalInt;

/**
 * This mixin enlarges the Ender Chest's Inventory Screen to 6 rows.
 * To enlarge the actual storage, {@link PlayerEntityMixin#inventorioResizeEnderChest} is used
 */
@Mixin(value = EnderChestBlock.class)
public class EnderChestBlockMinix
{
    @Redirect(method = "onBlockActivated",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;openHandledScreen(Lnet/minecraft/screen/NamedScreenHandlerFactory;)Ljava/util/OptionalInt;",
                    ordinal = 0),
            require = 0)
    private OptionalInt inventorioOnEnderChestOpenForge(PlayerEntity playerEntity, NamedScreenHandlerFactory factory)
    {
        return playerEntity.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInventory, playerEntityInner) ->
        {
            if (InventorioServerConfig.INSTANCE.getExpandedEnderChest())
                return GenericContainerScreenHandler.createGeneric9x6(syncId, playerInventory, playerEntityInner.getEnderChestInventory());
            else
                return GenericContainerScreenHandler.createGeneric9x3(syncId, playerInventory, playerEntityInner.getEnderChestInventory());
        }, new TranslatableText("container.enderchest")));
    }

    @Redirect(method = "onUse",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;openHandledScreen(Lnet/minecraft/screen/NamedScreenHandlerFactory;)Ljava/util/OptionalInt;",
                    ordinal = 0),
            require = 0)
    private OptionalInt inventorioOnEnderChestOpenFabric(PlayerEntity playerEntity, NamedScreenHandlerFactory factory)
    {
        return playerEntity.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInventory, playerEntityInner) ->
        {
            if (InventorioServerConfig.INSTANCE.getExpandedEnderChest())
                return GenericContainerScreenHandler.createGeneric9x6(syncId, playerInventory, playerEntityInner.getEnderChestInventory());
            else
                return GenericContainerScreenHandler.createGeneric9x3(syncId, playerInventory, playerEntityInner.getEnderChestInventory());
        }, new TranslatableText("container.enderchest")));
    }
}