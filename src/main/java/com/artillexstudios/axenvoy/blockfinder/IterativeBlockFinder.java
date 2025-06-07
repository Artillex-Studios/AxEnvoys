package com.artillexstudios.axenvoy.blockfinder;

import com.artillexstudios.axenvoy.config.impl.Config;
import com.artillexstudios.axenvoy.config.impl.EnvoyConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

public class IterativeBlockFinder implements BlockFinder {
    private final Set<Material> passableBlocks = new HashSet<>();

    public IterativeBlockFinder() {
        for (String passableBlock : Config.PASSABLE_BLOCKS) {
            this.passableBlocks.add(Material.valueOf(passableBlock));
        }
    }

    @Override
    public Block highestBlockAt(Location location, EnvoyConfig config) {
        if (location == null || location.getWorld() == null) {
            return null;
        }

        int height = location.getWorld().getMaxHeight();
        while (height >= location.getWorld().getMinHeight()) {
            Block block = location.getWorld().getBlockAt(location.getBlockX(), height, location.getBlockZ());
            if (!this.passableBlocks.contains(block.getType())) {
                return block;
            }
            height--;
        }

        return null;
    }
}
