package me.bencex100.rivalsenvoy.listeners;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.bencex100.rivalsenvoy.RivalsEnvoy;
import me.bencex100.rivalsenvoy.config.Config;
import me.bencex100.rivalsenvoy.utils.Utils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;

public class ActivateFlare implements Listener {
    private final YamlDocument config = Config.getCnf("config");
    private final YamlDocument messages = Config.getCnf("messages");
    HashMap<Player, Long> cd = new HashMap<>();

    @EventHandler
    private void onPlayerInteractEvent(PlayerInteractEvent e) {
        if (!e.getAction().isRightClick()) return;
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getItem() == null) return;
        if (e.getItem().getItemMeta() == null) return;
        ItemMeta meta = e.getItem().getItemMeta();
        if (meta.displayName() == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(RivalsEnvoy.getInstance(), "RIVALSENVOY");

        boolean isFlare = MiniMessage.miniMessage().serialize(e.getItem().getItemMeta().displayName()).equals(config.getString("flare.name"));
        if (container.has(key, PersistentDataType.STRING) && container.get(key, PersistentDataType.STRING).equals("flare"))
            isFlare = true;
        if (!isFlare) return;

        if (cd.containsKey(e.getPlayer()) && System.currentTimeMillis() - cd.get(e.getPlayer()) < config.getLong("flare-cooldown-in-seconds") * 1000) {
            e.getPlayer().sendRichMessage(config.getString("prefix") + messages.getString("error.flare-cooldown").replace("%time%", Utils.fancyTime(config.getLong("flare-cooldown-in-seconds") * 1000 - System.currentTimeMillis() + cd.get(e.getPlayer()))));
            return;
        }
        cd.put(e.getPlayer(), System.currentTimeMillis());
        e.getPlayer().getInventory().getItemInMainHand().setAmount(e.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
        RivalsEnvoy.getEvh().startEnvoy();
        e.getPlayer().sendRichMessage(messages.getString("success.started"));
    }
}
