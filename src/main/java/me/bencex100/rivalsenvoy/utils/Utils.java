package me.bencex100.rivalsenvoy.utils;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.bencex100.rivalsenvoy.config.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Utils {

    @NotNull
    public static Location deserializeLocation(@NotNull YamlDocument file, String path) {
        World world = Bukkit.getWorld(file.getString(path + ".world"));
        float x = file.getFloat(path + ".x");
        float y = file.getFloat(path + ".y");
        float z = file.getFloat(path + ".z");
        float yaw = file.getFloat(path + ".yaw");
        float pitch = file.getFloat(path + ".pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static String randomValue(HashMap<String, Double> map) {
        List<Pair<String,Double>> list = new ArrayList<>();
        map.forEach((key, value) -> list.add(new Pair<>(key, value)));

        EnumeratedDistribution<String> e = new EnumeratedDistribution<>(list);

        return e.sample();
    }

    public static Location topBlock(Location loc) {
        final YamlDocument config = Config.getCnf("config");
        ArrayList<Material> blacklist = new ArrayList<>();
        for (String i : config.getStringList("random-locations.dont-spawn-on-blocks")) {
            if (Material.getMaterial(i) == null) {
                Bukkit.getLogger().warning("Invalid material: " + i);
            } else {
                blacklist.add(Material.getMaterial(i));
            }
        }
        loc.setY(config.getDouble("random-locations.max-height"));
        while ((loc.getBlock().getType() != Material.AIR ) || loc.getY() >= -64) {
            loc.setY(loc.getY() - 1D);
            if (!blacklist.contains(loc.getBlock().getType())) {
                if (loc.getY() < config.getDouble("random-locations.min-height")) {
                    return null;
                }
                if (loc.getY() > config.getDouble("random-locations.max-height")) {
                    return null;
                }
                loc.setY(loc.getY() + 1D);
                if (loc.getBlock().getType() != Material.AIR) {
                    return null;
                }
                return loc;
            }
        }
        return null;
    }

    public static String fancyTime(long time) {

        Duration remainingTime = Duration.ofMillis(time);
        long days = remainingTime.toDays();
        remainingTime = remainingTime.minusDays(days);
        long weeks = days / 7;
        days %= 7;
        long hours = remainingTime.toHours();
        remainingTime = remainingTime.minusHours(hours);
        long minutes = remainingTime.toMinutes();
        remainingTime = remainingTime.minusMinutes(minutes);
        long seconds = remainingTime.getSeconds();
        String tm = "";
        String weeks2 = Long.toString(weeks);
        String hours2 = Long.toString(hours);
        String minutes2 = Long.toString(minutes);
        String seconds2 = Long.toString(seconds);
        if (weeks < 10) {weeks2 = "0" + weeks;}
        if (hours < 10) {hours2 = "0" + hours;}
        if (minutes < 10) {minutes2 = "0" + minutes;}
        if (seconds < 10) {seconds2 = "0" + seconds;}
        tm = tm + (weeks > 0 ? weeks2 + ":" : "");
        tm = tm + (hours > 0 ? hours2 + ":" : "00:");
        tm = tm + (minutes > 0 ? minutes2 + ":" : "00:");
        tm = tm + (seconds > 0 ? seconds2 : "00");

        return tm;
    }

    public static ItemStack createItem(final Material material, final int amount, final String name, final ArrayList<String> lore) {
        final ItemStack item = new ItemStack(material, amount);
        final ItemMeta meta = item.getItemMeta();
        meta.displayName(MiniMessage.miniMessage().deserialize(name).applyFallbackStyle(TextDecoration.ITALIC.withState(false)));

        ArrayList<Component> ar = new ArrayList<>();
        for (String s : lore) {
            ar.add(MiniMessage.miniMessage().deserialize(s).applyFallbackStyle(TextDecoration.ITALIC.withState(false)));
        }
        meta.lore(ar);

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        item.setItemMeta(meta);
        item.addUnsafeEnchantment(Enchantment.WATER_WORKER, 1);

        return item;
    }
}
