package com.artillexstudios.axenvoy.envoy;

import com.artillexstudios.axenvoy.config.ConfigManager;
import com.artillexstudios.axenvoy.utils.StringUtils;
import com.artillexstudios.axenvoy.utils.Utils;
import dev.dejvokep.boostedyaml.YamlDocument;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Envoy {
    private final ObjectArrayList<Material> notOnMaterials = new ObjectArrayList<>();
    private final ObjectArrayList<SpawnedCrate> spawnedCrates = new ObjectArrayList<>();
    private final Object2ObjectArrayMap<Crate, Double> cratesMap = new Object2ObjectArrayMap<>();
    private final YamlDocument document;
    private final String name;
    private final Location center;
    private final ItemStack flare;
    private final boolean broadcastCollect;
    private final boolean collectGlobalCooldown;
    private boolean active;
    private boolean randomSpawns;
    private boolean predefinedSpawns;
    private boolean flareEnabled;
    private int flareCooldown;
    private int collectCooldown;
    private int timeoutTime;
    private int crateAmount;
    private int minDistance;
    private int maxDistance;
    private int minHeight;
    private int maxHeight;

    public Envoy(@NotNull YamlDocument config) {
        this.document = config;
        this.active = false;
        this.name = config.getFile().getName().replace(".yml", "").replace(".yaml", "");
        this.center = Utils.deserializeLocation(config, "random-spawn.center");
        this.randomSpawns = config.getBoolean("random-spawn.enabled", false);
        this.predefinedSpawns = config.getBoolean("pre-defined-spawns.enabled", false);
        this.flareEnabled = config.getBoolean("flare.enabled", false);
        this.broadcastCollect = config.getBoolean("broadcast-collect", false);
        this.collectGlobalCooldown = config.getBoolean("collect-global-cooldown", false);
        this.crateAmount = config.getInt("amount", 30);
        this.collectCooldown = config.getInt("collect-cooldown", 10);
        this.timeoutTime = config.getInt("timeout-time", 900);
        this.minDistance = config.getInt("random-spawn.min-distance", 20);
        this.maxDistance = config.getInt("random-spawn.max-distance", 100);
        this.minHeight = config.getInt("random-spawn.min-height", 10);
        this.maxHeight = config.getInt("random-spawn.max-height", 200);
        this.flareCooldown = config.getInt("flare.cooldown", 30);

        if (flareEnabled) {
            flare = Utils.createItem(config.getSection("flare.item"), name);
        } else {
            flare = null;
        }

        for (Object crates : config.getSection("crates").getKeys()) {
            for (Crate crate : CrateLoader.crates) {
                if (!crate.getName().equals(crates)) continue;
                cratesMap.put(crate, config.getSection("crates").getDouble((String) crates));
            }
        }

        config.getOptionalStringList("random-spawn.not-on-blocks").ifPresent(list -> {
            ObjectArrayList<Pattern> patterns = new ObjectArrayList<>(list.size());
            for (String s : list) {
                patterns.add(Pattern.compile(s));
            }

            material:
            for (Material value : Material.values()) {
                for (Pattern pattern : patterns) {
                    Matcher matcher = pattern.matcher(value.name().toLowerCase(Locale.ENGLISH));
                    if (!matcher.find()) continue;

                    this.notOnMaterials.add(value);

                    // Don't waste time looking through all patterns if we've already found one that matches
                    continue material;
                }
            }
        });
    }

    public ObjectArrayList<SpawnedCrate> getSpawnedCrates() {
        return spawnedCrates;
    }

    public Object2ObjectArrayMap<Crate, Double> getCratesMap() {
        return cratesMap;
    }

    public YamlDocument getDocument() {
        return document;
    }

    public Location getCenter() {
        return center;
    }

    public boolean start(Player player) {
        if (center == null) {
            return false;
        }

        if (active) {
            return false;
        }

        this.active = true;

        if (predefinedSpawns) {
            for (String s : this.document.getStringList("pre-defined-spawns.locations")) {
                Location location = Utils.deserializeLocation(this.document, "pre-defined-spawns.locations.%s".formatted(s));
                this.spawnedCrates.add(new SpawnedCrate(this, Utils.randomCrate(cratesMap), Utils.topBlock(location)));
            }
        }

        if (randomSpawns) {
            for (int i = 0; i < crateAmount; i++) {
                Location location = null;
                int tries = 0;
                while (location == null || tries < 500) {
                    tries++;
                    location = Utils.getNextLocation(this, center.clone());
                }

                new SpawnedCrate(this, Utils.randomCrate(cratesMap), location.clone());
            }
        }

        if (player == null) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.sendMessage(StringUtils.toString(getMessage("prefix").append(getMessage("start").replaceText(replace -> {
                    replace.match("%amount%");
                    replace.replacement(String.valueOf(spawnedCrates.size()));
                }))));
            }
        } else {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.sendMessage(StringUtils.toString(getMessage("prefix").append(getMessage("flare-start").replaceText(replace -> {
                    replace.match("%player%");
                    replace.replacement(player.getName());
                }).replaceText(replace -> {
                    replace.match("%amount%");
                    replace.replacement(String.valueOf(spawnedCrates.size()));
                }))));
            }
        }

        return true;
    }

    public Component getMessage(String path) {
        return document.getOptionalString("messages.%s".formatted(path)).map(StringUtils::format).orElseGet(() -> StringUtils.format(ConfigManager.getLang().getString("messages.%s".formatted(path))));
    }

    public ObjectArrayList<Material> getNotOnMaterials() {
        return notOnMaterials;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isRandomSpawns() {
        return randomSpawns;
    }

    public void setRandomSpawns(boolean randomSpawns) {
        this.randomSpawns = randomSpawns;
    }

    public boolean isPredefinedSpawns() {
        return predefinedSpawns;
    }

    public void setPredefinedSpawns(boolean predefinedSpawns) {
        this.predefinedSpawns = predefinedSpawns;
    }

    public boolean isFlareEnabled() {
        return flareEnabled;
    }

    public void setFlareEnabled(boolean flareEnabled) {
        this.flareEnabled = flareEnabled;
    }

    public int getFlareCooldown() {
        return flareCooldown;
    }

    public void setFlareCooldown(int flareCooldown) {
        this.flareCooldown = flareCooldown;
    }

    public int getCollectCooldown() {
        return collectCooldown;
    }

    public void setCollectCooldown(int collectCooldown) {
        this.collectCooldown = collectCooldown;
    }

    public int getTimeoutTime() {
        return timeoutTime;
    }

    public void setTimeoutTime(int timeoutTime) {
        this.timeoutTime = timeoutTime;
    }

    public int getCrateAmount() {
        return crateAmount;
    }

    public void setCrateAmount(int crateAmount) {
        this.crateAmount = crateAmount;
    }

    public int getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(int minDistance) {
        this.minDistance = minDistance;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(int maxDistance) {
        this.maxDistance = maxDistance;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(int minHeight) {
        this.minHeight = minHeight;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public String getName() {
        return name;
    }

    public boolean isBroadcastCollect() {
        return broadcastCollect;
    }

    public ItemStack getFlare() {
        return flare;
    }

    public boolean isCollectGlobalCooldown() {
        return collectGlobalCooldown;
    }
}
