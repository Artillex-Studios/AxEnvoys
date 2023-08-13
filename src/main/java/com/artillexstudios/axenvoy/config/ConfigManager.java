package com.artillexstudios.axenvoy.config;

import com.artillexstudios.axenvoy.envoy.CrateLoader;
import com.artillexstudios.axenvoy.envoy.EnvoyLoader;
import com.artillexstudios.axenvoy.utils.ConfigUtils;
import com.artillexstudios.axenvoy.utils.FileUtils;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

import java.io.File;

public class ConfigManager {
    private static YamlDocument lang;
    private static YamlDocument config;
//    private static YamlDocument tempData;

    public static void reload() {
        try {
            lang = YamlDocument.create(new File(FileUtils.MAIN_DIRECTORY.toFile(), "messages.yml"), FileUtils.getResource("messages.yml"), GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().addRelocation("2", "messages.start", "messages.start.multiple", '.').addRelocation("2", "messages.flare-start", "messages.flare-start.multiple", '.').setVersioning(new BasicVersioning("config-version")).build());
            config = YamlDocument.create(new File(FileUtils.MAIN_DIRECTORY.toFile(), "config.yml"), FileUtils.getResource("config.yml"));
//            tempData = YamlDocument.create(new File(FileUtils.MAIN_DIRECTORY.toFile(), "data.yml"), FileUtils.getResource("data.yml"), GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.builder().setScalarStyle(ScalarStyle.LITERAL).build(), UpdaterSettings.DEFAULT);
        } catch (Exception exception) {
            ConfigUtils.testFile(new File(FileUtils.MAIN_DIRECTORY.toFile(), "messages.yml"));
            exception.printStackTrace();
        }

        load();
    }

    public static void load() {
        FileUtils.copyFromResource("crates");
        FileUtils.copyFromResource("envoys");

        CrateLoader.loadAll();
        EnvoyLoader.loadAll();
    }

    public static YamlDocument getLang() {
        return lang;
    }

//    public static YamlDocument getTempData() {
//        return tempData;
//     }

    public static YamlDocument getConfig() {
        return config;
    }
}
