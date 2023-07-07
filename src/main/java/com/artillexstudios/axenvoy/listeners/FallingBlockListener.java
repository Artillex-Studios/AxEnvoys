package com.artillexstudios.axenvoy.listeners;

import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.envoy.EnvoyLoader;
import com.artillexstudios.axenvoy.envoy.SpawnedCrate;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.jetbrains.annotations.NotNull;

public class FallingBlockListener implements Listener {

    @EventHandler
    public void onEntityChangeBlock(@NotNull EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof FallingBlock)) return;

        for (Envoy envoy : EnvoyLoader.envoys) {
            if (!envoy.isActive()) continue;
            for (SpawnedCrate spawnedCrate : envoy.getSpawnedCrates()) {
                if (spawnedCrate.getFallingBlock().equals(event.getEntity())) {
                    spawnedCrate.getFallingBlock().remove();
                    spawnedCrate.land(spawnedCrate.getFallLocation());
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
