package com.artillexstudios.axenvoy.utils;

import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.envoy.SpawnedCrate;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class FallingBlockChecker {
    private static final ObjectArrayList<SpawnedCrate> fallingCrates = new ObjectArrayList<>();

    public FallingBlockChecker() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(AxEnvoyPlugin.getInstance(), () -> {
            Iterator<SpawnedCrate> crateIterator = fallingCrates.iterator();
            while (crateIterator.hasNext()) {
                SpawnedCrate next = crateIterator.next();
                Location location = next.getFallLocation();
                if (next.getFallingBlock() != null && location.equals(next.getFallingBlock().getLocation())) {
                    crateIterator.remove();
                    Bukkit.getScheduler().runTask(AxEnvoyPlugin.getInstance(), () -> {
                        next.land(location);
                        next.getFallingBlock().remove();
                        next.setFallLocation(null);
                        next.setFallingBlock(null);
                    });
                    continue;
                }

                next.setFallLocation(next.getFallingBlock().getLocation());
            }
        }, 0, 5);
    }

    public static void addToCheck(@NotNull SpawnedCrate crate) {
        fallingCrates.add(crate);
    }
}
