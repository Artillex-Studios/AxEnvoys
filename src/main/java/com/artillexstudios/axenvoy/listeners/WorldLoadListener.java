package com.artillexstudios.axenvoy.listeners;

import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.envoy.Envoys;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class WorldLoadListener implements Listener {

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        Envoys.reload();
    }

    @EventHandler
    public void onWorldUnloadEvent(WorldUnloadEvent event) {
        for (Envoy envoy : Envoys.getTypes().values()) {
            if (!envoy.isActive()) continue;

            World world = envoy.getCenter().getWorld();
            if (world == null) continue;
            if (event.getWorld().equals(world)) {
                envoy.stop();
            }
        }
    }
}
