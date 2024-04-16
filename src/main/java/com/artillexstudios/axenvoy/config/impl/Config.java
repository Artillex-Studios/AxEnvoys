package com.artillexstudios.axenvoy.config.impl;

import com.artillexstudios.axenvoy.config.AbstractConfig;
import com.artillexstudios.axenvoy.utils.FileUtils;
import org.checkerframework.checker.units.qual.K;

public class Config extends AbstractConfig {

    @Key("listen-to-block-physics")
    @Comment("""
            Enable this, if you want to prevent the crateTypes from changing their surrounding blocks.
            Examples:
            If a crateType spawns on a 'dirt path' block, that block will become dirt, if this feature is disabled.
            Warning: This will probably tank your performance at crateType spawning, if lots of crateTypes spawn (lots = hundreds)
            Changing this requires a restart!\
            """)
    public static boolean LISTEN_TO_BLOCK_PHYSICS = false;

    @Key("locale")
    public static String LOCALE = "en";

    @Key("prefix")
    public static String PREFIX = "<#00FFAA>&lAxEnvoy &7» ";

    private static final Config CONFIG = new Config();

    public static void reload() {
        FileUtils.extractFile(Config.class, "config.yml", FileUtils.PLUGIN_DIRECTORY, false);

        CONFIG.reload(FileUtils.PLUGIN_DIRECTORY.resolve("config.yml"), Config.class, null, null);
    }
}
