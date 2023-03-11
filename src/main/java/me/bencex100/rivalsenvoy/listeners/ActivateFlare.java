package me.bencex100.rivalsenvoy.listeners;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.bencex100.rivalsenvoy.RivalsEnvoy;
import me.bencex100.rivalsenvoy.config.Config;
import me.bencex100.rivalsenvoy.utils.Utils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;

public class ActivateFlare implements Listener {
    private final YamlDocument config = Config.getCnf("config");
    private final YamlDocument messages = Config.getCnf("messages");
    HashMap<Player, Long> cd = new HashMap<>();

    @EventHandler
    private void onPlayerRightClick(PlayerInteractEvent e) {
        if (e.getAction().isRightClick()) {
            if (e.getHand() == EquipmentSlot.HAND) {
                if (e.getItem() != null) {
                    if (MiniMessage.miniMessage().serialize(e.getItem().getItemMeta().displayName()).equals(config.getString("flare.name"))) {

                        if (cd.containsKey(e.getPlayer())) {
                            if (System.currentTimeMillis() - cd.get(e.getPlayer()) < config.getLong("flare-cooldown-in-seconds") * 1000) {
                                e.getPlayer().sendRichMessage(config.getString("prefix") + messages.getString("error.flare-cooldown").replace("%time%", Utils.fancyTime(config.getLong("flare-cooldown-in-seconds") * 1000 - System.currentTimeMillis() + cd.get(e.getPlayer()))));
                                return;
                            }
                        }
                        cd.put(e.getPlayer(), System.currentTimeMillis());
                        int val = e.getPlayer().getInventory().getItemInMainHand().getAmount() - 1;
                        if (e.getPlayer().getInventory().getItemInMainHand().getAmount() - 1 > 0) {
                            e.getPlayer().getInventory().getItemInMainHand().setAmount(val);
                        } else {
                            e.getPlayer().getInventory().getItemInMainHand().setAmount(0);
                        }
                        RivalsEnvoy.getEvh().startEnvoy();
                        e.getPlayer().sendRichMessage(messages.getString("success.started"));
                    }
                }
            }
        }
    }
}
