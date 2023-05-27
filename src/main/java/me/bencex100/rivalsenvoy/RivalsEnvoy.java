package me.bencex100.rivalsenvoy;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIConfig;
import me.bencex100.rivalsenvoy.commands.Commands;
import me.bencex100.rivalsenvoy.config.ConfigManager;
import me.bencex100.rivalsenvoy.listeners.ActivateFlare;
import me.bencex100.rivalsenvoy.listeners.BlockPhysicsListener;
import me.bencex100.rivalsenvoy.listeners.CollectionListener;
import me.bencex100.rivalsenvoy.listeners.FallingBlockListener;
import me.bencex100.rivalsenvoy.listeners.FireworkDamageListener;
import me.bencex100.rivalsenvoy.utils.Utils;
import org.bukkit.plugin.java.JavaPlugin;

import static me.bencex100.rivalsenvoy.envoy.EnvoyHandler.crates;

public final class RivalsEnvoy extends JavaPlugin {
    private static RivalsEnvoy instance;

    public static RivalsEnvoy getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        new ConfigManager().loadConfig();
        CommandAPI.onLoad(new CommandAPIConfig());
        CommandAPI.onEnable(this);
        new Commands().register();
        new Utils().updateBlackList();

        getServer().getPluginManager().registerEvents(new CollectionListener(), this);
        getServer().getPluginManager().registerEvents(new FallingBlockListener(), this);
        getServer().getPluginManager().registerEvents(new ActivateFlare(), this);
        getServer().getPluginManager().registerEvents(new BlockPhysicsListener(), this);
        getServer().getPluginManager().registerEvents(new FireworkDamageListener(), this);

    }

    @Override
    public void onDisable() {
        crates.forEach((key, value) -> value.collectCrate(null));
        CommandAPI.onDisable();
        ConfigManager.saveData();
    }
}
