package com.artillexstudios.axenvoy.config.impl;

import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.utils.FileUtils;

import java.util.List;
import java.util.Map;

public class EnvoyConfig extends Messages {

    @Key("crates")
    public Map<Object, Object> CRATES = Map.of("common", 50, "legendary", "30", "rare", 40);

    @Key("amount")
    @Comment("You can also define a range, like 30-50")
    public String SPAWN_AMOUNT = "30";

    @Key("limit-predefined")
    @Comment("The amount cap will be used for predefined spawns aswell")
    public boolean LIMIT_PREDEFINED = true;

    @Key("collect-cooldown")
    @Comment("In seconds")
    public int COLLECT_COOLDOWN = 10;

    @Key("collect-global-cooldown")
    @Comment("Whether the cooldown should be for all crates of this envoy, or per-crate")
    public boolean COLLECT_GLOBAL_COOLDOWN = false;

    @Key("broadcast-collect")
    public boolean BROADCAST_COLLECT = true;

    @Key("timeout-time")
    @Comment("This is in seconds")
    public int TIMEOUT_TIME = 300;

    @Key("every")
    @Comment("Start an envoy event every hour. To disable, leave as \"\"")
    public String EVERY = "1h";

    @Key("send-spawn-message")
    public boolean SEND_SPAWN_MESSAGES = false;

    @Key("alert-times")
    public List<String> ALERT_TIMES = List.of("30m", "10m", "5s", "3s", "2s", "1s");

    @Key("only-in-global")
    @Comment("If there are other regions at that location other than __global__ an envoy can't spawn there")
    public boolean ONLY_IN_GLOBAL = false;

    @Key("min-players")
    public int MIN_PLAYERS = 2;

    @Key("random-spawn.enabled")
    @Comment("Should we use random spawning?")
    public boolean RANDOM_SPAWN = true;

    @Key("random-spawn.center")
    @Comment("The center location")
    public String RANDOM_SPAWN_CENTER = "world;0;100;0";

    @Key("random-spawn.max-distance.x")
    @Comment("The maximum distance from the center an envoy may spawn, x coordinate")
    public int RANDOM_SPAWN_MAX_DISTANCE_X = 100;

    @Key("random-spawn.max-distance.z")
    @Comment("The maximum distance from the center an envoy may spawn, z coordinate")
    public int RANDOM_SPAWN_MAX_DISTANCE_Z = 100;

    @Key("random-spawn.min-distance")
    @Comment("The minimum distance from the center an envoy may spawn")
    public int RANDOM_SPAWN_MIN_DISTANCE = 20;

    @Key("random-spawn.min-height")
    @Comment("The minimum height at which an envoy may spawn")
    public int RANDOM_SPAWN_MIN_HEIGHT = 10;

    @Key("random-spawn.max-height")
    @Comment("The maximum height at which an envoy may spawn")
    public int RANDOM_SPAWN_MAX_HEIGHT = 100;

    @Key("random-spawn.min-distance-between-crates")
    @Comment("The minimum distance that can be between crates")
    public int RANDOM_SPAWN_MIN_DISTANCE_BETWEEN_CRATES = 0;

    @Key("random-spawn.not-on-blocks")
    @Comment({"You can use RegEx here, to match the name of the block", "For a list of materials, visit: https://jd.papermc.io/paper/1.20/org/bukkit/Material.html"})
    public List<String> RANDOM_SPAWN_BLACKLISTED_MATERIALS = List.of("(?<!s)t?air(?!s)", "(leaves)", "(sign)", "diamond_block");

    @Key("rewards.use-prefix")
    @Comment("If we should use prefix for message rewards of this envoy")
    public boolean USE_PREFIX = true;

    @Key("time-format")
    @Comment({"1 - HH:MM:SS, for example 01:25:35", "2 - short format, for example 20m", "3 - text format, for example 01h 25m 35s"})
    public int TIME_FORMAT = 1;

    @Key("flare.enabled")
    public boolean FLARE_ENABLED = true;

    @Key("flare.cooldown")
    @Comment("In seconds")
    public int FLARE_COOLDOWN = 30;

    @Key("flare.item")
    public Map<Object, Object> FLARE_ITEM = Map.of("material", "prismarine_shard", "name", "&cEnvoy flare", "glow", true, "custom-model-data", 0, "lore", List.of("&7Right click with this, to start", "&7the default envoy!"));

    @Key("pre-defined-spawns.enabled")
    public boolean PREDEFINED_SPAWNS = false;

    @Key("pre-defined-spawns.locations")
    public List<String> PREDEFINED_LOCATIONS = List.of("world;1;1;1");

    public EnvoyConfig(String fileName) {
        super(fileName);
    }

    public void reload() {
        com.artillexstudios.axapi.config.Config config = new com.artillexstudios.axapi.config.Config(FileUtils.PLUGIN_DIRECTORY.resolve(fileName).toFile());

        if (config.getBackingDocument().isInt("random-spawn.max-distance")) {
            int maxDistance = config.getInt("random-spawn.max-distance");

            config.getBackingDocument().remove("random-spawn.max-distance");
            config.set("random-spawn.max-distance.x", maxDistance);
            config.set("random-spawn.max-distance.z", maxDistance);
            config.save();
        }

        this.reload(FileUtils.PLUGIN_DIRECTORY.resolve(fileName), Messages.class, this, AxEnvoyPlugin.getMessages());
        this.reload(FileUtils.PLUGIN_DIRECTORY.resolve(fileName), EnvoyConfig.class, this, null);
    }
}
