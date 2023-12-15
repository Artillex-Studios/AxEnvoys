package com.artillexstudios.axenvoy.envoy;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.PaperUtils;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.config.impl.EnvoyConfig;
import com.artillexstudios.axenvoy.listeners.FlareListener;
import com.artillexstudios.axenvoy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Envoy {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    private final ArrayList<Material> blacklistMaterials = new ArrayList<>();
    private final ArrayList<SpawnedCrate> spawnedCrates = new ArrayList<>();
    private final HashMap<CrateType, Double> cratesMap = new HashMap<>();
    private final String name;
    private final File file;
    private Location center;
    private ArrayList<Calendar> warns = new ArrayList<>();
    private Calendar next = Calendar.getInstance();
    private boolean active;
    private EnvoyConfig config;
    private int minCrateAmount;
    private int maxCrateAmount;
    private long startTime;

    public Envoy(@NotNull File file) {
        this.file = file;
        this.name = file.getName().replace(".yml", "").replace(".yaml", "");
        Envoys.register(this);

        this.active = false;
        this.config = new EnvoyConfig("envoys/" + file.getName());
        this.config.reload();
    }

    public void reload() {
        config.reload();
        this.center = Utils.deserializeLocation(config.RANDOM_SPAWN_CENTER).getWorld() != null ? Utils.deserializeLocation(config.RANDOM_SPAWN_CENTER) : new ArrayList<>(config.PREDEFINED_LOCATIONS).stream().map(Utils::deserializeLocation).toList().stream().findFirst().get();

        if (!config.SPAWN_AMOUNT.contains("-")) {
            this.minCrateAmount = this.maxCrateAmount = Integer.parseInt(config.SPAWN_AMOUNT);
        } else {
            String[] s = config.SPAWN_AMOUNT.split("-");
            this.minCrateAmount = Integer.parseInt(s[0]);
            this.maxCrateAmount = Integer.parseInt(s[1]);
        }

        for (Map.Entry<Object, Object> crate : config.CRATES.entrySet()) {
            String key = (String) crate.getKey();
            CrateType crateType = Crates.valueOf(key);
            if (crateType == null) return;

            cratesMap.put(crateType, ((Number) crate.getValue()).doubleValue());
        }

        List<String> blacklist = config.RANDOM_SPAWN_BLACKLISTED_MATERIALS;
        ArrayList<Pattern> patterns = new ArrayList<>(blacklist.size());
        for (String pattern : blacklist) {
            patterns.add(Pattern.compile(pattern));
        }

        material:
        for (Material value : Material.values()) {
            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(value.name().toLowerCase(Locale.ENGLISH));
                if (!matcher.find()) continue;

                this.blacklistMaterials.add(value);

                // Don't waste time looking through all patterns if we've already found one that matches
                continue material;
            }
        }

        if (!config.EVERY.isBlank()) {
            updateNext();
        }
    }

    public EnvoyConfig getConfig() {
        return config;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Material> getBlacklistMaterials() {
        return blacklistMaterials;
    }

    public ArrayList<SpawnedCrate> getSpawnedCrates() {
        return spawnedCrates;
    }

    public HashMap<CrateType, Double> getCratesMap() {
        return cratesMap;
    }

    public File getFile() {
        return file;
    }

    public ArrayList<Calendar> getWarns() {
        return warns;
    }

    public Location getCenter() {
        return center;
    }

    public void updateNext() {
        next = Calendar.getInstance();
        setCalendar(next, null, config.EVERY);
        warns.clear();
        warns = updateWarns();
    }

    public void setCalendar(Calendar calendar, Calendar parent, String format) {
        if (parent != null) {
            calendar.setTimeInMillis(parent.getTimeInMillis());
            for (String s : format.split(" ")) {
                if (s.contains("d")) {
                    calendar.add(Calendar.DATE, -Integer.parseInt(s.replace("d", "")));
                }
                if (s.contains("h")) {
                    calendar.add(Calendar.HOUR, -Integer.parseInt(s.replace("h", "")));
                }
                if (s.contains("m")) {
                    calendar.add(Calendar.MINUTE, -Integer.parseInt(s.replace("m", "")));
                }
                if (s.contains("s")) {
                    calendar.add(Calendar.SECOND, -Integer.parseInt(s.replace("s", "")));
                }
            }
        } else {
            for (String s : format.split(" ")) {
                if (s.contains("d")) {
                    calendar.add(Calendar.DATE, Integer.parseInt(s.replace("d", "")));
                }
                if (s.contains("h")) {
                    calendar.add(Calendar.HOUR, Integer.parseInt(s.replace("h", "")));
                }
                if (s.contains("m")) {
                    calendar.add(Calendar.MINUTE, Integer.parseInt(s.replace("m", "")));
                }
                if (s.contains("s")) {
                    calendar.add(Calendar.SECOND, Integer.parseInt(s.replace("s", "")));
                }
            }
        }
    }

    public Calendar getNext() {
        return next;
    }

    private ArrayList<Calendar> updateWarns() {
        ArrayList<Calendar> calendars = new ArrayList<>();

        for (String warn : config.ALERT_TIMES) {
            Calendar calendar = Calendar.getInstance();
            setCalendar(calendar, getNext(), warn);
            calendars.add(calendar);
        }

        return calendars;
    }

    public boolean start(Player player) {
        if (center == null) {
            return false;
        }

        if (active) {
            return false;
        }

        active = true;
        startTime = System.currentTimeMillis();

        int crateAmount = ThreadLocalRandom.current().nextInt(minCrateAmount, maxCrateAmount + 1);

        if (config.PREDEFINED_SPAWNS) {
            List<Location> locations = new ArrayList<>(config.PREDEFINED_LOCATIONS.stream().map(Utils::deserializeLocation).toList());

            if (config.LIMIT_PREDEFINED) {
                for (int i = 0; i < crateAmount; i++) {
                    Location location = locations.get(ThreadLocalRandom.current().nextInt(locations.size()));

                    new SpawnedCrate(this, Utils.randomCrate(cratesMap), location.clone());
                    locations.remove(location);

                    if (locations.isEmpty()) {
                        break;
                    }
                }
            } else {
                for (Location location : locations) {
                    new SpawnedCrate(this, Utils.randomCrate(cratesMap), location.clone());
                }
            }
        }

        if (config.RANDOM_SPAWN) {
            if (PaperUtils.isFolia()) {
                EXECUTOR.execute(() -> {
                    int count = crateAmount - this.spawnedCrates.size();
                    if (count > 0) {
                        for (int i = 0; i < count; i++) {
                            Location location = null;
                            int tries = 0;
                            while (location == null && tries < 100) {
                                tries++;
                                try {
                                    location = Utils.getNextLocationFolia(this, center.clone()).get();
                                } catch (InterruptedException | ExecutionException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            if (location != null) {
                                new SpawnedCrate(this, Utils.randomCrate(cratesMap), location.clone());
                            }
                        }
                    }

                    if (player == null) {
                        if (crateAmount > 1) {
                            String message = StringUtils.formatToString(config.PREFIX + config.MULTIPLE_START.replace("%world%", getCenter() != null ? getCenter().getWorld().getName() : "world")).replace("%x%", String.valueOf(getCenter() != null ? getCenter().getBlockX() : "x")).replace("%y%", String.valueOf(getCenter() != null ? getCenter().getBlockY() : "y")).replace("%z%", String.valueOf(getCenter() != null ? getCenter().getBlockZ() : "z")).replace("%amount%", String.valueOf(spawnedCrates.size())).replace("%location%", config.LOCATION_FORMAT.replace("%world%", getCenter() != null ? getCenter().getWorld().getName() : "world").replace("%x%", String.valueOf(getCenter() != null ? getCenter().getBlockX() : "x")).replace("%y%", String.valueOf(getCenter() != null ? getCenter().getBlockY() : "y")).replace("%z%", String.valueOf(getCenter() != null ? getCenter().getBlockZ() : "z")));
                            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                onlinePlayer.sendMessage(message);
                            }
                        } else {
                            String message = StringUtils.formatToString(config.PREFIX + config.SINGLE_START.replace("%world%", spawnedCrates.get(0).getFinishLocation().getWorld().getName())).replace("%x%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockX())).replace("%y%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockY())).replace("%z%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockZ())).replace("%amount%", String.valueOf(spawnedCrates.size())).replace("%location%", config.LOCATION_FORMAT.replace("%world%", spawnedCrates.get(0).getFinishLocation().getWorld().getName()).replace("%x%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockX())).replace("%y%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockY())).replace("%z%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockZ())));
                            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                onlinePlayer.sendMessage(message);
                            }
                        }
                    } else {
                        if (crateAmount > 1) {
                            String message = StringUtils.formatToString(config.PREFIX + config.MULTIPLE_START_FLARE.replace("%player_name%", player.getName()).replace("%player%", player.getName()).replace("%world%", getCenter() != null ? getCenter().getWorld().getName() : "world")).replace("%x%", String.valueOf(getCenter() != null ? getCenter().getBlockX() : "x")).replace("%y%", String.valueOf(getCenter() != null ? getCenter().getBlockY() : "y")).replace("%z%", String.valueOf(getCenter() != null ? getCenter().getBlockZ() : "z")).replace("%amount%", String.valueOf(spawnedCrates.size())).replace("%location%", config.LOCATION_FORMAT.replace("%world%", getCenter() != null ? getCenter().getWorld().getName() : "world").replace("%x%", String.valueOf(getCenter() != null ? getCenter().getBlockX() : "x")).replace("%y%", String.valueOf(getCenter() != null ? getCenter().getBlockY() : "y")).replace("%z%", String.valueOf(getCenter() != null ? getCenter().getBlockZ() : "z")));
                            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                onlinePlayer.sendMessage(message);
                            }
                        } else {
                            String message = StringUtils.formatToString(config.PREFIX + config.SINGLE_START_FLARE.replace("%player_name%", player.getName()).replace("%player%", player.getName()).replace("%world%", spawnedCrates.get(0).getFinishLocation().getWorld().getName())).replace("%x%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockX())).replace("%y%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockY())).replace("%z%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockZ())).replace("%amount%", String.valueOf(spawnedCrates.size())).replace("%location%", config.LOCATION_FORMAT.replace("%world%", spawnedCrates.get(0).getFinishLocation().getWorld().getName()).replace("%x%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockX())).replace("%y%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockY())).replace("%z%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockZ())));
                            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                onlinePlayer.sendMessage(message);
                            }
                        }
                    }

                    if (config.SEND_SPAWN_MESSAGES) {
                        for (SpawnedCrate spawnedCrate : spawnedCrates) {
                            String message = StringUtils.formatToString(config.PREFIX + config.CRATE_SPAWN.replace("%location%", config.LOCATION_FORMAT.replace("%world%", spawnedCrate.getFinishLocation().getWorld().getName()).replace("%x%", String.valueOf(spawnedCrate.getFinishLocation().getBlockX())).replace("%y%", String.valueOf(spawnedCrate.getFinishLocation().getBlockY())).replace("%z%", String.valueOf(spawnedCrate.getFinishLocation().getBlockZ()))));

                            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                if (!onlinePlayer.getPersistentDataContainer().has(AxEnvoyPlugin.MESSAGE_KEY, PersistentDataType.BYTE)) {
                                    onlinePlayer.sendMessage(StringUtils.formatToString(message));
                                }
                            }
                        }
                    }

                    if (config.TIMEOUT_TIME > 0) {
                        Scheduler.get().runLater(task -> {
                            if (!active) return;

                            stop();
                        }, config.TIMEOUT_TIME * 20L);
                    }
                });
            } else {
                int count = crateAmount - this.spawnedCrates.size();
                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        Location location = null;
                        int tries = 0;
                        while (location == null && tries < 100) {
                            tries++;
                            location = Utils.getNextLocation(this, center.clone());
                        }

                        if (location != null) {
                            new SpawnedCrate(this, Utils.randomCrate(cratesMap), location.clone());
                        }
                    }
                }

                if (player == null) {
                    if (crateAmount > 1) {
                        String message = StringUtils.formatToString(config.PREFIX + config.MULTIPLE_START.replace("%world%", getCenter() != null ? getCenter().getWorld().getName() : "world")).replace("%x%", String.valueOf(getCenter() != null ? getCenter().getBlockX() : "x")).replace("%y%", String.valueOf(getCenter() != null ? getCenter().getBlockY() : "y")).replace("%z%", String.valueOf(getCenter() != null ? getCenter().getBlockZ() : "z")).replace("%amount%", String.valueOf(spawnedCrates.size())).replace("%location%", config.LOCATION_FORMAT.replace("%world%", getCenter() != null ? getCenter().getWorld().getName() : "world").replace("%x%", String.valueOf(getCenter() != null ? getCenter().getBlockX() : "x")).replace("%y%", String.valueOf(getCenter() != null ? getCenter().getBlockY() : "y")).replace("%z%", String.valueOf(getCenter() != null ? getCenter().getBlockZ() : "z")));
                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                            onlinePlayer.sendMessage(message);
                        }
                    } else {
                        String message = StringUtils.formatToString(config.PREFIX + config.SINGLE_START.replace("%world%", spawnedCrates.get(0).getFinishLocation().getWorld().getName())).replace("%x%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockX())).replace("%y%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockY())).replace("%z%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockZ())).replace("%amount%", String.valueOf(spawnedCrates.size())).replace("%location%", config.LOCATION_FORMAT.replace("%world%", spawnedCrates.get(0).getFinishLocation().getWorld().getName()).replace("%x%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockX())).replace("%y%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockY())).replace("%z%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockZ())));
                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                            onlinePlayer.sendMessage(message);
                        }
                    }
                } else {
                    if (crateAmount > 1) {
                        String message = StringUtils.formatToString(config.PREFIX + config.MULTIPLE_START_FLARE.replace("%player_name%", player.getName()).replace("%player%", player.getName()).replace("%world%", getCenter() != null ? getCenter().getWorld().getName() : "world")).replace("%x%", String.valueOf(getCenter() != null ? getCenter().getBlockX() : "x")).replace("%y%", String.valueOf(getCenter() != null ? getCenter().getBlockY() : "y")).replace("%z%", String.valueOf(getCenter() != null ? getCenter().getBlockZ() : "z")).replace("%amount%", String.valueOf(spawnedCrates.size())).replace("%location%", config.LOCATION_FORMAT.replace("%world%", getCenter() != null ? getCenter().getWorld().getName() : "world").replace("%x%", String.valueOf(getCenter() != null ? getCenter().getBlockX() : "x")).replace("%y%", String.valueOf(getCenter() != null ? getCenter().getBlockY() : "y")).replace("%z%", String.valueOf(getCenter() != null ? getCenter().getBlockZ() : "z")));
                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                            onlinePlayer.sendMessage(message);
                        }
                    } else {
                        String message = StringUtils.formatToString(config.PREFIX + config.SINGLE_START_FLARE.replace("%player_name%", player.getName()).replace("%player%", player.getName()).replace("%world%", spawnedCrates.get(0).getFinishLocation().getWorld().getName())).replace("%x%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockX())).replace("%y%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockY())).replace("%z%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockZ())).replace("%amount%", String.valueOf(spawnedCrates.size())).replace("%location%", config.LOCATION_FORMAT.replace("%world%", spawnedCrates.get(0).getFinishLocation().getWorld().getName()).replace("%x%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockX())).replace("%y%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockY())).replace("%z%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockZ())));
                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                            onlinePlayer.sendMessage(message);
                        }
                    }
                }

                if (config.SEND_SPAWN_MESSAGES) {
                    for (SpawnedCrate spawnedCrate : spawnedCrates) {
                        String message = StringUtils.formatToString(config.PREFIX + config.CRATE_SPAWN.replace("%location%", config.LOCATION_FORMAT.replace("%world%", spawnedCrate.getFinishLocation().getWorld().getName()).replace("%x%", String.valueOf(spawnedCrate.getFinishLocation().getBlockX())).replace("%y%", String.valueOf(spawnedCrate.getFinishLocation().getBlockY())).replace("%z%", String.valueOf(spawnedCrate.getFinishLocation().getBlockZ()))));

                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                            if (!onlinePlayer.getPersistentDataContainer().has(AxEnvoyPlugin.MESSAGE_KEY, PersistentDataType.BYTE)) {
                                onlinePlayer.sendMessage(StringUtils.formatToString(message));
                            }
                        }
                    }
                }

                if (config.TIMEOUT_TIME > 0) {
                    Scheduler.get().runLater(task -> {
                        if (!active) return;

                        stop();
                    }, config.TIMEOUT_TIME * 20L);
                }
            }
        }

        return true;
    }

    public void stop() {
        if (!active) return;

        Iterator<SpawnedCrate> crateIterator = this.spawnedCrates.iterator();

        while (crateIterator.hasNext()) {
            SpawnedCrate next = crateIterator.next();
            crateIterator.remove();
            next.claim(null, this, false);
        }

        this.active = false;
        this.updateNext();
    }

    public ItemStack getFlare(int amount) {
        return new ItemBuilder(config.FLARE_ITEM).amount(amount).storePersistentData(FlareListener.KEY, PersistentDataType.STRING, this.name.toLowerCase(Locale.ENGLISH)).get();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getStartTime() {
        return startTime;
    }
}
