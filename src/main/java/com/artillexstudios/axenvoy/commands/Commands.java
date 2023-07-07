package com.artillexstudios.axenvoy.commands;

import cloud.commandframework.Command;
import cloud.commandframework.CommandTree;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.bukkit.parsers.MaterialArgument;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.FilteringCommandSuggestionProcessor;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.config.ConfigManager;
import com.artillexstudios.axenvoy.envoy.Crate;
import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.envoy.EnvoyLoader;
import com.artillexstudios.axenvoy.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.function.Function;

import static cloud.commandframework.arguments.standard.StringArgument.builder;

public class Commands {
    private BukkitCommandManager<CommandSender> manager;

    public Commands(@NotNull AxEnvoyPlugin plugin) {
        final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction =
                AsynchronousCommandExecutionCoordinator.<CommandSender>builder().build();

        final Function<CommandSender, CommandSender> mapperFunction = Function.identity();
        try {
            manager = new PaperCommandManager<>(
                    AxEnvoyPlugin.getInstance(),
                    executionCoordinatorFunction,
                    mapperFunction,
                    mapperFunction
            );
        } catch (final Exception e) {
            plugin.getLogger().severe("Failed to initialize the command this.manager");
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        manager.commandSuggestionProcessor(new FilteringCommandSuggestionProcessor<>(
                FilteringCommandSuggestionProcessor.Filter.<CommandSender>contains(true).andTrimBeforeLastSpace()
        ));

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

        this.manager.command(commands
                .literal("flare")
                .handler(c -> {
                    ((Player) c.getSender()).getInventory().addItem(EnvoyLoader.envoys.get(0).getFlare());
                }));
    }
}
