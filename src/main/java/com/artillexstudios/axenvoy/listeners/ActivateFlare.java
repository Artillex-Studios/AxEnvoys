package com.artillexstudios.axenvoy.listeners;

import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.envoy.EnvoyLoader;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class ActivateFlare implements Listener {

    @EventHandler
    private void onPlayerInteractEvent(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getItem() == null) return;
        if (event.getItem().getItemMeta() == null) return;
        final ItemMeta meta = event.getItem().getItemMeta();
        final PersistentDataContainer container = meta.getPersistentDataContainer();
        final NamespacedKey key = new NamespacedKey(AxEnvoyPlugin.getInstance(), "rivalsenvoy");
        if (!container.has(key, PersistentDataType.STRING)) return;

        for (Envoy envoy : EnvoyLoader.envoys) {
            if (envoy.getName().equals(container.get(key, PersistentDataType.STRING))) {
                if (!envoy.isFlareEnabled()) {
                    event.getPlayer().sendMessage(String.format("%s%s", envoy.getMessage("prefix"), envoy.getMessage("flare-disabled")));
                    return;
                }

                if (envoy.isActive()) {
                    event.getPlayer().sendMessage(String.format("%s%s", envoy.getMessage("prefix"), envoy.getMessage("already-active")));
                    return;
                }

                if (envoy.getCenter() == null) {
                    return;
                }

                if (envoy.start(event.getPlayer())) {
                    if (event.getItem().getAmount() > 1) {
                        event.getItem().setAmount(event.getItem().getAmount() - 1);
                    } else {
                        event.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                    }
                }

                return;
            }
        }
    }
}
