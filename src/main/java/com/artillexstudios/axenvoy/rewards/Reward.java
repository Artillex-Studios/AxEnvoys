package com.artillexstudios.axenvoy.rewards;

import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.utils.StringUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record Reward(double chance, List<String> commands, List<String> messages) {

    public void execute(@NotNull Player player, @NotNull Envoy envoy) {
        for (String message : this.messages) {
            message = message.replace("%player%", player.getName()).replace("%player_name%", player.getName());
            if (AxEnvoyPlugin.getInstance().isPlaceholderApi()) {
                message = PlaceholderAPI.setPlaceholders(player, message);
            }

            if (envoy.isUseRewardPrefix()) {
                player.sendMessage(String.format("%s%s", envoy.getMessage("prefix"), StringUtils.format(message)));
            } else {
                player.sendMessage(StringUtils.format(message));
            }
        }

        for (String command : this.commands) {
            command = command.replace("%player%", player.getName()).replace("%player_name%", player.getName());
            if (AxEnvoyPlugin.getInstance().isPlaceholderApi()) {
                command = PlaceholderAPI.setPlaceholders(player, command);
            }

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }
}
