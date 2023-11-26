package com.artillexstudios.axenvoy.placeholders;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.envoy.Envoys;
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
        Envoy envoy = Envoys.getTypes().get(envoyName);
        if (envoy == null) return "";

        switch (args[0]) {
            case "active" -> {
                if (envoy.isActive()) {
                    return StringUtils.formatToString(envoy.getConfig().PLACEHOLDER_RUNNING);
                }

                return StringUtils.formatToString(envoy.getConfig().PLACEHOLDER_NOT_RUNNING);
            }
            case "remaining" -> {
                if (!envoy.isActive()) {
                    return StringUtils.formatToString(envoy.getConfig().PLACEHOLDER_NOT_RUNNING);
                }

                return StringUtils.formatToString(envoy.getConfig().PLACEHOLDER_REMAINING.replace("%remaining%", String.valueOf(envoy.getSpawnedCrates().size())));
            }
            case "timeleft" -> {
                if (!envoy.isActive()) {
                    return StringUtils.formatToString(envoy.getConfig().PLACEHOLDER_NOT_RUNNING);
                }

                return StringUtils.formatToString(envoy.getConfig().PLACEHOLDER_REMAINING_TIME.replace("%time%", Utils.fancyTime((envoy.getStartTime() + envoy.getConfig().TIMEOUT_TIME * 1000L) - System.currentTimeMillis())));
            }
            case "nextstart" -> {
                if (envoy.isActive() || envoy.getNext() == null) {
                    return StringUtils.formatToString(envoy.getConfig().PLACEHOLDER_RUNNING);
                }

                return StringUtils.formatToString(envoy.getConfig().PLACEHOLDER_UNTIL_NEXT.replace("%time%", Utils.fancyTime(envoy.getNext().getTimeInMillis() - Calendar.getInstance().getTimeInMillis())));
            }
        }

        return "";
    }
}
