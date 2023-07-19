package com.artillexstudios.axenvoy.listeners;

import com.artillexstudios.axenvoy.envoy.SpawnedCrate;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class FireworkDamageListener implements Listener {

    @EventHandler
    public void onFireworkDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Firework fw)) return;
        if (!fw.getPersistentDataContainer().has(SpawnedCrate.FIREWORK_KEY, PersistentDataType.BYTE)) return;

        e.setCancelled(true);
    }
}
