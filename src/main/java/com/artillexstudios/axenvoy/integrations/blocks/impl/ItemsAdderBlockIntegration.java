package com.artillexstudios.axenvoy.integrations.blocks.impl;

import com.artillexstudios.axenvoy.integrations.blocks.BlockIntegration;
import dev.lone.itemsadder.api.CustomBlock;
import org.bukkit.Location;
import org.bukkit.Material;

public class ItemsAdderBlockIntegration implements BlockIntegration {

    @Override
    public void place(String id, Location location) {
        String blockId = id.substring("itemsadder:".length());
        CustomBlock block = CustomBlock.getInstance(blockId);
        if (block == null) return;

        location.getBlock().setType(Material.STONE);
        block.place(location);
    }

    @Override
    public void remove(Location location) {
        CustomBlock block = CustomBlock.byAlreadyPlaced(location.getBlock());
        if (block == null) return;

        block.remove();
        location.getBlock().setType(Material.AIR);
    }
}
