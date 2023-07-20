package com.artillexstudios.axenvoy.listeners;

import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.envoy.EnvoyLoader;
import com.artillexstudios.axenvoy.envoy.SpawnedCrate;
import com.artillexstudios.axenvoy.user.User;
import com.artillexstudios.axenvoy.utils.StringUtils;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

public class CollectionListener implements Listener {

    @EventHandler
    public void onPlayerInteractEvent(@NotNull PlayerInteractEvent e) {
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
                e.setCancelled(true);
                e.setUseInteractedBlock(Event.Result.DENY);

                if (user.canCollect(envoy, spawnedCrate.getHandle())) {
                    spawnedCrate.claim(e.getPlayer(), envoy);

                    int cooldown = spawnedCrate.getHandle().isHasCollectionCooldown() ? spawnedCrate.getHandle().getCollectionCooldown() : envoy.getCollectCooldown();
                    if (envoy.isCollectGlobalCooldown()) {
                        cooldown = envoy.getCollectCooldown();
                    }

                    user.addCrateCooldown(spawnedCrate.getHandle(), cooldown, envoy);
                } else {
                    e.getPlayer().sendMessage(String.format("%s%s", envoy.getMessage("prefix"), envoy.getMessage("cooldown").replace("%crate%", StringUtils.format(spawnedCrate.getHandle().getDisplayName())).replace("%cooldown%", String.valueOf((user.getCooldown(envoy, spawnedCrate.getHandle()) - System.currentTimeMillis()) / 1000))));
                }
                return;
            }
        }
    }
}
