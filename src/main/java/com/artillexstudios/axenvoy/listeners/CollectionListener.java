package com.artillexstudios.axenvoy.listeners;

import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.envoy.EnvoyLoader;
import com.artillexstudios.axenvoy.envoy.SpawnedCrate;
import com.artillexstudios.axenvoy.user.User;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class CollectionListener implements Listener {

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getClickedBlock() == null) return;
        if (e.getClickedBlock().getType() == Material.AIR) return;

        for (Envoy envoy : EnvoyLoader.envoys) {
            if (!envoy.isActive()) continue;
            for (SpawnedCrate spawnedCrate : envoy.getSpawnedCrates()) {
                if (!spawnedCrate.getFinishLocation().equals(e.getClickedBlock().getLocation())) continue;
                User user = User.USER_MAP.get(e.getPlayer().getUniqueId());
                if (user == null) return;

                if (user.canCollect(spawnedCrate.getHandle())) {
                    System.out.println("collectingg");
                    spawnedCrate.claim(e.getPlayer(), envoy);
                    user.addCooldown(spawnedCrate.getHandle(), spawnedCrate.getHandle().isHasCollectionCooldown() ? spawnedCrate.getHandle().getCollectionCooldown() : envoy.getCollectCooldown());
                    return;
                }
            }
        }
    }
}
