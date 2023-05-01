package me.bencex100.rivalsenvoy.utils;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.bencex100.rivalsenvoy.RivalsEnvoy;
import me.bencex100.rivalsenvoy.config.Config;
import me.bencex100.rivalsenvoy.envoy.EnvoyHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import static me.bencex100.rivalsenvoy.listeners.CollectionListener.cratesSpawned;
import static me.bencex100.rivalsenvoy.listeners.FallingBlockListener.fallingBlocks;
import static me.bencex100.rivalsenvoy.envoy.EnvoyHandler.crates;
import static me.bencex100.rivalsenvoy.utils.Utils.topBlock;

public class FallingBlockChecker {
    private final YamlDocument messages = Config.getCnf("messages");
    private final YamlDocument config = Config.getCnf("config");

    public void checkIfAlive(Entity falling) {
        new BukkitRunnable() {
            Location lastLoc = falling.getLocation();

            @Override
            public void run() {
                if (lastLoc.equals(falling.getLocation()) && fallingBlocks.get(falling) != null) {
                    fallingBlocks.get(falling).land();
                    fallingBlocks.remove(falling);
                    this.cancel();

                    if (fallingBlocks.size() == 0 && !EnvoyHandler.bcd) {
                        EnvoyHandler.bcd = true;
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.sendRichMessage(config.getString("prefix") + messages.getString("broadcast.started").replace("%amount%", Integer.toString(crates.size())));
                        }
                        cratesSpawned = crates.size();
                    }
                    return;
                }
                lastLoc = falling.getLocation();
            }
        }.runTaskTimer(RivalsEnvoy.getInstance(), 5L, 5L);
    }
}
