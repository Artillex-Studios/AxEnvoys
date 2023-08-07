package com.artillexstudios.axenvoy.config;

import com.artillexstudios.axenvoy.envoy.CrateLoader;
import com.artillexstudios.axenvoy.envoy.EnvoyLoader;
import com.artillexstudios.axenvoy.utils.ConfigUtils;
import com.artillexstudios.axenvoy.utils.FileUtils;
import dev.dejvokep.boostedyaml.YamlDocument;

import java.io.File;

public class ConfigManager {
    private static YamlDocument lang;
    private static YamlDocument config;
    //private static YamlDocument tempData;

    public static void reload() {
        try {
            lang = YamlDocument.create(new File(FileUtils.MAIN_DIRECTORY.toFile(), "messages.yml"), FileUtils.getResource("messages.yml"));
            config = YamlDocument.create(new File(FileUtils.MAIN_DIRECTORY.toFile(), "config.yml"), FileUtils.getResource("config.yml"));
            //tempData = YamlDocument.create(new File(FileUtils.MAIN_DIRECTORY.toFile(), "data.yml"), FileUtils.getResource("data.yml"));
        } catch (Exception exception) {
            ConfigUtils.testFile(new File(FileUtils.MAIN_DIRECTORY.toFile(), "messages.yml"));
            exception.printStackTrace();
        }

        FileUtils.copyFromResource("crates");
        FileUtils.copyFromResource("envoys");

        CrateLoader.loadAll();
        EnvoyLoader.loadAll();
    }

    public static YamlDocument getLang() {
        return lang;
    }

    //public static YamlDocument getTempData() {
    //return tempData;
    // }

    public static YamlDocument getConfig() {
        return config;
    }
}
