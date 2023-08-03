package com.artillexstudios.axenvoy;

import com.artillexstudios.axenvoy.commands.Commands;
import com.artillexstudios.axenvoy.config.ConfigManager;
import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.envoy.EnvoyLoader;
import com.artillexstudios.axenvoy.listeners.ActivateFlare;
import com.artillexstudios.axenvoy.listeners.BlockPhysicsListener;
import com.artillexstudios.axenvoy.listeners.CollectionListener;
import com.artillexstudios.axenvoy.listeners.FireworkDamageListener;
import com.artillexstudios.axenvoy.placeholders.Placeholders;
import com.artillexstudios.axenvoy.user.User;
import com.artillexstudios.axenvoy.utils.EditorListener;
import com.artillexstudios.axenvoy.utils.FallingBlockChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AxEnvoyPlugin extends JavaPlugin {
    private static AxEnvoyPlugin instance;
    private boolean placeholderApi;

    public static AxEnvoyPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        ConfigManager.reload();
        new Commands(this);
        this.placeholderApi = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (placeholderApi) {
            new Placeholders().register();
        }

        Bukkit.getOnlinePlayers().forEach(User::new);
        User.listen();
        new FallingBlockChecker();
        Bukkit.getPluginManager().registerEvents(new ActivateFlare(), this);
        Bukkit.getPluginManager().registerEvents(new BlockPhysicsListener(), this);
        Bukkit.getPluginManager().registerEvents(new CollectionListener(), this);
        Bukkit.getPluginManager().registerEvents(new FireworkDamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new EditorListener(), this);
        new Metrics(this, 19146);
    }

    @Override
    public void onDisable() {
        for (Envoy envoy : EnvoyLoader.envoys.values()) {
            if (!envoy.isActive()) continue;
            envoy.stop();
        }
    }

    public boolean isPlaceholderApi() {
        return this.placeholderApi;
    }
}
