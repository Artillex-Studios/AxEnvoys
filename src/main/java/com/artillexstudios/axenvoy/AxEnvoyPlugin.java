package com.artillexstudios.axenvoy;

import com.artillexstudios.axenvoy.commands.Commands;
import com.artillexstudios.axenvoy.config.ConfigManager;
import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.envoy.EnvoyLoader;
import com.artillexstudios.axenvoy.envoy.SpawnedCrate;
import com.artillexstudios.axenvoy.listeners.ActivateFlare;
import com.artillexstudios.axenvoy.listeners.BlockPhysicsListener;
import com.artillexstudios.axenvoy.listeners.CollectionListener;
import com.artillexstudios.axenvoy.listeners.FireworkDamageListener;
import com.artillexstudios.axenvoy.placeholders.Placeholders;
import com.artillexstudios.axenvoy.user.User;
import com.artillexstudios.axenvoy.utils.EditorListener;
import com.artillexstudios.axenvoy.utils.FallingBlockChecker;
import com.artillexstudios.axenvoy.utils.Utils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class AxEnvoyPlugin extends JavaPlugin {
    private static AxEnvoyPlugin instance;
    private boolean placeholderApi;

    public static AxEnvoyPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        ConfigManager.reload();
        new Commands(this);
        this.placeholderApi = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (placeholderApi) {
            new Placeholders().register();
        }

        Bukkit.getOnlinePlayers().forEach(User::new);
        User.listen();
        new FallingBlockChecker();
        Bukkit.getPluginManager().registerEvents(new ActivateFlare(), this);
        if (ConfigManager.getConfig().getBoolean("listen-to-block-physics")) {
            Bukkit.getPluginManager().registerEvents(new BlockPhysicsListener(), this);
        }
        Bukkit.getPluginManager().registerEvents(new CollectionListener(), this);
        Bukkit.getPluginManager().registerEvents(new FireworkDamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new EditorListener(), this);
        new Metrics(this, 19146);

        ConfigManager.getTempData().getKeys().forEach(key -> {
            List<String> remainingCrates = ConfigManager.getTempData().getStringList(String.format("%s.locations", key), new ArrayList<>());
            if (remainingCrates.isEmpty()) return;
            for (String remainingCrate : remainingCrates) {
                Location location = Utils.deserializeLocation(remainingCrate);
                location.getBlock().setType(Material.AIR);
            }

            ConfigManager.getTempData().remove(key.toString());
            try {
                ConfigManager.getTempData().save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            EnvoyLoader.envoys.forEach((name, envoy) -> {
                if (!envoy.isActive()) return;

                for (SpawnedCrate spawnedCrate : envoy.getSpawnedCrates()) {
                    if (spawnedCrate.getHandle().getFlareTicks() == 0) continue;
                    spawnedCrate.tickFlare();
                }
            });
        }, 0, 0);
    }

    @Override
    public void onDisable() {
        for (Envoy envoy : EnvoyLoader.envoys.values()) {
            if (!envoy.isActive()) continue;
            envoy.stop();
        }
    }

    public boolean isPlaceholderApi() {
        return this.placeholderApi;
    }
}
