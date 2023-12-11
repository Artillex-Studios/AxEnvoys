package com.artillexstudios.axenvoy.utils;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.user.User;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EditorListener implements Listener {

    @EventHandler
    public void onBlockBreakEvent(@NotNull final BlockBreakEvent event) {
        User user = User.USER_MAP.get(event.getPlayer().getUniqueId());
        if (user == null) return;
        Envoy editor = user.getEditor();
        if (editor == null) return;

        List<String> locations = editor.getConfig().getConfig().getStringList("pre-defined-spawns.locations");
        String serialized = Utils.serializeLocation(event.getBlock().getLocation());
        if (locations.contains(serialized)) {
            locations.remove(serialized);
            editor.getConfig().getConfig().set("pre-defined-spawns.locations", locations);

            User.USER_MAP.forEach(((uuid, user1) -> {
                if (user1.getEditor().equals(editor)) {
                    user1.getPlayer().sendBlockChange(event.getBlock().getLocation(), Material.AIR.createBlockData());
                }
            }));

            try {
                editor.getConfig().getConfig().save();
                editor.reload();
                event.getPlayer().sendMessage(StringUtils.formatToString(editor.getConfig().REMOVE_PREDEFINED));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onBlockPlaceEvent(@NotNull final BlockPlaceEvent event) {
        User user = User.USER_MAP.get(event.getPlayer().getUniqueId());
        if (user == null) return;
        Envoy editor = user.getEditor();
        if (editor == null) return;
        if (event.getBlockPlaced().getType() != Material.DIAMOND_BLOCK) return;

        List<String> locations = editor.getConfig().getConfig().getStringList("pre-defined-spawns.locations");

        String serialized = Utils.serializeLocation(event.getBlock().getLocation());
        if (locations.contains(serialized)) {
            return;
        }

        locations.add(serialized);
        event.setCancelled(true);
        editor.getConfig().getConfig().set("pre-defined-spawns.locations", locations);

        Scheduler.get().runLater(task -> {
            User.USER_MAP.forEach(((uuid, user1) -> {
                if (user1.getEditor() == null) return;
                if (user1.getEditor().equals(editor)) {
                    user1.getPlayer().sendBlockChange(event.getBlock().getLocation(), Material.DIAMOND_BLOCK.createBlockData());
                }
            }));
        }, 2);

        try {
            editor.getConfig().getConfig().save();
            editor.reload();
            event.getPlayer().sendMessage(StringUtils.formatToString(editor.getConfig().SET_PREDEFINED));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
