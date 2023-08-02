package com.artillexstudios.axenvoy.commands;

import cloud.commandframework.Command;
import cloud.commandframework.CommandTree;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.FilteringCommandSuggestionProcessor;
import cloud.commandframework.paper.PaperCommandManager;
import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.config.ConfigManager;
import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.envoy.EnvoyLoader;
import com.artillexstudios.axenvoy.envoy.SpawnedCrate;
import com.artillexstudios.axenvoy.user.User;
import com.artillexstudios.axenvoy.utils.StringUtils;
import com.artillexstudios.axenvoy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Commands {
    private BukkitCommandManager<CommandSender> manager;

    public Commands(@NotNull AxEnvoyPlugin plugin) {
        final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction = AsynchronousCommandExecutionCoordinator.<CommandSender>builder().build();

        final Function<CommandSender, CommandSender> mapperFunction = Function.identity();
        try {
            manager = new PaperCommandManager<>(AxEnvoyPlugin.getInstance(), executionCoordinatorFunction, mapperFunction, mapperFunction);
        } catch (final Exception e) {
            plugin.getLogger().severe("Failed to initialize the command this.manager");
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        manager.commandSuggestionProcessor(new FilteringCommandSuggestionProcessor<>(FilteringCommandSuggestionProcessor.Filter.<CommandSender>contains(true).andTrimBeforeLastSpace()));

        if (manager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            manager.registerBrigadier();
        }

        if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            ((PaperCommandManager<CommandSender>) manager).registerAsynchronousCompletions();
        }

        this.register();
    }

    private void register() {
        Command.Builder<CommandSender> commands = this.manager.commandBuilder("envoy", "axenvoy");

        CommandArgument<CommandSender, String> argument = StringArgument.<CommandSender>builder("envoy").withSuggestionsProvider((context, string) -> EnvoyLoader.envoys.keySet().stream().toList()).build();
        CommandArgument<CommandSender, String> optional = StringArgument.<CommandSender>builder("envoy").withSuggestionsProvider((context, string) -> EnvoyLoader.envoys.keySet().stream().toList()).asOptional().build();

        this.manager.command(commands.literal("flare").permission("axenvoy.command.flare").argument(argument.copy()).argument(PlayerArgument.optional("player")).argument(IntegerArgument.optional("amount", 1)).handler(c -> {
            String envoyName = c.get("envoy");
            Optional<Player> playerArg = c.getOptional("player");
            Envoy envoy = EnvoyLoader.envoys.get(envoyName);
            if (envoy == null) {
                c.getSender().sendMessage(StringUtils.format(String.format("%s%s", ConfigManager.getLang().getString("messages.prefix"), ConfigManager.getLang().getString("messages.no-envoy-found"))));
                return;
            }

            playerArg.ifPresent(player -> {
                if (envoy.getName().equals(envoyName)) {
                    ItemStack item = envoy.getFlare().clone();
                    Optional<Integer> count = c.getOptional("amount");
                    count.ifPresent(item::setAmount);
                    player.getInventory().addItem(item);
                }
            });

            if (c.getSender() instanceof Player player && playerArg.isEmpty()) {
                if (envoy.getName().equals(envoyName)) {
                    ItemStack item = envoy.getFlare().clone();
                    Optional<Integer> count = c.getOptional("amount");
                    count.ifPresent(item::setAmount);
                    player.getInventory().addItem(item);
                }
            }
        })).command(commands.literal("start").argument(argument.copy()).permission("axenvoy.command.start").handler(c -> {
            String envoyName = c.get("envoy");
            Envoy envoy = EnvoyLoader.envoys.get(envoyName);
            if (envoy == null) {
                c.getSender().sendMessage(StringUtils.format(String.format("%s%s", ConfigManager.getLang().getString("messages.prefix"), ConfigManager.getLang().getString("messages.no-envoy-found"))));
                return;
            }

            envoy.start(null);
        })).command(commands.literal("stop").argument(argument.copy()).permission("axenvoy.command.stop").handler(c -> {
            String envoyName = c.get("envoy");
            Bukkit.getScheduler().runTask(AxEnvoyPlugin.getInstance(), () -> {
                Envoy envoy = EnvoyLoader.envoys.get(envoyName);
                if (envoy == null) {
                    c.getSender().sendMessage(StringUtils.format(String.format("%s%s", ConfigManager.getLang().getString("messages.prefix"), ConfigManager.getLang().getString("messages.no-envoy-found"))));
                    return;
                }

                if (!envoy.isActive()) return;
                Iterator<SpawnedCrate> crates = envoy.getSpawnedCrates().iterator();
                while (crates.hasNext()) {
                    SpawnedCrate crate = crates.next();
                    crate.claim(null, envoy, false);
                    crates.remove();
                }
            });
        })).command(commands.literal("stopall").permission("axenvoy.command.stopall").handler(c -> Bukkit.getScheduler().runTask(AxEnvoyPlugin.getInstance(), () -> {
            for (Envoy envoy : EnvoyLoader.envoys.values()) {
                if (!envoy.isActive()) return;
                Iterator<SpawnedCrate> crates = envoy.getSpawnedCrates().iterator();
                while (crates.hasNext()) {
                    SpawnedCrate crate = crates.next();
                    crate.claim(null, envoy, false);
                    crates.remove();
                }
            }
        }))).command(commands.literal("reload").permission("axenvoy.command.reload").handler(c -> {
            long now = System.currentTimeMillis();
            Bukkit.getScheduler().runTask(AxEnvoyPlugin.getInstance(), () -> {
                for (Envoy envoy : EnvoyLoader.envoys.values()) {
                    if (!envoy.isActive()) continue;
                    Iterator<SpawnedCrate> iterator = envoy.getSpawnedCrates().iterator();
                    while (iterator.hasNext()) {
                        SpawnedCrate next = iterator.next();
                        next.claim(null, envoy, false);
                        iterator.remove();
                    }
                }

                ConfigManager.reload();
                c.getSender().sendMessage(StringUtils.format(ConfigManager.getLang().getString("messages.reload").replace("%time%", String.valueOf((System.currentTimeMillis() - now)))));
            });
        })).command(commands.literal("center").permission("axenvoy.command.center").argument(argument.copy()).senderType(Player.class).handler(c -> {
            String envoyName = c.get("envoy");
            Envoy envoy = EnvoyLoader.envoys.get(envoyName);
            if (envoy == null) {
                c.getSender().sendMessage(StringUtils.format(String.format("%s%s", ConfigManager.getLang().getString("messages.prefix"), ConfigManager.getLang().getString("messages.no-envoy-found"))));
                return;
            }


            if (envoy.getName().equals(envoyName)) {
                envoy.getDocument().set("random-spawn.center", Utils.serializeLocation(((Player) c.getSender()).getLocation()));

                try {
                    envoy.getDocument().save();
                    ConfigManager.reload();
                    c.getSender().sendMessage(envoy.getMessage("set-center"));
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }

        })).command(commands.literal("definedspawn").permission("axenvoy.command.definedspawn").argument(argument.copy()).handler(c -> {
            String envoyName = c.get("envoy");
            Envoy envoy = EnvoyLoader.envoys.get(envoyName);
            if (envoy == null) {
                c.getSender().sendMessage(StringUtils.format(String.format("%s%s", ConfigManager.getLang().getString("messages.prefix"), ConfigManager.getLang().getString("messages.no-envoy-found"))));
                return;
            }


            if (envoy.getName().equals(envoyName)) {
                List<String> locations = envoy.getDocument().getStringList("pre-defined-spawns.locations", new ArrayList<>());
                locations.add(Utils.serializeLocation(((Player) c.getSender()).getLocation()));
                envoy.getDocument().set("pre-defined-spawns.locations", locations);

                try {
                    envoy.getDocument().save();
                    ConfigManager.reload();
                    c.getSender().sendMessage(envoy.getMessage("set-predefined"));
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }

        })).command(commands.literal("editor").permission("axenvoy.command.editor").senderType(Player.class).argument(optional.copy()).handler(c -> {
            Optional<String> envoyName = c.getOptional("envoy");
            Player sender = (Player) c.getSender();
            User user = User.USER_MAP.get(sender.getUniqueId());
            if (envoyName.isEmpty()) {
                Envoy envoy = user.getEditor();
                if (envoy == null) {
                    sender.sendMessage(StringUtils.format(String.format("%s%s", ConfigManager.getLang().getString("messages.prefix"), ConfigManager.getLang().getString("messages.no-envoy-found"))));
                    return;
                }

                List<Location> locations = envoy.getDocument().getStringList("pre-defined-spawns.locations", new ArrayList<>()).stream().map(Utils::deserializeLocation).toList();
                for (Location location : locations) {
                    sender.sendBlockChange(location, Material.AIR.createBlockData());
                }

                user.setEditor(null);
                sender.sendMessage(String.format("%s%s", envoy.getMessage("prefix"), envoy.getMessage("editor.leave")));
                return;
            }

            Envoy envoy = EnvoyLoader.envoys.get(envoyName.get());
            if (envoy == null) {
                sender.sendMessage(StringUtils.format(String.format("%s%s", ConfigManager.getLang().getString("messages.prefix"), ConfigManager.getLang().getString("messages.no-envoy-found"))));
                return;
            }

            if (envoy.getName().equals(envoyName.get())) {
                if (user.getEditor() != null) {
                    List<Location> locations = envoy.getDocument().getStringList("pre-defined-spawns.locations", new ArrayList<>()).stream().map(Utils::deserializeLocation).toList();
                    for (Location location : locations) {
                        sender.sendBlockChange(location, Material.AIR.createBlockData());
                    }

                    user.setEditor(null);
                    sender.sendMessage(String.format("%s%s", envoy.getMessage("prefix"), envoy.getMessage("editor.leave")));

                    return;
                }

                user.setEditor(envoy);
                List<Location> locations = envoy.getDocument().getStringList("pre-defined-spawns.locations", new ArrayList<>()).stream().map(Utils::deserializeLocation).toList();

                for (Location location : locations) {
                    sender.sendBlockChange(location, Material.DIAMOND_BLOCK.createBlockData());
                }

                sender.getInventory().addItem(new ItemStack(Material.DIAMOND_BLOCK));

                sender.sendMessage(String.format("%s%s", envoy.getMessage("prefix"), envoy.getMessage("editor.join")));
            }
        }));
    }
}
