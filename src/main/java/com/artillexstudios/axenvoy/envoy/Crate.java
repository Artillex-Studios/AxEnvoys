package com.artillexstudios.axenvoy.envoy;

import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.rewards.Reward;
import com.artillexstudios.axenvoy.utils.StringUtils;
import dev.dejvokep.boostedyaml.YamlDocument;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Crate {
    private final ObjectArrayList<Reward> rewards = new ObjectArrayList<>();
    private final List<String> hologramLines;
    private final String name;
    private final String fireworkHex;
    private final String flareFireworkHex;
    private final String displayName;
    private final Material material;
    private final Material fallingBlockType;
    private final boolean hasCollectionCooldown;
    private final boolean fallingBlock;
    private final boolean flareEnabled;
    private final boolean hologram;
    private final TriState broadcastCollect;
    private final boolean firework;
    private final double hologramHeight;
    private final double fallingBlockSpeed;
    private final int collectionCooldown;
    private final int fallingBlockHeight;
    private final int flareTicks;
    private final FireworkEffect.Type fireworkType;
    private final FireworkEffect.Type flareFireworkType;
    private final Color fireworkColor;
    private final Color flareFireworkColor;
    private final YamlDocument document;

    public Crate(@NotNull YamlDocument config) {
        this.document = config;
        this.name = config.getFile().getName().replace(".yml", "").replace(".yaml", "");
        this.material = Material.matchMaterial(config.getString("block", "stone"));
        this.collectionCooldown = config.getInt("collect-cooldown", 10);
        this.hasCollectionCooldown = config.getOptional("collect-cooldown").isPresent();
        if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
            this.hologram = config.getBoolean("hologram.enabled", true);
        } else {
            AxEnvoyPlugin.getInstance().getLogger().warning(StringUtils.format("<color:#ff0000>Disabling hologram due to DecentHolograms not being loaded!"));
            this.hologram = false;
        }
        this.hologramLines = config.getStringList("hologram.lines", new ArrayList<>());
        this.hologramHeight = config.getDouble("hologram.height", 2.0);
        this.fallingBlock = config.getBoolean("falling-block.enabled", false);
        this.broadcastCollect = TriState.byBoolean(config.getBoolean("broadcast-collect"));
        this.firework = config.getBoolean("firework.enabled", false);
        this.fireworkHex = config.getString("firework.color", "#ff0000");
        this.flareFireworkHex = config.getString("flare.firework.color", "#ff0000");
        this.fallingBlockHeight = config.getInt("falling-block.height", 10);
        this.fallingBlockType = Material.matchMaterial(config.getString("falling-block.block", "stone"));
        this.fallingBlockSpeed = config.getDouble("falling-block.speed", 1.0D);
        this.displayName = config.getString("display-name", this.name);
        this.fireworkType = FireworkEffect.Type.valueOf(config.getString("firework.type", "ball").toUpperCase(Locale.ENGLISH));
        this.fireworkColor = new Color(Integer.valueOf(fireworkHex.substring(1, 3), 16), Integer.valueOf(fireworkHex.substring(3, 5), 16), Integer.valueOf(fireworkHex.substring(5, 7), 16));
        this.flareFireworkColor = new Color(Integer.valueOf(flareFireworkHex.substring(1, 3), 16), Integer.valueOf(flareFireworkHex.substring(3, 5), 16), Integer.valueOf(flareFireworkHex.substring(5, 7), 16));
        this.flareFireworkType = FireworkEffect.Type.valueOf(config.getString("flare.firework.type", "ball").toUpperCase(Locale.ENGLISH));
        this.flareEnabled = config.getBoolean("flare.enabled", true);
        this.flareTicks = config.getInt("flare.every", 0);

        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<?, ?> map : config.getMapList("rewards")) {
            HashMap<String, Object> hashMap = new HashMap<>();
            map.forEach((key, value) -> {
                hashMap.put((String) key, value);
            });

            list.add(hashMap);
        }

        for (Map<String, Object> map : list) {
            this.rewards.add(new Reward((double) map.get("chance"), (List<String>) map.getOrDefault("commands", new ArrayList<String>()), (List<String>) map.getOrDefault("messages", new ArrayList<String>())));
        }
    }

    public ObjectArrayList<Reward> getRewards() {
        return rewards;
    }

    public Material getMaterial() {
        return material;
    }

    public int getCollectionCooldown() {
        return collectionCooldown;
    }

    public boolean isHologram() {
        return hologram;
    }

    public boolean isFirework() {
        return firework;
    }

    public boolean isFallingBlock() {
        return fallingBlock;
    }

    public int getFallingBlockHeight() {
        return fallingBlockHeight;
    }

    public Material getFallingBlockType() {
        return fallingBlockType;
    }

    public double getFallingBlockSpeed() {
        return fallingBlockSpeed;
    }

    public List<String> getHologramLines() {
        return hologramLines;
    }

    public double getHologramHeight() {
        return hologramHeight;
    }

    public String getFireworkHex() {
        return fireworkHex;
    }

    public String getName() {
        return name;
    }

    public boolean isHasCollectionCooldown() {
        return hasCollectionCooldown;
    }

    public TriState isBroadcastCollect() {
        return broadcastCollect;
    }

    public String getDisplayName() {
        return displayName;
    }

    public FireworkEffect.Type getFireworkType() {
        return fireworkType;
    }

    public boolean isFlareEnabled() {
        return flareEnabled;
    }

    public String getFlareFireworkHex() {
        return flareFireworkHex;
    }

    public FireworkEffect.Type getFlareFireworkType() {
        return flareFireworkType;
    }

    public Color getFireworkColor() {
        return fireworkColor;
    }

    public Color getFlareFireworkColor() {
        return flareFireworkColor;
    }

    public int getFlareTicks() {
        return flareTicks;
    }
}
