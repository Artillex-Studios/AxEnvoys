package me.bencex100.rivalsenvoy.listeners;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.bencex100.rivalsenvoy.config.ConfigManager;
import me.bencex100.rivalsenvoy.envoy.Crate;
import me.bencex100.rivalsenvoy.envoy.EnvoyHandler;
import me.bencex100.rivalsenvoy.utils.ColorUtils;
import me.bencex100.rivalsenvoy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;

public class CollectionListener implements Listener {
    public static int cratesSpawned;
    private final YamlDocument config = ConfigManager.getCnf("config");
    private final YamlDocument messages = ConfigManager.getCnf("messages");
    private final HashMap<Player, Long> cd = new HashMap<>();

    @EventHandler
    public void onCollect(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getClickedBlock() == null) return;
        if (e.getClickedBlock().getType() == Material.AIR) return;
        if (!(EnvoyHandler.crates.size() > 0)) return;

        for (Map.Entry<Location, Crate> entry : EnvoyHandler.crates.entrySet()) {

            if (!e.getClickedBlock().getLocation().equals(entry.getKey())) continue;
            e.setCancelled(true);

            if (cd.containsKey(e.getPlayer()) && System.currentTimeMillis() - cd.get(e.getPlayer()) < config.getLong("collect-cooldown-in-seconds") * 1000) {
                e.getPlayer().sendMessage(ColorUtils.deserialize(config.getString("prefix") + messages.getString("error.cooldown-message").replace("%time%", Long.toString(config.getLong("collect-cooldown-in-seconds") - Math.round((System.currentTimeMillis() - cd.get(e.getPlayer())) / 1000f)))));
                return;
            }

            cd.put(e.getPlayer(), System.currentTimeMillis());
            entry.getValue().collectCrate(e.getPlayer());
            --cratesSpawned;

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(ColorUtils.deserialize(config.getString("prefix") + messages.getString("broadcast.found").replace("%amount%", Integer.toString(cratesSpawned)).replace("%player%", e.getPlayer().getName())));
            }

            if (cratesSpawned != 0) continue;

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(ColorUtils.deserialize(config.getString("prefix") + messages.getString("broadcast.ended")));
            }

            return;
        }
    }
}
