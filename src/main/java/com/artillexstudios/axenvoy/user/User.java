package com.artillexstudios.axenvoy.user;

import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.envoy.Crate;
import com.artillexstudios.axenvoy.envoy.Envoy;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.checkerframework.common.value.qual.EnumVal;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class User {
    public static final Object2ObjectArrayMap<UUID, User> USER_MAP = new Object2ObjectArrayMap<>();
    private final Object2LongArrayMap<Crate> crateCooldown = new Object2LongArrayMap<>();
    private final Object2LongArrayMap<Envoy> flareCooldown = new Object2LongArrayMap<>();
    private final Player player;

    public User(@NotNull Player player) {
        this.player = player;
        USER_MAP.put(player.getUniqueId(), this);
    }

    public void clear() {
        crateCooldown.clear();
        flareCooldown.clear();
    }

    public void addCooldown(Crate crate, long seconds) {
        crateCooldown.put(crate, System.currentTimeMillis() + (seconds * 1000));
    }

    public void addCooldown(Envoy envoy, long seconds) {
        flareCooldown.put(envoy, System.currentTimeMillis() + (seconds * 1000));
    }

    public boolean canCollect(Crate crate) {
        long cooldown = crateCooldown.getOrDefault(crate, System.currentTimeMillis());
        if (cooldown <= System.currentTimeMillis()) {
            return true;
        }

        return false;
    }

    public boolean canUseFlare(Envoy envoy) {
        long cooldown = flareCooldown.getOrDefault(envoy, System.currentTimeMillis());
        if (cooldown <= System.currentTimeMillis()) {
            return true;
        }

        return false;
    }

    public long getCooldown(Crate crate) {
        return this.crateCooldown.getOrDefault(crate, System.currentTimeMillis());
    }

    public static void listen() {
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerJoinEvent(@NotNull PlayerJoinEvent event) {
                new User(event.getPlayer());
            }

            @EventHandler
            public void onPlayerQuitEvent(@NotNull PlayerQuitEvent event) {
                USER_MAP.get(event.getPlayer().getUniqueId()).clear();
                USER_MAP.remove(event.getPlayer().getUniqueId());
            }
        }, AxEnvoyPlugin.getInstance());
    }
}
