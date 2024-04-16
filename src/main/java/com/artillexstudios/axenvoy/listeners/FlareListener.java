package com.artillexstudios.axenvoy.listeners;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.config.impl.Config;
import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.envoy.Envoys;
import com.artillexstudios.axenvoy.locale.LocaleManager;
import com.artillexstudios.axenvoy.locale.LocaleString;
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

public class FlareListener implements Listener {
    public static final NamespacedKey KEY = new NamespacedKey(AxEnvoyPlugin.getInstance(), "rivalsenvoy");

    @EventHandler
    private void onPlayerInteractEvent(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getItem() == null) return;
        final ItemMeta meta = event.getItem().getItemMeta();
        if (meta == null) return;
        final PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(KEY, PersistentDataType.STRING)) return;

        String envoyName = container.get(KEY, PersistentDataType.STRING);
        if (envoyName == null) return;
        Envoy envoy = Envoys.valueOf(envoyName);
        if (envoy == null) return;

        if (!envoy.getConfig().FLARE_ENABLED) {
            event.getPlayer().sendMessage(StringUtils.formatToString(Config.PREFIX + LocaleManager.getMessage(LocaleString.FLARE_DISABLED)));
            return;
        }

        if (envoy.isActive()) {
            event.getPlayer().sendMessage(StringUtils.formatToString(Config.PREFIX + LocaleManager.getMessage(LocaleString.EVENT_ALREADY_RUNNING)));
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
    }
}
