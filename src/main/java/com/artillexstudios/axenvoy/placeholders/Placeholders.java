package com.artillexstudios.axenvoy.placeholders;

import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.envoy.EnvoyLoader;
import com.artillexstudios.axenvoy.utils.StringUtils;
import com.artillexstudios.axenvoy.utils.Utils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;

public class Placeholders extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "axenvoy";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ArtillexStudios";
    }

    @Override
    public @NotNull String getVersion() {
        return AxEnvoyPlugin.getInstance().getDescription().getVersion();
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        String[] args = params.split("_");
        String envoyName = args[1];
        Envoy envoy = EnvoyLoader.envoys.get(envoyName);
        if (envoy == null) return "";

        switch (args[0]) {
            case "active" -> {
                if (envoy.isActive()) {
                    return StringUtils.format(envoy.getMessage("placeholder.running"));
                }

                return StringUtils.format(envoy.getMessage("placeholder.not-running"));
            }
            case "remaining" -> {
                if (!envoy.isActive()) {
                    return StringUtils.format(envoy.getMessage("placeholder.not-running"));
                }

                return StringUtils.format(envoy.getMessage("placeholder.remaining").replace("%remaining%", String.valueOf(envoy.getSpawnedCrates().size())));
            }
            case "time_left" -> {
                if (!envoy.isActive()) {
                    return StringUtils.format(envoy.getMessage("placeholder.not-running"));
                }

                return StringUtils.format(envoy.getMessage("placeholder.remaining-time").replace("%time%", Utils.fancyTime((envoy.getStartTime() + envoy.getTimeoutTime() * 1000L) - System.currentTimeMillis())));
            }
            case "next_start" -> {
                if (envoy.isActive() || envoy.getNext() == null) {
                    return StringUtils.format(envoy.getMessage("placeholder.running"));
                }

                return StringUtils.format(envoy.getMessage("placeholder.until-next").replace("%time%", Utils.fancyTime(envoy.getNext().getTimeInMillis() - Calendar.getInstance().getTimeInMillis())));
            }
        }

        return "";
    }
}
