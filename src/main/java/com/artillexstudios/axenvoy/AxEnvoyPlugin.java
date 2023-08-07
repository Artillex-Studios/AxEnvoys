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
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class AxEnvoyPlugin extends JavaPlugin {
    private static AxEnvoyPlugin instance;
    private boolean placeholderApi;
    private boolean decentHolograms;

    public static AxEnvoyPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        if (Bukkit.getPluginManager().getPlugin("DecentHolograms") != null) {
            getLogger().info("Enabled DecentHolograms hook!");
            this.decentHolograms = true;
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("Enabled PlaceholderAPI hook!");
            this.placeholderApi = true;
            new Placeholders().register();
        }

        new Commands(this);
        ConfigManager.reload();

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

        //ConfigManager.getTempData().getKeys().forEach(key -> {
//            List<String> remainingCrates = ConfigManager.getTempData().getStringList(String.format("%s.locations", key), new ArrayList<>());
//            if (remainingCrates.isEmpty()) return;
//            for (String remainingCrate : remainingCrates) {
//                Location location = Utils.deserializeLocation(remainingCrate);
//                location.getBlock().setType(Material.AIR);
//            }
//
//            ConfigManager.getTempData().remove(key.toString());
//            try {
//                ConfigManager.getTempData().save();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            EnvoyLoader.envoys.forEach((string, envoy) -> {
                if (envoy.getEvery().isEmpty()) return;
                if (envoy.isActive()) return;
                Calendar now = Calendar.getInstance();
                now.clear(Calendar.MILLISECOND);

                ObjectListIterator<Calendar> iterator = envoy.getWarns().iterator();
                while (iterator.hasNext()) {
                    Calendar warn = iterator.next();
                    Calendar timeCheck = Calendar.getInstance();
                    timeCheck.setTimeInMillis(warn.getTimeInMillis());
                    timeCheck.clear(Calendar.MILLISECOND);

                    if (timeCheck.compareTo(now) == 0) {
                        iterator.remove();
                        Bukkit.broadcastMessage(envoy.getMessage("alert").replace("%time%", Utils.fancyTime(envoy.getNext().getTimeInMillis() - Calendar.getInstance().getTimeInMillis())));
                    }
                }

                Calendar next = Calendar.getInstance();
                next.setTimeInMillis(envoy.getNext().getTimeInMillis());
                next.clear(Calendar.MILLISECOND);

                if (next.compareTo(now) <= 0) {
                    if (Bukkit.getOnlinePlayers().size() < envoy.getMinPlayers()) {
                        envoy.updateNext();
                        Bukkit.broadcastMessage(envoy.getMessage("not-enough-autostart"));
                        return;
                    }

                    if (envoy.isActive()) {
                        envoy.updateNext();
                        return;
                    }

                    Bukkit.getScheduler().runTask(AxEnvoyPlugin.getInstance(), () -> {
                        envoy.start(null);
                    });
                }
            });
        }, 0, 200, TimeUnit.MILLISECONDS);

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

    public boolean isDecentHolograms() {
        return decentHolograms;
    }
}
