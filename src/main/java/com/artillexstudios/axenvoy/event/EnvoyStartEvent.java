package com.artillexstudios.axenvoy.event;

import com.artillexstudios.axenvoy.envoy.Envoy;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class EnvoyStartEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Envoy envoy;

    public EnvoyStartEvent(Envoy envoy) {
        this.envoy = envoy;
    }

    public Envoy getEnvoy() {
        return envoy;
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
