package com.artillexstudios.axenvoy.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StringUtils {
    private static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.builder().character('\u00a7').useUnusualXRepeatedCharacterHexFormat().hexColors().build();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @NotNull
    public static String format(@NotNull String message) {
        return format(message, null);
    }

    @NotNull
    public static String format(@NotNull String message, Player player) {
        message = PlaceholderAPI.setPlaceholders(player, message);
        message = message.replace('\u00a7', '&');
        message = toLegacy(MINI_MESSAGE.deserialize(message));
        message = ChatColor.translateAlternateColorCodes('&', message);

        return message;
    }

    @NotNull
    public static Component formatToComponent(@NotNull String message) {
        return toComponent(format(message));
    }

    public static @NotNull String toLegacy(@NotNull Component component) {
        return LEGACY_COMPONENT_SERIALIZER.serialize(component);
    }

    public static @NotNull Component toComponent(@NotNull String message) {
        return LEGACY_COMPONENT_SERIALIZER.deserialize(message);
    }
}
