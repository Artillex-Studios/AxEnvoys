package com.artillexstudios.axenvoy.rewards;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record CommandReward(double chance, List<String> commands) {

    public void execute(@NotNull Player player) {
        for (String command : this.commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPI.setPlaceholders(player, command.replace("%player%", player.getName()).replace("%player_name%", player.getName())));
        }
    }
}
