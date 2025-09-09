package com.artillexstudios.axenvoy.user;

import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.envoy.CrateType;
import com.artillexstudios.axenvoy.envoy.Envoy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class User {
    public static final HashMap<UUID, User> USER_MAP = new HashMap<>();
    private final ArrayList<CrateCooldown> crateCooldowns = new ArrayList<>();
    private final ArrayList<CrateCooldown> damageCooldowns = new ArrayList<>();
    private final HashMap<Envoy, Long> flareCooldown = new HashMap<>();
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
                User user = USER_MAP.remove(event.getPlayer().getUniqueId());
                if (user != null) {
                    user.clear();
                }
            }
        }, AxEnvoyPlugin.getInstance());
    }

    public void clear() {
        crateCooldowns.clear();
        flareCooldown.clear();
    }

    public void addCrateCooldown(CrateType crateType, long seconds, Envoy envoy) {
        if (!envoy.getConfig().COLLECT_GLOBAL_COOLDOWN) {
            crateCooldowns.add(new CrateCooldown(envoy, crateType, System.currentTimeMillis() + (seconds * 1000)));
        } else {
            crateCooldowns.add(new CrateCooldown(envoy, null, System.currentTimeMillis() + (seconds * 1000)));
        }
    }

    public void addFlareCooldown(Envoy envoy, long seconds) {
        flareCooldown.put(envoy, System.currentTimeMillis() + (seconds * 1000));
    }

    public boolean canCollect(Envoy envoy, CrateType crateType) {
        return (getCollectCooldown(envoy, crateType) - System.currentTimeMillis()) / 1000 == 0;
    }

    public boolean canDamage(Envoy envoy, CrateType crateType) {
        return (getDamageCooldown(envoy, crateType) - System.currentTimeMillis()) / 50 == 0;
    }

    public boolean canUseFlare(Envoy envoy) {
        long cooldown = flareCooldown.getOrDefault(envoy, System.currentTimeMillis());
        return cooldown <= System.currentTimeMillis();
    }

    public void addDamageCooldown(CrateType crateType, long ticks, Envoy envoy) {
        damageCooldowns.add(new CrateCooldown(envoy, crateType, System.currentTimeMillis() + (ticks * 50)));
    }


    public long getDamageCooldown(Envoy envoy, CrateType crateType) {
        Iterator<CrateCooldown> cooldownIterator = damageCooldowns.iterator();
        while (cooldownIterator.hasNext()) {
            CrateCooldown next = cooldownIterator.next();
            if (next.envoy != envoy) continue;
            if (next.end <= System.currentTimeMillis()) {
                cooldownIterator.remove();
                continue;
            }

            if (next.crateType != crateType) continue;

            return next.end;
        }

        return System.currentTimeMillis();
    }

    public long getCollectCooldown(Envoy envoy, CrateType crateType) {
        Iterator<CrateCooldown> cooldownIterator = crateCooldowns.iterator();
        while (cooldownIterator.hasNext()) {
            CrateCooldown next = cooldownIterator.next();
            if (next.envoy != envoy) continue;
            if (next.end <= System.currentTimeMillis()) {
                cooldownIterator.remove();
                continue;
            }

            if (envoy.getConfig().COLLECT_GLOBAL_COOLDOWN) {
                return next.end;
            }

            if (next.crateType != crateType) continue;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;

        if (!getPlayer().equals(user.getPlayer())) return false;
        return getEditor() != null ? getEditor().equals(user.getEditor()) : user.getEditor() == null;
    }

    @Override
    public int hashCode() {
        int result = getPlayer().hashCode();
        result = 31 * result + (getEditor() != null ? getEditor().hashCode() : 0);
        return result;
    }
}
