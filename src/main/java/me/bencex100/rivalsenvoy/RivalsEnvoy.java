package me.bencex100.rivalsenvoy;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import me.bencex100.rivalsenvoy.commands.Commands;
import me.bencex100.rivalsenvoy.config.ConfigManager;
import me.bencex100.rivalsenvoy.crateconfig.Crates;
import me.bencex100.rivalsenvoy.listeners.ActivateFlare;
import me.bencex100.rivalsenvoy.listeners.BlockPhysicsListener;
import me.bencex100.rivalsenvoy.listeners.CollectionListener;
import me.bencex100.rivalsenvoy.listeners.FallingBlockListener;
import me.bencex100.rivalsenvoy.listeners.FireworkDamageListener;
import me.bencex100.rivalsenvoy.utils.Utils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static me.bencex100.rivalsenvoy.envoy.EnvoyHandler.crates;

public final class RivalsEnvoy extends JavaPlugin {
    private static RivalsEnvoy instance;

    public static RivalsEnvoy getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
    }

    @Override
    public void onEnable() {
        instance = this;

        CommandAPI.onEnable();
        new ConfigManager().loadConfig();
        new Commands().register();
        new Utils().updateBlackList();

        File path = new File(getDataFolder(), "crates");
        if (!path.exists()) {
            try {
                YamlDocument.create(new File(getDataFolder(), "crates/gyakori.yml"), getResource("crates/gyakori.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().build(), DumperSettings.DEFAULT, UpdaterSettings.builder().build());
                YamlDocument.create(new File(getDataFolder(), "crates/ritka.yml"), getResource("crates/ritka.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().build(), DumperSettings.DEFAULT, UpdaterSettings.builder().build());
                YamlDocument.create(new File(getDataFolder(), "crates/legendas.yml"), getResource("crates/legendas.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().build(), DumperSettings.DEFAULT, UpdaterSettings.builder().build());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        Crates.loadCrates();

        getServer().getPluginManager().registerEvents(new CollectionListener(), this);
        getServer().getPluginManager().registerEvents(new FallingBlockListener(), this);
        getServer().getPluginManager().registerEvents(new ActivateFlare(), this);
        getServer().getPluginManager().registerEvents(new BlockPhysicsListener(), this);
        getServer().getPluginManager().registerEvents(new FireworkDamageListener(), this);


    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
        crates.forEach((key, value) -> value.collectCrate(null));
        ConfigManager.saveData();
    }
}
