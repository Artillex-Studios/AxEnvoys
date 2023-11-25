package com.artillexstudios.axenvoy.integrations.blocks.impl;

import com.artillexstudios.axenvoy.integrations.blocks.BlockIntegration;
import io.th0rgal.oraxen.api.OraxenBlocks;
import org.bukkit.Location;

public class OraxenBlockIntegration implements BlockIntegration {

    @Override
    public void place(String id, Location location) {
        String blockId = id.substring("oraxen:".length());
        if (!OraxenBlocks.isOraxenBlock(blockId)) return;

        OraxenBlocks.place(blockId, location);
    }

    @Override
    public void remove(Location location) {
        if (!OraxenBlocks.isOraxenBlock(location.getBlock())) return;

        OraxenBlocks.remove(location, null);
    }
}
