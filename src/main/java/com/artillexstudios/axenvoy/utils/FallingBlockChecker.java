package com.artillexstudios.axenvoy.utils;

import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.envoy.SpawnedCrate;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class FallingBlockChecker {
    private static final ObjectArrayList<SpawnedCrate> fallingCrates = new ObjectArrayList<>();

    public FallingBlockChecker() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(AxEnvoyPlugin.getInstance(), () -> {
            Iterator<SpawnedCrate> crateIterator = fallingCrates.iterator();
            while (crateIterator.hasNext()) {
                SpawnedCrate next = crateIterator.next();
                Entity fallingBlock = next.getFallingBlock();
                if (fallingBlock == null) continue;
                Location finishLocation = next.getFinishLocation();
                Location currentLocation = fallingBlock.getLocation();

                if (next.getFallingBlock() != null && (finishLocation.getWorld().equals(currentLocation.getWorld()) && finishLocation.getBlockX() == currentLocation.getBlockX() && finishLocation.getBlockY() == currentLocation.getBlockY() && finishLocation.getBlockZ() == currentLocation.getBlockZ())) {
                    crateIterator.remove();
                    next.setFallingBlock(null);
                    Bukkit.getScheduler().runTask(AxEnvoyPlugin.getInstance(), () -> {
                        fallingBlock.remove();
                        next.land(next.getFinishLocation());
                    });
                }
            }
        }, 0, 0);
    }

    public static void addToCheck(@NotNull SpawnedCrate crate) {
        fallingCrates.add(crate);
    }
}
