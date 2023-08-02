package com.artillexstudios.axenvoy.listeners;

import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.envoy.EnvoyLoader;
import com.artillexstudios.axenvoy.envoy.SpawnedCrate;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.jetbrains.annotations.NotNull;

public class BlockPhysicsListener implements Listener {

    @EventHandler
    public void onBlockPhysicsEvent(@NotNull BlockPhysicsEvent event) {
        ObjectCollection<Envoy> envoys = EnvoyLoader.envoys.values();
        if (envoys.isEmpty()) return;

        for (Envoy envoy : envoys) {
            if (!envoy.isActive()) continue;
            if (!envoy.getCenter().getWorld().equals(event.getBlock().getWorld())) continue;
            ObjectArrayList<SpawnedCrate> spawnedCrate = envoy.getSpawnedCrates();
            if (spawnedCrate.isEmpty()) continue;
            int spawnedCrateSize = spawnedCrate.size();
            for (int i1 = 0; i1 < spawnedCrateSize; i1++) {
                SpawnedCrate crate = spawnedCrate.get(i1);
                BlockFace[] faces = BlockFace.values();
                int faceSize = faces.length;
                for (int i2 = 0; i2 < faceSize; i2++) {
                    Block relative = crate.getFinishLocation().getBlock().getRelative(faces[i2]);

                    if (relative.getLocation().distanceSquared(event.getSourceBlock().getLocation()) <= 4) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }
}
