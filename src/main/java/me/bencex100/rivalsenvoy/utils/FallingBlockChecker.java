package me.bencex100.rivalsenvoy.utils;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.bencex100.rivalsenvoy.RivalsEnvoy;
import me.bencex100.rivalsenvoy.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

import static me.bencex100.rivalsenvoy.listeners.CollectionListener.cratesSpawned;
import static me.bencex100.rivalsenvoy.listeners.FallingBlockListener.fallingBlocks;
import static me.bencex100.rivalsenvoy.utils.EnvoyHandler.crates;
import static me.bencex100.rivalsenvoy.utils.Utils.topBlock;

public class FallingBlockChecker {
    private final YamlDocument messages = Config.getCnf("messages");
    private final YamlDocument config = Config.getCnf("config");
    public void checkIfAlive(Entity falling) {
        final Location[] lastLoc = {falling.getLocation()};
        new BukkitRunnable() {
            @Override
            public void run() {
                if (lastLoc[0].equals(falling.getLocation())) {
                    if (fallingBlocks.get(falling) == null) {
                        this.cancel();
                        return;
                    }
                    Location l = falling.getLocation().getBlock().getLocation();
                    if (topBlock(l) != null) {
                        fallingBlocks.get(falling).landAt(topBlock(l));
                    }
                    fallingBlocks.remove(falling);


                    if (fallingBlocks.size() == 0 && !EnvoyHandler.bcd) {
                        EnvoyHandler.bcd = true;
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.sendRichMessage(config.getString("prefix") + messages.getString("broadcast.started").replace("%amount%", Integer.toString(crates.size())));
                        }
                        cratesSpawned = crates.size();
                    }
                    this.cancel();
                }
                lastLoc[0] = falling.getLocation();
            }
        }.runTaskTimer(RivalsEnvoy.getInstance(), 5L, 5L);
    }
}
