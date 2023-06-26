package me.bencex100.rivalsenvoy.listeners;

import me.bencex100.rivalsenvoy.envoy.SpawnedCrate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.util.HashMap;

public class FallingBlockListener implements Listener {
    public static final HashMap<Entity, SpawnedCrate> fallingBlocks = new HashMap<>();

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        if (!(e.getEntity() instanceof FallingBlock)) return;

        if (fallingBlocks.containsKey(e.getEntity())) {
            e.setCancelled(true);
            fallingBlocks.get(e.getEntity()).land();
            fallingBlocks.remove(e.getEntity());
        }
    }
}
