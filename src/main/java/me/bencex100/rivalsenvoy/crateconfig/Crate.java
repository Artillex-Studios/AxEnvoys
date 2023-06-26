package me.bencex100.rivalsenvoy.crateconfig;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import me.bencex100.rivalsenvoy.RivalsEnvoy;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Crate {
    private final String name;
    private YamlDocument crateConfig;
    private double spawnChance;
    private Material material;
    private boolean fireworkEnabled;
    private String fireworkColor;
    private double hologramHeight;
    private List<String> hologramLines;

    public Crate(@NotNull String name) {
        this.name = name;
        loadConfig();
    }

    public void loadConfig() {
        RivalsEnvoy main = RivalsEnvoy.getInstance();
        try {
            crateConfig = YamlDocument.create(new File(main.getDataFolder(), "crates/" + name + ".yml"), main.getResource("crates/" + name + ".yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().build(), DumperSettings.DEFAULT, UpdaterSettings.builder().build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.spawnChance = crateConfig.getDouble("spawn-chance");
        this.material = Material.getMaterial(crateConfig.getString("material"));
        this.fireworkEnabled = crateConfig.getBoolean("firework.enabled");
        this.fireworkColor = crateConfig.getString("firework.firework-color");
        this.hologramHeight = crateConfig.getDouble("hologram-height");
        this.hologramLines = crateConfig.getStringList("hologram");
    }

    @NotNull
    public YamlDocument getCrateConfig() {
        return crateConfig;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public List<String> getHologramLines() {
        return hologramLines;
    }

    public boolean isFireworkEnabled() {
        return fireworkEnabled;
    }

    public double getHologramHeight() {
        return hologramHeight;
    }

    public double getSpawnChance() {
        return spawnChance;
    }

    public Material getMaterial() {
        return material;
    }

    public String getFireworkColor() {
        return fireworkColor;
    }
}
