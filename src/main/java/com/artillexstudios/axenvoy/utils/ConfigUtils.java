package com.artillexstudios.axenvoy.utils;

import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import me.neznamy.yamlassist.YamlAssist;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.util.List;

public class ConfigUtils {

    public static boolean testFile(@NotNull File file) {
        try {
            YamlConfiguration config = new YamlConfiguration();
            config.load(file);
        } catch (Exception exception) {
            if (exception.getCause() instanceof YAMLException) {
                List<String> suggestions = YamlAssist.getSuggestions(file);

                AxEnvoyPlugin.getInstance().getComponentLogger().error(StringUtils.format("<color:#ff0000>Found %s issues while parsing file: %s. This file won't be loaded.</color>".formatted(suggestions.size(), file.getName())));
                for (String suggestion : suggestions) {
                    AxEnvoyPlugin.getInstance().getComponentLogger().error(StringUtils.format("<color:#ff0000>Possible issue: %s</color>".formatted(suggestion)));
                }
                return false;
            }

            return false;
        }
        return true;
    }
}
