package me.bencex100.rivalsenvoy.utils;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.bencex100.rivalsenvoy.RivalsEnvoy;
import me.bencex100.rivalsenvoy.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static me.bencex100.rivalsenvoy.utils.Utils.topBlock;

public class EnvoyHandler {
    private final YamlDocument config = Config.getCnf("config");
    private final YamlDocument data = Config.getCnf("data");
    private final YamlDocument messages = Config.getCnf("messages");
    static boolean active = false;
    static boolean bcd = false;
    public static HashMap<Location, Crate> crates = new HashMap<>();
    Long cd;

    public void startEnvoy() {
        if (cd != null) {
            if (System.currentTimeMillis() - cd < 1500) {
                return;
            }
        }
        cd = System.currentTimeMillis();
        crates.forEach((key, value) -> value.collectCrate(null));
        crates.clear();
        bcd = false;
        active = true;
        HashMap<String, Double> map = new HashMap<>();
        for (Object j : config.getSection("crates").getKeys()) {
            map.put(j.toString(), config.getDouble("crates." + j + ".spawn-chance"));
        }
        for (int i = 0; i < config.getInt("random-locations.crates-amount"); i++) {
            Location loc = Utils.deserializeLocation(data, "data.center").getBlock().getLocation();
            int tries = 300 + config.getInt("random-locations.crates-amount") * 10;
            do {
                loc.setX(Double.parseDouble(String.valueOf(Math.round(loc.getX()) + ThreadLocalRandom.current().nextInt(config.getInt("random-locations.distance") * - 1, config.getInt("random-locations.distance")))));
                loc.setZ(Double.parseDouble(String.valueOf(Math.round(loc.getZ()) + ThreadLocalRandom.current().nextInt(config.getInt("random-locations.distance") * - 1, config.getInt("random-locations.distance")))));
                tries--;
                if (tries < 0) {
                    Bukkit.getLogger().warning("Error while starting event, can't find any locations!");
                    return;
                }
            } while (topBlock(loc) == null);
            loc = topBlock(loc);
            Crate cr = new Crate(loc, Utils.randomValue(map));
            cr.load();
        }
    }
}
