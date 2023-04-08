package me.bencex100.rivalsenvoy.listeners;

import me.bencex100.rivalsenvoy.utils.EnvoyHandler;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

public class BlockPhysicsListener implements Listener {

    @EventHandler
    public void onForm(BlockPhysicsEvent e) {
        if (EnvoyHandler.isActive()) {
            if (e.getBlock().getType() == Material.OCHRE_FROGLIGHT || e.getBlock().getType() == Material.PEARLESCENT_FROGLIGHT || e.getBlock().getType() == Material.VERDANT_FROGLIGHT) {
                if (EnvoyHandler.getCenter().getWorld().equals(e.getBlock().getWorld())) {
                    e.setCancelled(true);
                }
            }
        }
    }
}
