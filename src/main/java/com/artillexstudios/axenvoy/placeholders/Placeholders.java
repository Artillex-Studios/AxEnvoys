package com.artillexstudios.axenvoy.placeholders;

import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.envoy.EnvoyLoader;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

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
        Optional<Envoy> optionalEnvoy = EnvoyLoader.envoys.stream().filter(envoy1 -> envoy1.getName().equalsIgnoreCase(envoyName)).findFirst();

        if (optionalEnvoy.isEmpty()) {
            return "";
        }
        Envoy envoy = optionalEnvoy.get();

        switch (args[0]) {
            case "active" -> {
                if (envoy.isActive()) {
                    return LegacyComponentSerializer.legacySection().serialize(envoy.getMessage("placeholder.running"));
                }

                return LegacyComponentSerializer.legacySection().serialize(envoy.getMessage("placeholder.not-running"));
            }
            case "remaining" -> {
                if (!envoy.isActive()) {
                    return LegacyComponentSerializer.legacySection().serialize(envoy.getMessage("placeholder.not-running"));
                }

                return LegacyComponentSerializer.legacySection().serialize(envoy.getMessage("placeholder.remaining").replaceText(replacement -> {
                    replacement.match("%replacement%");
                    replacement.replacement(String.valueOf(envoy.getSpawnedCrates().size()));
                }));
            }
        }

        return "";
    }
}
