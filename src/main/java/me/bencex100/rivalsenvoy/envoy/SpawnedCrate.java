package me.bencex100.rivalsenvoy.envoy;

import dev.dejvokep.boostedyaml.YamlDocument;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.bencex100.rivalsenvoy.RivalsEnvoy;
import me.bencex100.rivalsenvoy.config.ConfigManager;
import me.bencex100.rivalsenvoy.crateconfig.Crate;
import me.bencex100.rivalsenvoy.crateconfig.Crates;
import me.bencex100.rivalsenvoy.listeners.FallingBlockListener;
import me.bencex100.rivalsenvoy.utils.ColorUtils;
import me.bencex100.rivalsenvoy.utils.FallingBlockChecker;
import me.bencex100.rivalsenvoy.utils.Utils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;

public class SpawnedCrate {
    private final YamlDocument config = ConfigManager.getCnf("config");
    private final Location loc;
    private Hologram holo;
    private final String type;
    private final Crate cr;

    public SpawnedCrate(@NotNull Location loc, String type) {
        this.loc = loc.getBlock().getLocation();
        this.type = type;
        this.cr = Crates.getCrate(type);
    }

    public void load() {
        Location loc2 = loc.getBlock().getLocation();

        if (!loc2.isChunkLoaded()) return;
        if (config.getDouble("fall-height") == 0.0d) {
            land();
            return;
        }

        loc2.add(0, config.getDouble("fall-height"), 0);
        FallingBlock falling = loc.getWorld().spawnFallingBlock(loc2, Material.getMaterial(config.getString("falling-block-material")).createBlockData());
        falling.setDropItem(false);
        falling.setVelocity(new Vector(0, config.getDouble("falling-speed"), 0));
        FallingBlockListener.fallingBlocks.put(falling, this);
        new FallingBlockChecker().checkIfAlive(falling);
    }

    public void land() {
        Location loc2 = loc.getBlock().getLocation();
        loc2.getBlock().setType(cr.getMaterial());
        List<String> lines = cr.getHologramLines();
        lines.replaceAll(input -> LegacyComponentSerializer.legacyAmpersand().serialize(MiniMessage.miniMessage().deserialize(input)));
        loc2.add(0.5, 0D, 0.5);
        EnvoyHandler.crates.put(loc, this);
        loc2.setY(loc2.getY() + cr.getHologramHeight());

        if (DHAPI.getHologram("RIVALSENVOY-" + loc2.getBlockX() + loc2.getBlockY() + loc2.getBlockZ()) != null) {
            DHAPI.getHologram("RIVALSENVOY-" + loc2.getBlockX() + loc2.getBlockY() + loc2.getBlockZ()).delete();
        }

        holo = DHAPI.createHologram("RIVALSENVOY-" + loc2.getBlockX() + loc2.getBlockY() + loc2.getBlockZ(), loc2, lines);
    }

    public void collectCrate(@Nullable Player p) {
        loc.getBlock().setType(Material.AIR);
        holo.delete();

        if (p == null) return;

        final HashMap<String, Double> map = new HashMap<>();
        for (Object j : cr.getCrateConfig().getSection("reward-commands").getKeys()) {
            map.put(j.toString(), cr.getCrateConfig().getDouble("reward-commands." + j + ".chance"));
        }

        String rw = Utils.randomValue(map);
        p.sendMessage(ColorUtils.deserialize(config.getString("prefix") + cr.getCrateConfig().getString("reward-commands." + rw + ".message")));
        for (String i : cr.getCrateConfig().getStringList("reward-commands." + rw + ".commands")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), i.replace("%player_name%", p.getName()).replace("%player%", p.getName()));
        }

        if (cr.isFireworkEnabled()) {
            String hex = cr.getFireworkColor();
            Location loc2 = loc.clone();
            loc2.add(0.5, 0.5, 0.5);
            Firework fw = (Firework) loc.getWorld().spawnEntity(loc2, EntityType.FIREWORK);
            FireworkMeta meta = fw.getFireworkMeta();
            Color color = new Color(Integer.valueOf(hex.substring(1, 3), 16), Integer.valueOf(hex.substring(3, 5), 16), Integer.valueOf(hex.substring(5, 7), 16));
            meta.addEffect(FireworkEffect.builder().withColor(org.bukkit.Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue())).build());
            meta.setPower(0);
            fw.setFireworkMeta(meta);
            fw.setMetadata("RIVALSENVOY", new FixedMetadataValue(RivalsEnvoy.getInstance(), true));
            fw.detonate();
        }
}
}
