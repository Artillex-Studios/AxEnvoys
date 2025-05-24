package com.artillexstudios.axenvoy.config.impl;

import com.artillexstudios.axenvoy.config.AbstractConfig;
import com.artillexstudios.axenvoy.utils.FileUtils;

public class Config extends AbstractConfig {

    @Key("listen-to-block-physics")
    @Comment({"Enable this, if you want to prevent the crateTypes from changing their surrounding blocks.",
            "Examples:",
            "If a crateType spawns on a 'dirt path' block, that block will become dirt, if this feature is disabled.",
            "Warning: This will probably tank your performance at crateType spawning, if lots of crateTypes spawn (lots = hundreds)",
            "Changing this requires a restart!"
    })
    public static boolean LISTEN_TO_BLOCK_PHYSICS = false;

    @Key("dont-replace-blocks")
    @Comment({
            "Enable this, if you want to prevent some blocks that",
            "replaced by crates in certain cases."
    })
    public static boolean DONT_REPLACE_BLOCKS = true;

    @Key("update-checker.enabled")
    @Comment({
            "Enable this, if you want the plugin to check for updates"
    })
    public static boolean UPDATE_CHECKER_ENABLED = true;

    @Key("update-checker.message-on-join")
    @Comment({
            "Enable this, if you want the plugin to check for updates",
            "and send messages to users with the axenvoys.updatecheck.onjoin permission.",
            "This only works if the update checker is enabled!"
    })
    public static boolean UPDATE_CHECKER_MESSAGE_ON_JOIN = true;

    @Key("debug")
    @Comment({
            "Enable this, if you'd like to receive some detailed",
            "logs of why something is happening. This will send messages",
            "in the console."
    })
    public static boolean DEBUG = false;

    private static final Config CONFIG = new Config();

    public static void reload() {
        FileUtils.extractFile(Config.class, "config.yml", FileUtils.PLUGIN_DIRECTORY, false);

        CONFIG.reload(FileUtils.PLUGIN_DIRECTORY.resolve("config.yml"), Config.class, null, null);
    }
}
