package me.bencex100.rivalsenvoy.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import me.bencex100.rivalsenvoy.RivalsEnvoy;
import me.bencex100.rivalsenvoy.utils.Utils;

import java.io.File;
import java.io.IOException;

public class Config {
    private static YamlDocument config = null;
    private static YamlDocument data = null;
    private static YamlDocument messages = null;
    public void loadConfig() {
        try {
            config = YamlDocument.create(new File(RivalsEnvoy.getInstance().getDataFolder(), "config.yml"), RivalsEnvoy.getInstance().getResource("config.yml"),
                    GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).build());
            data = YamlDocument.create(new File(RivalsEnvoy.getInstance().getDataFolder(), "data.yml"), RivalsEnvoy.getInstance().getResource("data.yml"));
            messages = YamlDocument.create(new File(RivalsEnvoy.getInstance().getDataFolder(), "messages.yml"), RivalsEnvoy.getInstance().getResource("messages.yml"),
                    GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static YamlDocument getCnf(String name) {
        return switch (name) {
            case "data" -> data;
            case "messages" -> messages;
            default -> config;
        };
    }

    public static void reloadCfs() {
        try {
            config.reload();
            data.reload();
            messages.reload();
            new Utils().updateBlackList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
