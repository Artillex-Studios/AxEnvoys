package com.artillexstudios.axenvoy.config.impl;

import com.artillexstudios.axenvoy.config.AbstractConfig;
import com.artillexstudios.axenvoy.utils.FileUtils;

import java.util.List;
import java.util.Map;

public class CrateConfig extends AbstractConfig {

    @Key("collect-cooldown")
    @Comment({"Cooldown in seconds until a player can collect a new crateType of this type", "THIS OVERRIDES THE SAME SETTING IN THE ENVOY'S CATEGORY! If you do NOT want to override it, remove it from here"})
    public int COLLECT_COOLDOWN = 10;

    @Key("broadcast-collect")
    public boolean BROADCAST_COLLECT = true;

    @Key("display-name")
    public String DISPLAY_NAME = "Crate";

    @Key("block")
    public String BLOCK_TYPE = "iron_block";

    @Key("required-interactions.amount")
    public int REQUIRED_INTERACTION_AMOUNT = 1;

    @Key("required-interactions.cooldown")
    @Comment("In ticks. 1 second is 20 ticks! Set to 0 to disable!")
    public int REQUIRED_INTERACTION_COOLDOWN = 0;

    @Key("falling-block.enabled")
    public boolean FALLING_BLOCK_ENABLED = true;

    @Key("falling-block.height")
    public double FALLING_BLOCK_HEIGHT = 10;

    @Key("falling-block.block")
    public String FALLING_BLOCK_BLOCK = "end_rod";

    @Key("falling-block.speed")
    public double FALLING_BLOCK_SPEED = -1;

    @Key("firework.enabled")
    public boolean FIREWORK_ENABLED = true;

    @Key("firework.color")
    public String FIREWORK_COLOR = "#ff0000";

    @Key("firework.type")
    public String FIREWORK_TYPE = "ball";

    @Key("flare.enabled")
    public boolean FLARE_ENABLED = true;

    @Key("flare.every")
    @Comment("In ticks, 20 ticks is 1 second")
    public long FLARE_EVERY = 200;

    @Key("flare.firework.color")
    public String FLARE_FIREWORK_COLOR = "#ff0000";

    @Key("flare.firework.type")
    public String FLARE_FIREWORK_TYPE = "ball";

    @Key("rewards")
    public List<Map<Object, Object>> REWARDS = List.of();

    @Key("hologram.enabled")
    public boolean HOLOGRAM_ENABLED = true;

    @Key("hologram.height")
    public double HOLOGRAM_HEIGHT = 2.0;

    @Key("hologram.lines")
    public List<String> HOLOGRAM_LINES = List.of("&7Envoy!", "&fRarity: &acommon.");

    protected final String fileName;

    public CrateConfig(String fileName) {
        this.fileName = fileName;
    }

    public void reload() {
        this.reload(FileUtils.PLUGIN_DIRECTORY.resolve(fileName), CrateConfig.class, this, null);
    }
}
