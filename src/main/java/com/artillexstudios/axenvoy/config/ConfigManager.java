package com.artillexstudios.axenvoy.config;

import com.artillexstudios.axenvoy.envoy.CrateLoader;
import com.artillexstudios.axenvoy.envoy.EnvoyLoader;
import com.artillexstudios.axenvoy.utils.ConfigUtils;
import com.artillexstudios.axenvoy.utils.FileUtils;
import dev.dejvokep.boostedyaml.YamlDocument;

import java.io.File;

public class ConfigManager {
    private static YamlDocument lang;

    public static void reload() {
        try {
            lang = YamlDocument.create(new File(FileUtils.MAIN_DIRECTORY.toFile(), "messages.yml"), FileUtils.getResource("messages.yml"));
        } catch (Exception exception) {
            ConfigUtils.testFile(new File(FileUtils.MAIN_DIRECTORY.toFile(), "messages.yml"));
        }

        FileUtils.copyFromResource("crates");
        FileUtils.copyFromResource("envoys");

        CrateLoader.loadAll();
        EnvoyLoader.loadAll();
    }

    public static YamlDocument getLang() {
        return lang;
    }
}
