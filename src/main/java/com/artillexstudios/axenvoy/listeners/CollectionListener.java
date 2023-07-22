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
    public void onPlayerInteractEvent(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() == Material.AIR) return;

        for (Envoy envoy : EnvoyLoader.envoys) {
            if (!envoy.isActive()) continue;
            for (SpawnedCrate spawnedCrate : envoy.getSpawnedCrates()) {
                if (!spawnedCrate.getFinishLocation().equals(event.getClickedBlock().getLocation())) continue;
                User user = User.USER_MAP.get(event.getPlayer().getUniqueId());
                if (user == null) return;
                event.setCancelled(true);
                event.setUseInteractedBlock(Event.Result.DENY);

                if (user.canCollect(envoy, spawnedCrate.getHandle())) {
                    spawnedCrate.claim(event.getPlayer(), envoy);

                    int cooldown = spawnedCrate.getHandle().isHasCollectionCooldown() ? spawnedCrate.getHandle().getCollectionCooldown() : envoy.getCollectCooldown();
                    if (envoy.isCollectGlobalCooldown()) {
                        cooldown = envoy.getCollectCooldown();
                    }

                    user.addCrateCooldown(spawnedCrate.getHandle(), cooldown, envoy);
                } else {
                    event.getPlayer().sendMessage(String.format("%s%s", StringUtils.format(envoy.getMessage("prefix")), envoy.getMessage("cooldown", event.getPlayer()).replace("%crate%", StringUtils.format(spawnedCrate.getHandle().getDisplayName())).replace("%cooldown%", String.valueOf((user.getCooldown(envoy, spawnedCrate.getHandle()) - System.currentTimeMillis()) / 1000))));
                }
                return;
            }
        }
    }
}
