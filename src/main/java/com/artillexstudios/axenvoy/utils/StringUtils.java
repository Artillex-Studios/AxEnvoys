package com.artillexstudios.axenvoy.utils;

import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    private static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.builder().character('\u00a7').useUnusualXRepeatedCharacterHexFormat().hexColors().build();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");

    @NotNull
    public static String format(@NotNull String message) {
        return format(message, null);
    }

    @NotNull
    public static String format(@NotNull String message, Player player) {
        if (AxEnvoyPlugin.getInstance().isPlaceholderApi()) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }
        message = message.replace('\u00a7', '&');
        message = toLegacy(MINI_MESSAGE.deserialize(message));
        message = legacyHexFormat(message);
        message = ChatColor.translateAlternateColorCodes('&', message);

        return message;
    }

    // Thanks! https://www.spigotmc.org/threads/hex-color-code-translate.449748/
    public static String legacyHexFormat(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        var builder = new StringBuilder(message.length() + 4 * 8);

        while (matcher.find()) {
            var group = matcher.group(1);
            matcher.appendReplacement(builder, ChatColor.COLOR_CHAR + "x"
                    + ChatColor.COLOR_CHAR + group.charAt(0) + ChatColor.COLOR_CHAR + group.charAt(1)
                    + ChatColor.COLOR_CHAR + group.charAt(2) + ChatColor.COLOR_CHAR + group.charAt(3)
                    + ChatColor.COLOR_CHAR + group.charAt(4) + ChatColor.COLOR_CHAR + group.charAt(5)
            );
        }

        return matcher.appendTail(builder).toString();
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
