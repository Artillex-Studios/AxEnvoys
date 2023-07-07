package com.artillexstudios.axenvoy.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

public class StringUtils {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer legacyComponentSerializer = LegacyComponentSerializer.legacyAmpersand();

    @NotNull
    public static Component format(String string) {
        string = PlaceholderAPI.setPlaceholders(null, string);
        final Component ct = legacyComponentSerializer.deserialize(string);
        string = miniMessage.serialize(ct).replace("\\", "");
        return miniMessage.deserialize(string);
    }

    @NotNull
    public static String formatToString(String string) {
        string = PlaceholderAPI.setPlaceholders(null, string);
        final Component ct = legacyComponentSerializer.deserialize(string);
        string = miniMessage.serialize(ct).replace("\\", "");
        return legacyComponentSerializer.serialize(miniMessage.deserialize(string));
    }
}
