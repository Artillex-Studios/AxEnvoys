package com.artillexstudios.axenvoy.listeners;

import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class FireworkDamageListener implements Listener {

    @EventHandler
    public void onFireworkDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Firework fw)) return;
        if (!fw.hasMetadata("axenvoy")) return;

        e.setCancelled(true);
    }
}
