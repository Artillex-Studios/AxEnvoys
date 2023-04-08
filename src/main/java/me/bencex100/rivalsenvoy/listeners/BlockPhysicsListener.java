package me.bencex100.rivalsenvoy.listeners;

import me.bencex100.rivalsenvoy.utils.EnvoyHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

public class BlockPhysicsListener implements Listener {

    @EventHandler
    public void onForm(BlockPhysicsEvent e) {
        if (!EnvoyHandler.isActive()) return;
        if (!EnvoyHandler.getCenter().getWorld().equals(e.getBlock().getWorld())) return;
        e.setCancelled(true);
    }
}
