package me.bencex100.rivalsenvoy.utils;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.bencex100.rivalsenvoy.RivalsEnvoy;
import me.bencex100.rivalsenvoy.config.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {
    private static final YamlDocument config = Config.getCnf("config");
    private static final ArrayList<Material> blackList = new ArrayList<>();

    public void updateBlackList() {
        blackList.clear();
        for (String i : config.getStringList("random-locations.dont-spawn-on-blocks")) {
            if (Material.getMaterial(i) == null) {
                Bukkit.getLogger().warning("Invalid material: " + i);
            } else {
                blackList.add(Material.getMaterial(i));
            }
        }
    }

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

    @Nullable
    public static Location isGood(Location loc) {
        loc.setX(Double.parseDouble(String.valueOf(Math.round(loc.getX()) + ThreadLocalRandom.current().nextInt(config.getInt("random-locations.distance") * - 1, config.getInt("random-locations.distance")))));
        loc.setZ(Double.parseDouble(String.valueOf(Math.round(loc.getZ()) + ThreadLocalRandom.current().nextInt(config.getInt("random-locations.distance") * - 1, config.getInt("random-locations.distance")))));

        Location loc2 = topBlock(loc);
        Location tempLoc = loc2.clone();
        if (blackList.contains(tempLoc.add(0, -1, 0).getBlock().getType()))
            return null;
        if (loc2.getY() < config.getDouble("random-locations.min-height"))
            return null;
        if (loc2.getY() > config.getDouble("random-locations.max-height"))
            return null;

        return loc2;
    }

    public static Location topBlock(Location loc) {
        loc.getWorld().getChunkAtAsync(loc, false).thenAccept(chunk -> {
            loc.setY(chunk.getChunkSnapshot().getHighestBlockYAt(loc.getBlockX() & 15, loc.getBlockZ() & 15) + 1);
        });
        return loc;
    }

    public static String fancyTime(long time) {

        Duration remainingTime = Duration.ofMillis(time);
        long total = remainingTime.getSeconds();
        long days = total / 84600;
        long hours = (total % 84600) / 3600;
        long minutes = (total % 3600) / 60;
        long seconds = total % 60;

        if (days > 0)
            return String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static ItemStack createItem(final Material material, final int amount, final String name, final ArrayList<String> lore, final String id) {
        final ItemStack item = new ItemStack(material, amount);
        final ItemMeta meta = item.getItemMeta();
        meta.displayName(ColorUtils.deserialize(name).applyFallbackStyle(TextDecoration.ITALIC.withState(false)));

        ArrayList<Component> ar = new ArrayList<>();
        for (String s : lore) {
            ar.add(ColorUtils.deserialize(s).applyFallbackStyle(TextDecoration.ITALIC.withState(false)));
        }
        meta.lore(ar);

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        NamespacedKey key = new NamespacedKey(RivalsEnvoy.getInstance(), "RIVALSENVOY");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);

        item.setItemMeta(meta);
        item.addUnsafeEnchantment(Enchantment.WATER_WORKER, 1);

        return item;
    }
}
