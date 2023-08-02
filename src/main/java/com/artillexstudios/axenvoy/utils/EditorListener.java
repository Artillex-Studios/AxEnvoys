package com.artillexstudios.axenvoy.utils;

import com.artillexstudios.axenvoy.config.ConfigManager;
import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.user.User;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EditorListener implements Listener {

    @EventHandler
    public void onBlockBreakEvent(@NotNull final BlockBreakEvent event) {
        User user = User.USER_MAP.get(event.getPlayer().getUniqueId());
        if (user == null) return;
        Envoy editor = user.getEditor();
        if (editor == null) return;

        List<String> locations = editor.getDocument().getStringList("pre-defined-spawns.locations", new ArrayList<String>());
        String serialized = Utils.serializeLocation(event.getBlock().getLocation());
        if (locations.contains(serialized)) {
            locations.remove(serialized);
            editor.getDocument().set("pre-defined-spawns.locations", locations);

            User.USER_MAP.forEach(((uuid, user1) -> {
                if (user1.getEditor() == editor) {
                    user1.getPlayer().sendBlockChange(event.getBlock().getLocation(), Material.AIR.createBlockData());
                }
            }));

            try {
                editor.getDocument().save();
                ConfigManager.reload();
                event.getPlayer().sendMessage(editor.getMessage("remove-predefined"));
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

        List<String> locations = editor.getDocument().getStringList("pre-defined-spawns.locations", new ArrayList<String>());

        locations.add(Utils.serializeLocation(event.getBlock().getLocation()));
        editor.getDocument().set("pre-defined-spawns.locations", locations);

        User.USER_MAP.forEach(((uuid, user1) -> {
            if (user1.getEditor() == editor) {
                user1.getPlayer().sendBlockChange(event.getBlock().getLocation(), Material.DIAMOND_BLOCK.createBlockData());
            }
        }));

        try {
            editor.getDocument().save();
            ConfigManager.reload();
            event.getPlayer().sendMessage(editor.getMessage("remove-predefined"));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
