package com.artillexstudios.axenvoy.commands;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.config.impl.Config;
import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.envoy.Envoys;
import com.artillexstudios.axenvoy.envoy.SpawnedCrate;
import com.artillexstudios.axenvoy.locale.LocaleManager;
import com.artillexstudios.axenvoy.locale.LocaleString;
import com.artillexstudios.axenvoy.user.User;
import com.artillexstudios.axenvoy.utils.Utils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.math3.util.Pair;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.List;

@Command({"envoy", "axenvoy", "axen", "envoys"})
public class EnvoyCommand {

    @Subcommand("flare")
    @CommandPermission("axenvoy.command.flare")
    public void flare(CommandSender sender, Envoy envoy, @Default("me") Player receiver, @Default("1") int amount) {
        if (envoy == null) {
            sender.sendMessage(StringUtils.formatToString(Config.PREFIX + LocaleManager.getMessage(LocaleString.ENVOY_NOT_FOUND)));
            return;
        }

        ItemStack item = envoy.getFlare(amount);
        receiver.getInventory().addItem(item);
    }

    @Subcommand("start")
    @CommandPermission("axenvoy.command.start")
    public void start(CommandSender sender, Envoy envoy) {
        if (envoy == null) {
            sender.sendMessage(StringUtils.formatToString(Config.PREFIX + LocaleManager.getMessage(LocaleString.ENVOY_NOT_FOUND)));
            return;
        }

        envoy.start(null);
    }

    @Subcommand("stop")
    @CommandPermission("axenvoy.command.stop")
    public void stop(CommandSender sender, Envoy envoy) {
        if (envoy == null) {
            sender.sendMessage(StringUtils.formatToString(Config.PREFIX + LocaleManager.getMessage(LocaleString.ENVOY_NOT_FOUND)));
            return;
        }

        if (!envoy.isActive()) return;
        envoy.stop();
    }

    @Subcommand("stopall")
    @CommandPermission("axenvoy.command.stopall")
    public void stopAll(CommandSender sender) {
        for (Envoy envoy : Envoys.getTypes().values()) {
            if (!envoy.isActive()) return;
            envoy.stop();
        }
    }

    @Subcommand("reload")
    @CommandPermission("axenvoy.command.reload")
    public void reload(CommandSender sender) {
        sender.sendMessage(StringUtils.formatToString(Config.PREFIX + LocaleManager.getMessage(LocaleString.RELOAD).replace("%time%", String.valueOf(AxEnvoyPlugin.getInstance().reloadWithTime()))));
    }

    @Subcommand("center")
    @CommandPermission("axenvoy.command.center")
    public void center(Player sender, Envoy envoy) {
        if (envoy == null) {
            sender.sendMessage(StringUtils.formatToString(Config.PREFIX + LocaleManager.getMessage(LocaleString.ENVOY_NOT_FOUND)));
            return;
        }

        envoy.getConfig().getConfig().set("random-spawn.center", Utils.serializeLocation(sender.getLocation()));

        try {
            envoy.getConfig().getConfig().save();
            envoy.getConfig().reload();
            sender.sendMessage(StringUtils.formatToString(Config.PREFIX + LocaleManager.getMessage(LocaleString.CENTER_SET)));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Subcommand("editor")
    @CommandPermission("axenvoy.command.editor")
    public void editor(Player sender, @Optional Envoy envoy) {
        User user = User.USER_MAP.get(sender.getUniqueId());
        Envoy editor = user.getEditor();

        if (envoy == null || envoy.equals(editor)) {
            user.setEditor(null);
            if (editor != null) {
                List<Location> locations = editor.getConfig().PREDEFINED_LOCATIONS.stream().map(Utils::deserializeLocation).toList();

                for (Location location : locations) {
                    sender.sendBlockChange(location, Material.AIR.createBlockData());
                }

                sender.getInventory().remove(new ItemStack(Material.DIAMOND_BLOCK, 1));
                sender.sendMessage(StringUtils.formatToString(Config.PREFIX + LocaleManager.getMessage(LocaleString.EDITOR_LEAVE)));
            }
            return;
        }

        user.setEditor(envoy);
        List<Location> locations = envoy.getConfig().PREDEFINED_LOCATIONS.stream().map(Utils::deserializeLocation).toList();

        for (Location location : locations) {
            sender.sendBlockChange(location, Material.DIAMOND_BLOCK.createBlockData());
        }

        sender.getInventory().addItem(new ItemStack(Material.DIAMOND_BLOCK));

        sender.sendMessage(StringUtils.formatToString(Config.PREFIX + LocaleManager.getMessage(LocaleString.EDITOR_JOIN)));
    }

    @Subcommand("coords")
    @CommandPermission("axenvoy.command.coords")
    public void coords(CommandSender sender, Envoy envoy) {
        if (envoy == null) {
            sender.sendMessage(StringUtils.formatToString(Config.PREFIX + LocaleManager.getMessage(LocaleString.ENVOY_NOT_FOUND)));
            return;
        }
        Audience audience = BukkitAudiences.create(AxEnvoyPlugin.getInstance()).sender(sender);

        for (SpawnedCrate spawnedCrate : envoy.getSpawnedCrates()) {
            Location finish = spawnedCrate.getFinishLocation();
            audience.sendMessage(MiniMessage.miniMessage().deserialize("<click:run_command:'/tp %x% %y% %z%'><hover:show_text:'<color:#7df0ff>Click to teleport!</color>'><white>Crate</white> %crate% %x% %y% %z%.</hover></click>"
                    .replace("%x%", String.valueOf(finish.getBlockX()))
                    .replace("%y%", String.valueOf(finish.getBlockY()))
                    .replace("%z%", String.valueOf(finish.getBlockZ()))
                    .replace("%crate%", String.valueOf(spawnedCrate.getHandle().getName()))
            ));
        }
    }

    @Subcommand("toggle")
    @CommandPermission("axenvoy.command.toggle")
    public void toggle(Player sender) {
        if (sender.getPersistentDataContainer().has(AxEnvoyPlugin.MESSAGE_KEY, PersistentDataType.BYTE)) {
            sender.getPersistentDataContainer().remove(AxEnvoyPlugin.MESSAGE_KEY);
            sender.sendMessage(StringUtils.formatToString(Config.PREFIX + LocaleManager.getMessage(LocaleString.ENVOY_START_MESSAGE_ON)));
            return;
        }

        sender.getPersistentDataContainer().set(AxEnvoyPlugin.MESSAGE_KEY, PersistentDataType.BYTE, (byte) 0);
        sender.sendMessage(StringUtils.formatToString(Config.PREFIX + LocaleManager.getMessage(LocaleString.ENVOY_START_MESSAGE_OFF)));
    }

    @Subcommand("time")
    @CommandPermission("axenvoy.command.time")
    public void time(CommandSender sender) {
        Pair<Envoy, Long> pair = Utils.getNextEnvoy();
        sender.sendMessage(StringUtils.formatToString(Config.PREFIX + LocaleManager.getMessage(LocaleString.TIME_START).replace("%time%", Utils.fancyTime(pair.getSecond(), pair.getFirst())).replace("%envoy%", pair.getFirst().getName())));
    }
}
