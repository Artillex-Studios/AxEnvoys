package com.artillexstudios.axenvoy.rewards;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.envoy.Envoy;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public record Reward(double chance, List<String> commands, List<String> messages, List<Map<Object, Object>> items, Map<Object, Object> requiredItem, List<String> sounds, String name) {

    public void execute(@NotNull Player player, @NotNull Envoy envoy) {
        for (String message : this.messages) {
            message = message.replace("%player%", player.getName()).replace("%player_name%", player.getName());
            if (AxEnvoyPlugin.getInstance().isPlaceholderApi()) {
                message = PlaceholderAPI.setPlaceholders(player, message);
            }

            if (envoy.getConfig().USE_PREFIX) {
                player.sendMessage(StringUtils.formatToString(envoy.getConfig().PREFIX + message));
            } else {
                player.sendMessage(StringUtils.formatToString(message));
            }
        }

        for (String command : this.commands) {
            command = command.replace("%player%", player.getName()).replace("%player_name%", player.getName());
            if (AxEnvoyPlugin.getInstance().isPlaceholderApi()) {
                command = PlaceholderAPI.setPlaceholders(player, command);
            }

            String finalCommand = command;
            Scheduler.get().run(task -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
            });
        }

        for (Map<Object, Object> item : this.items) {
            player.getInventory().addItem(ItemBuilder.create(item).get());
        }

        for (String sound : sounds) {
            String[] split = sound.split(";");
            float volume = Float.parseFloat(split[1]);
            float pitch = Float.parseFloat(split[2]);
            player.getWorld().playSound(player, Sound.valueOf(split[0].toUpperCase(Locale.ENGLISH)), volume, pitch);
        }
    }

    public boolean doesMatchRequired(ItemStack itemStack) {
        if (this.requiredItem == null) {
            return false;
        }

        ItemStack requiredStack = ItemBuilder.create(requiredItem).get();
        return itemStack.isSimilar(requiredStack);
    }
}
