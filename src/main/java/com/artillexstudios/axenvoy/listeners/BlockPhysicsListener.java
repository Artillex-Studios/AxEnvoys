package com.artillexstudios.axenvoy.listeners;

import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.envoy.Envoys;
import com.artillexstudios.axenvoy.envoy.SpawnedCrate;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class BlockPhysicsListener implements Listener {

    @EventHandler
    public void onBlockPhysicsEvent(@NotNull BlockPhysicsEvent event) {
        Collection<Envoy> envoys = Envoys.getTypes().values();
        if (envoys.isEmpty()) return;

        for (Envoy envoy : envoys) {
            if (!envoy.isActive()) continue;
            if (!envoy.getCenter().getWorld().equals(event.getBlock().getWorld())) continue;
            ArrayList<SpawnedCrate> spawnedCrate = envoy.getSpawnedCrates();
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

    @EventHandler
    public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
        if (!event.getEntity().getPersistentDataContainer().has(SpawnedCrate.FALLING_BLOCK_KEY, PersistentDataType.BYTE)) return;
        event.setCancelled(true);
        event.getEntity().remove();
    }
}
