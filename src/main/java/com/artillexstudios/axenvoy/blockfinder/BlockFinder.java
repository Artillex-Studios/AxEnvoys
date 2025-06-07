package com.artillexstudios.axenvoy.blockfinder;

import com.artillexstudios.axenvoy.config.impl.EnvoyConfig;
import org.bukkit.Location;
import org.bukkit.block.Block;

public interface BlockFinder {

    Block highestBlockAt(Location location, EnvoyConfig config);
}
