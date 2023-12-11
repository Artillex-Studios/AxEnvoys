package com.artillexstudios.axenvoy.utils;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axenvoy.envoy.SpawnedCrate;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FallingBlockChecker {
    private static final ConcurrentLinkedQueue<SpawnedCrate> fallingCrates = new ConcurrentLinkedQueue<>();

    public static void start() {
         Scheduler.get().runAsyncTimer(task -> {
            Iterator<SpawnedCrate> crateIterator = fallingCrates.iterator();

            while (crateIterator.hasNext()) {
                SpawnedCrate next = crateIterator.next();
                Entity fallingBlock = next.getFallingBlock();
                Entity vex = next.getVex();
                if (fallingBlock == null || vex == null) continue;
                Location finishLocation = next.getFinishLocation();
                Location currentLocation = vex.getLocation();

                if (next.getVex() != null && (finishLocation.getWorld().equals(currentLocation.getWorld()) && finishLocation.getBlockX() == currentLocation.getBlockX() && finishLocation.getBlockY() == currentLocation.getBlockY() && finishLocation.getBlockZ() == currentLocation.getBlockZ())) {
                    crateIterator.remove();
                    next.setFallingBlock(null);
                    next.setVex(null);
                    Scheduler.get().runAt(fallingBlock.getLocation(), t -> {
                        fallingBlock.remove();
                        vex.remove();
                        next.land(next.getFinishLocation());
                    });
                }
            }

        }, 1, 1);
    }


    public static void addToCheck(@NotNull SpawnedCrate crate) {
        fallingCrates.add(crate);
    }
}
