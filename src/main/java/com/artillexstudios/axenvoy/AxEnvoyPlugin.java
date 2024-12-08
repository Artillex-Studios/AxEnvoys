package com.artillexstudios.axenvoy;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.libs.libby.BukkitLibraryManager;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axenvoy.commands.EnvoyCommand;
import com.artillexstudios.axenvoy.config.impl.Config;
import com.artillexstudios.axenvoy.config.impl.Messages;
import com.artillexstudios.axenvoy.envoy.Crates;
import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.envoy.Envoys;
import com.artillexstudios.axenvoy.envoy.SpawnedCrate;
import com.artillexstudios.axenvoy.libraries.Libraries;
import com.artillexstudios.axenvoy.listeners.BlockPhysicsListener;
import com.artillexstudios.axenvoy.listeners.CollectionListener;
import com.artillexstudios.axenvoy.listeners.FireworkDamageListener;
import com.artillexstudios.axenvoy.listeners.FlareListener;
import com.artillexstudios.axenvoy.listeners.WorldLoadListener;
import com.artillexstudios.axenvoy.placeholders.Placeholders;
import com.artillexstudios.axenvoy.user.User;
import com.artillexstudios.axenvoy.utils.EditorListener;
import com.artillexstudios.axenvoy.utils.FallingBlockChecker;
import com.artillexstudios.axenvoy.utils.Utils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import revxrsal.commands.bukkit.BukkitCommandHandler;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class AxEnvoyPlugin extends AxPlugin {
    public static NamespacedKey MESSAGE_KEY;
    private static AxEnvoyPlugin instance;
    private static Messages MESSAGES;
    private boolean placeholderApi;
    private boolean worldGuard;

    public static AxEnvoyPlugin getInstance() {
        return instance;
    }

    public static Messages getMessages() {
        return MESSAGES;
    }

    @Override
    public void updateFlags(FeatureFlags flags) {
        flags.PACKET_ENTITY_TRACKER_ENABLED.set(true);
        flags.HOLOGRAM_UPDATE_TICKS.set(10L);
    }

    @Override
    public void load() {
        BukkitLibraryManager manager = new BukkitLibraryManager(this);
        manager.addMavenCentral();
        manager.addJitPack();

        for (Libraries value : Libraries.values()) {
            manager.loadLibrary(value.getLibrary());
        }
    }

    @Override
    public void enable() {
        instance = this;
        MESSAGE_KEY = new NamespacedKey(this, "envoy_messages");

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("Enabled PlaceholderAPI hook!");
            this.placeholderApi = true;
            new Placeholders().register();
        }

        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            getLogger().info("Enabled WorldGuard hook!");
            this.worldGuard = true;
        }

        MESSAGES = new Messages("messages.yml");
        reload();

        BukkitCommandHandler handler = BukkitCommandHandler.create(this);

        handler.registerValueResolver(Envoy.class, context -> {
            String envoy = context.popForParameter();

            return Envoys.valueOf(envoy.toLowerCase(Locale.ENGLISH));
        });

        handler.getAutoCompleter().registerParameterSuggestions(Envoy.class, (args, sender, command) -> Envoys.getTypes().keySet());

        handler.register(new EnvoyCommand());

        Bukkit.getOnlinePlayers().forEach(User::new);
        User.listen();
        FallingBlockChecker.start();
        Bukkit.getPluginManager().registerEvents(new FlareListener(), this);

        if (Config.LISTEN_TO_BLOCK_PHYSICS) {
            Bukkit.getPluginManager().registerEvents(new BlockPhysicsListener(), this);
        }

        Bukkit.getPluginManager().registerEvents(new CollectionListener(), this);
        Bukkit.getPluginManager().registerEvents(new FireworkDamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new EditorListener(), this);
        Bukkit.getPluginManager().registerEvents(new WorldLoadListener(), this);
        new Metrics(this, 19146);

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            Envoys.getTypes().forEach((string, envoy) -> {
                if (envoy.getConfig().EVERY.isBlank()) return;
                if (envoy.isActive()) return;
                ZonedDateTime now = ZonedDateTime.now();

                Iterator<Calendar> iterator = envoy.getWarns().iterator();
                while (iterator.hasNext()) {
                    Calendar warn = iterator.next();
                    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(warn.getTimeInMillis()), ZoneId.systemDefault());

                    if (zonedDateTime.getHour() == now.getHour() && zonedDateTime.getMinute() == now.getMinute() && zonedDateTime.getSecond() == now.getSecond()) {
                        iterator.remove();
                        if (!envoy.getConfig().ALERT.isBlank()) {
                            Bukkit.broadcastMessage(StringUtils.formatToString(envoy.getConfig().ALERT.replace("%time%", Utils.fancyTime(envoy.getNext().getTimeInMillis() - Calendar.getInstance().getTimeInMillis(), envoy))));
                        }
                    }
                }

                ZonedDateTime next = ZonedDateTime.ofInstant(Instant.ofEpochMilli(envoy.getNext().getTimeInMillis()), ZoneId.systemDefault());
                if (next.getHour() == now.getHour() && next.getMinute() == now.getMinute() && next.getSecond() == now.getSecond()) {
                    if (envoy.startAttempt()) {
                        return;
                    }

                    envoy.setStartAttempt(true);
                    if (Bukkit.getOnlinePlayers().size() < envoy.getConfig().MIN_PLAYERS) {
                        envoy.updateNext();
                        Bukkit.broadcastMessage(StringUtils.formatToString(envoy.getConfig().NOT_ENOUGH_AUTO_START));
                        return;
                    }

                    if (envoy.isActive()) {
                        envoy.updateNext();
                        return;
                    }

                    Scheduler.get().run(task -> {
                        envoy.start(null);
                    });
                }
            });
        }, 0, 200, TimeUnit.MILLISECONDS);

        Scheduler.get().runTimer(task -> {
            Envoys.getTypes().forEach((name, envoy) -> {
                if (!envoy.isActive()) return;

                for (SpawnedCrate spawnedCrate : envoy.getSpawnedCrates()) {
                    if (spawnedCrate.getHandle().getConfig().FLARE_EVERY == 0 || !spawnedCrate.getHandle().getConfig().FLARE_ENABLED)
                        continue;
                    spawnedCrate.tickFlare();
                }
            });
        }, 1, 1);
    }

    @Override
    public void reload() {
        Config.reload();
        MESSAGES.reload();
        Crates.reload();
        Envoys.reload();
    }

    @Override
    public void disable() {
        for (Envoy envoy : Envoys.getTypes().values()) {
            if (!envoy.isActive()) continue;
            envoy.stop();
        }
    }

    public boolean isPlaceholderApi() {
        return this.placeholderApi;
    }

    public boolean isWorldGuard() {
        return worldGuard;
    }
}
