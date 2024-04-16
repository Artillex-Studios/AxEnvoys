package com.artillexstudios.axenvoy.event;

import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.envoy.SpawnedCrate;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class EnvoyCrateCollectEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Player player;
    private final Envoy envoy;
    private final SpawnedCrate crate;

    public EnvoyCrateCollectEvent(Player player, Envoy envoy, SpawnedCrate crate) {
        this.player = player;
        this.envoy = envoy;
        this.crate = crate;
    }

    public Envoy getEnvoy() {
        return envoy;
    }

    public SpawnedCrate getCrate() {
        return crate;
    }

    public Player getPlayer() {
        return player;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
