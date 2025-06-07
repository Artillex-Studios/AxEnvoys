package com.artillexstudios.axenvoy.blockfinder;

import com.artillexstudios.axenvoy.config.impl.EnvoyConfig;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Locale;

public class HeightmapBlockFinder implements BlockFinder {

    @Override
    public Block highestBlockAt(Location location, EnvoyConfig config) {
        if (location == null) {
            return null;
        }

        HeightMap heightMap;
        try {
            heightMap = HeightMap.valueOf(config.HEIGHTMAP.toUpperCase(Locale.ENGLISH));
        } catch (EnumConstantNotPresentException exception) {
            heightMap = HeightMap.MOTION_BLOCKING;
        }

        return location.getWorld().getHighestBlockAt(location, heightMap).getLocation().add(0, 1, 0).getBlock();
    }
}
