package me.bencex100.rivalsenvoy.listeners;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.bencex100.rivalsenvoy.config.Config;
import me.bencex100.rivalsenvoy.utils.Crate;
import me.bencex100.rivalsenvoy.utils.EnvoyHandler;
import me.bencex100.rivalsenvoy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

public class CollectionListener implements Listener {
    private final YamlDocument config = Config.getCnf("config");
    private final YamlDocument messages = Config.getCnf("messages");
    HashMap<Player, Long> cd = new HashMap<>();
    public static int cratesSpawned;

    @EventHandler
    public void onCollect(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;

        for (Map.Entry<Location, Crate> entry : EnvoyHandler.crates.entrySet()) {

            if (e.getClickedBlock().getLocation().equals(entry.getKey())) {
                if (cd.containsKey(e.getPlayer())) {
                    if (System.currentTimeMillis() - cd.get(e.getPlayer()) < config.getLong("collect-cooldown-in-seconds") * 1000) {
                        e.getPlayer().sendRichMessage(config.getString("prefix") + messages.getString("error.cooldown-message").replace("%time%", Utils.fancyTime(config.getLong("collect-cooldown-in-seconds") * 1000 - System.currentTimeMillis() + cd.get(e.getPlayer()))));
                        return;
                    }
                }
                cd.put(e.getPlayer(), System.currentTimeMillis());
                --cratesSpawned;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendRichMessage(config.getString("prefix") + messages.getString("broadcast.found").replace("%amount%", Integer.toString(cratesSpawned)).replace("%player%", e.getPlayer().getName()));
                }
                if (cratesSpawned == 0) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendRichMessage(config.getString("prefix") + messages.getString("broadcast.ended"));
                    }
                }
                entry.getValue().collectCrate(e.getPlayer());
                return;
            }
        }
    }
}
