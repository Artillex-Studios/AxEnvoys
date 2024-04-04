package com.artillexstudios.axenvoy.integrations.blocks.impl;

import com.artillexstudios.axenvoy.integrations.blocks.BlockIntegration;
import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomFurniture;
import org.bukkit.Location;

@SuppressWarnings("UnreachableCode")
public class ItemsAdderBlockIntegration implements BlockIntegration {

    @Override
    public void place(String id, Location location) {
        String blockId = id.substring("itemsadder:".length());
        if (CustomBlock.isInRegistry(blockId)) {
            CustomBlock block = CustomBlock.getInstance(blockId);

            if (block != null) {
                block.place(location);
            }
        } else if (CustomFurniture.isInRegistry(blockId)) {
            CustomFurniture.spawn(blockId, location.getBlock());
        }
    }

    @Override
    public void remove(Location location) {
        CustomBlock block = CustomBlock.byAlreadyPlaced(location.getBlock());
        if (block != null) {
            block.remove();
            return;
        }

        CustomFurniture furniture = CustomFurniture.byAlreadySpawned(location.getBlock());

        if (furniture != null) {
            furniture.remove(false);
        }
    }
}
