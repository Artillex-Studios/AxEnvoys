package com.artillexstudios.axenvoy.envoy;

import com.artillexstudios.axenvoy.config.impl.CrateConfig;
import com.artillexstudios.axenvoy.rewards.Reward;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.bukkit.FireworkEffect;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CrateType {
    private final ArrayList<Reward> rewards = new ArrayList<>();
    private final File file;
    private final CrateConfig config;
    private final String name;
    private FireworkEffect.Type fireworkType;
    private FireworkEffect.Type flareFireworkType;
    private Color fireworkColor;
    private Color flareFireworkColor;
    private EnumeratedDistribution<Reward> reward;

    public CrateType(@NotNull File file) {
        this.name = file.getName().replace(".yml", "").replace(".yaml", "");
        this.file = file;
        Crates.register(this);

        this.config = new CrateConfig("crates/" + file.getName());
        this.config.reload();
    }

    public Reward randomReward() {
        return reward.sample();
    }

    public void reload() {
        config.reload();

        this.fireworkType = FireworkEffect.Type.valueOf(config.FIREWORK_TYPE.toUpperCase(Locale.ENGLISH));
        this.fireworkColor = new Color(Integer.valueOf(config.FIREWORK_COLOR.substring(1, 3), 16), Integer.valueOf(config.FIREWORK_COLOR.substring(3, 5), 16), Integer.valueOf(config.FIREWORK_COLOR.substring(5, 7), 16));
        this.flareFireworkColor = new Color(Integer.valueOf(config.FLARE_FIREWORK_COLOR.substring(1, 3), 16), Integer.valueOf(config.FLARE_FIREWORK_COLOR.substring(3, 5), 16), Integer.valueOf(config.FLARE_FIREWORK_COLOR.substring(5, 7), 16));
        this.flareFireworkType = FireworkEffect.Type.valueOf(config.FLARE_FIREWORK_TYPE.toUpperCase(Locale.ENGLISH));

        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<Object, Object> map : config.REWARDS) {
            HashMap<String, Object> hashMap = new HashMap<>();
            map.forEach((key, value) -> {
                hashMap.put((String) key, value);
            });

            list.add(hashMap);
        }


        this.rewards.clear();

        for (Map<String, Object> map : list) {
            this.rewards.add(new Reward(((Number) map.getOrDefault("chance", 0)).doubleValue(), (List<String>) map.getOrDefault("commands", new ArrayList<String>()), (List<String>) map.getOrDefault("messages", new ArrayList<String>()), (List<Map<Object, Object>>) map.getOrDefault("items", new ArrayList<Map<Object, Object>>()), (Map<Object, Object>) map.get("required-item"), (List<String>) map.getOrDefault("sounds", new ArrayList<String>()), (String) map.getOrDefault("name", "---")));
        }

        List<Pair<Reward, Double>> list1 = new ArrayList<>();
        for (Reward reward : rewards) {
            if (reward.requiredItem() != null) continue;
            list1.add(new Pair<>(reward, reward.chance()));
        }

        reward = new EnumeratedDistribution<>(list1);
    }

    public void remove() {
        Envoys.getTypes().forEach((name, envoy) -> {
            Iterator<SpawnedCrate> crateIterator = envoy.getSpawnedCrates().iterator();
            while (crateIterator.hasNext()) {
                SpawnedCrate next = crateIterator.next();
                if (next.getHandle().equals(this)) {
                    crateIterator.remove();
                    next.claim(null, envoy, false);
                }
            }
        });
    }

    public ArrayList<Reward> getRewards() {
        return rewards;
    }

    public File getFile() {
        return file;
    }

    public CrateConfig getConfig() {
        return config;
    }

    public Color getFireworkColor() {
        return fireworkColor;
    }

    public FireworkEffect.Type getFireworkType() {
        return fireworkType;
    }

    public FireworkEffect.Type getFlareFireworkType() {
        return flareFireworkType;
    }

    public Color getFlareFireworkColor() {
        return flareFireworkColor;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof CrateType crateType)) return false;

        return getFile().equals(crateType.getFile());
    }

    @Override
    public int hashCode() {
        return getFile().hashCode();
    }
}
