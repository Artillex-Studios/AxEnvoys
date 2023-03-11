package me.bencex100.rivalsenvoy.commands;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import me.bencex100.rivalsenvoy.RivalsEnvoy;
import me.bencex100.rivalsenvoy.config.Config;
import me.bencex100.rivalsenvoy.utils.EnvoyHandler;
import me.bencex100.rivalsenvoy.utils.Utils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;

public class Commands {
    private final YamlDocument config = Config.getCnf("config");
    private final YamlDocument data = Config.getCnf("data");
    private final YamlDocument messages = Config.getCnf("messages");
    public void register() {
        new CommandTree("rivalsenvoy")
                .withAliases("envoy", "renvoy", "envoys")
                .then(new LiteralArgument("help")
                        .executes((sender, objects) -> {
                            messages.getStringList("help").forEach(sender::sendRichMessage);
                        })
                )
                .then(new LiteralArgument("reload")
                        .withPermission("rivalsenvoy.reload")
                        .executes((sender, objects) -> {
                            Config.reloadCfs();
                            sender.sendRichMessage(messages.getString("success.reloaded"));
                        })
                )
                .then(new LiteralArgument("center")
                        .withPermission("rivalsenvoy.admin")
                        .executes((sender, objects) -> {
                            Player p = (Player) sender;
                            data.set("data.center", p.getLocation().serialize());
                            sender.sendRichMessage(messages.getString("success.set-center"));
                            try {
                                data.save();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                )
                .then(new LiteralArgument("start")
                        .withPermission("rivalsenvoy.admin")
                        .executes((sender, objects) -> {
                            RivalsEnvoy.getEvh().startEnvoy();
                            sender.sendRichMessage(messages.getString("success.started"));
                        })
                )
                .then(new LiteralArgument("stop")
                        .withPermission("rivalsenvoy.admin")
                        .executes((sender, objects) -> {
                            EnvoyHandler.crates.forEach((key, value) -> value.collectCrate(null));
                            EnvoyHandler.crates.clear();
                            sender.sendRichMessage(messages.getString("success.stopped"));
                        })
                )
                .then(new LiteralArgument("flare")
                        .withPermission("rivalsenvoy.admin")
                        .then(new PlayerArgument("name")
                                .then(new IntegerArgument("amount")
                                        .executes((sender, args) -> {
                                            if (!config.getBoolean("flare.enabled")) return;
                                            Player p = (Player) args[0];
                                            Integer am = (Integer) args[1];
                                            ItemStack it = Utils.createItem(Material.getMaterial(config.getString("flare.material")), am, config.getString("flare.name"), new ArrayList<>(config.getStringList("flare.lore")));
                                            p.getInventory().addItem(it);
                                        })
                                )
                                .executes((sender, objects) -> {
                                    sender.sendRichMessage(config.getString("prefix") + PlaceholderAPI.setPlaceholders((Player) sender, messages.getString("error.incorrect-amount")));
                                })
                        )
                        .executes((sender, objects) -> {
                            sender.sendRichMessage(config.getString("prefix") + PlaceholderAPI.setPlaceholders((Player) sender, messages.getString("error.player-not-found")));
                        })
                )
                .executes((sender, objects) -> {
                    sender.sendRichMessage(config.getString("prefix") + PlaceholderAPI.setPlaceholders((Player) sender, messages.getString("command.info-inactive")));
                })
                .register();
    }
}
