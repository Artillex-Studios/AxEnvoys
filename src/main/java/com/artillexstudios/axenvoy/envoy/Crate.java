package com.artillexstudios.axenvoy.envoy;

import com.artillexstudios.axenvoy.rewards.CommandReward;
import dev.dejvokep.boostedyaml.YamlDocument;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.util.TriState;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Crate {
    private final ObjectArrayList<CommandReward> rewards = new ObjectArrayList<>();
    private final List<String> hologramLines;
    private final String name;
    private final String fireworkHex;
    private final String displayName;
    private final Material material;
    private final Material fallingBlockType;
    private final boolean hasCollectionCooldown;
    private final boolean fallingBlock;
    private final boolean hologram;
    private final TriState broadcastCollect;
    private final boolean firework;
    private final double hologramHeight;
    private final double fallingBlockSpeed;
    private final int collectionCooldown;
    private final int fallingBlockHeight;
    private final YamlDocument document;

    public Crate(@NotNull YamlDocument config) {
        System.out.println("new crate");
        this.document = config;
        this.name = config.getFile().getName().replace(".yml", "").replace(".yaml", "");
        this.material = Material.matchMaterial(config.getString("block", "stone"));
        this.collectionCooldown = config.getInt("collect-cooldown", 10);
        this.hasCollectionCooldown = config.getOptional("collect-cooldown").isPresent();
        this.hologram = config.getBoolean("hologram.enabled", true);
        this.hologramLines = config.getStringList("hologram.lines", new ArrayList<>());
        this.hologramHeight = config.getDouble("hologram.height", 2.0);
        this.fallingBlock = config.getBoolean("falling-block.enabled", false);
        this.broadcastCollect = TriState.byBoolean(config.getBoolean("broadcast-collect"));
        this.firework = config.getBoolean("firework.enabled", false);
        this.fireworkHex = config.getString("firework.color", "#ff0000");
        this.fallingBlockHeight = config.getInt("falling-block.height", 10);
        this.fallingBlockType = Material.matchMaterial(config.getString("falling-block.block", "stone"));
        this.fallingBlockSpeed = config.getDouble("falling-block.speed", 1.0D);
        this.displayName = config.getString("display-name", this.name);

        for (Map<?, ?> map : config.getMapList("rewards")) {
            this.rewards.add(new CommandReward((double) map.get("chance"), (List<String>) map.get("commands")));
        }
    }

    public ObjectArrayList<CommandReward> getRewards() {
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
}
