package com.artillexstudios.axenvoy.user;

import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.envoy.Crate;
import com.artillexstudios.axenvoy.envoy.Envoy;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.UUID;

public class User {
    public static final Object2ObjectArrayMap<UUID, User> USER_MAP = new Object2ObjectArrayMap<>();
    private final ObjectArrayList<CrateCooldown> crateCooldowns = new ObjectArrayList<>();
    private final Object2LongArrayMap<Envoy> flareCooldown = new Object2LongArrayMap<>();
    private final Player player;
    private Envoy editor = null;

    public User(@NotNull Player player) {
        this.player = player;
        USER_MAP.put(player.getUniqueId(), this);
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

    public void clear() {
        crateCooldowns.clear();
        flareCooldown.clear();
    }

    public void addCrateCooldown(Crate crate, long seconds, Envoy envoy) {
        if (!envoy.isCollectGlobalCooldown()) {
            crateCooldowns.add(new CrateCooldown(envoy, crate, System.currentTimeMillis() + (seconds * 1000)));
        } else {
            crateCooldowns.add(new CrateCooldown(envoy, null, System.currentTimeMillis() + (seconds * 1000)));
        }
    }

    public void addFlareCooldown(Envoy envoy, long seconds) {
        flareCooldown.put(envoy, System.currentTimeMillis() + (seconds * 1000));
    }

    public boolean canCollect(Envoy envoy, Crate crate) {
        return (getCooldown(envoy, crate) - System.currentTimeMillis()) / 1000 == 0;
    }

    public boolean canUseFlare(Envoy envoy) {
        long cooldown = flareCooldown.getOrDefault(envoy, System.currentTimeMillis());
        return cooldown <= System.currentTimeMillis();
    }

    public long getCooldown(Envoy envoy, Crate crate) {
        Iterator<CrateCooldown> cooldownIterator = crateCooldowns.iterator();
        while (cooldownIterator.hasNext()) {
            CrateCooldown next = cooldownIterator.next();
            if (next.envoy != envoy) continue;
            if (next.end <= System.currentTimeMillis()) {
                cooldownIterator.remove();
                continue;
            }

            if (envoy.isCollectGlobalCooldown()) {
                return next.end;
            }

            if (next.crate != crate) continue;

            return next.end;
        }

        return System.currentTimeMillis();
    }

    public void setEditor(Envoy value) {
        this.editor = value;
    }

    public Envoy getEditor() {
        return editor;
    }

    public Player getPlayer() {
        return player;
    }
}
