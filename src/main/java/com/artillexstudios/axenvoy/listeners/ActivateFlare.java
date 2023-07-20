package com.artillexstudios.axenvoy.listeners;

import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.envoy.EnvoyLoader;
import com.artillexstudios.axenvoy.utils.StringUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
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
    private void onPlayerInteractEvent(@NotNull PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getItem() == null) return;
        if (e.getItem().getItemMeta() == null) return;
        final ItemMeta meta = e.getItem().getItemMeta();
        final PersistentDataContainer container = meta.getPersistentDataContainer();
        final NamespacedKey key = new NamespacedKey(AxEnvoyPlugin.getInstance(), "rivalsenvoy");
        if (!container.has(key, PersistentDataType.STRING)) return;

        for (Envoy envoy : EnvoyLoader.envoys) {
            if (envoy.getName().equals(container.get(key, PersistentDataType.STRING))) {
                if (!envoy.isFlareEnabled()) {
                    e.getPlayer().sendMessage(StringUtils.toString(envoy.getMessage("prefix").append(envoy.getMessage("flare-disabled"))));
                    return;
                }

                if (envoy.isActive()) {
                    e.getPlayer().sendMessage(StringUtils.toString(envoy.getMessage("prefix").append(envoy.getMessage("already-active"))));
                    return;
                }

                if (envoy.getCenter() == null) {
                    return;
                }

                if (envoy.start(e.getPlayer())) {
                    if (e.getItem().getAmount() > 1) {
                        e.getItem().setAmount(e.getItem().getAmount() - 1);
                    } else {
                        e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                    }
                }

                return;
            }
        }
    }
}
