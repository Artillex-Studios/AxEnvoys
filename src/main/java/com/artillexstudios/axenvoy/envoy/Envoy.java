package com.artillexstudios.axenvoy.envoy;

import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.config.ConfigManager;
import com.artillexstudios.axenvoy.utils.StringUtils;
import com.artillexstudios.axenvoy.utils.Utils;
import dev.dejvokep.boostedyaml.YamlDocument;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Envoy {
    private final ObjectArrayList<Material> notOnMaterials = new ObjectArrayList<>();
    private final ObjectArrayList<SpawnedCrate> spawnedCrates = new ObjectArrayList<>();
    private final Object2ObjectArrayMap<Crate, Double> cratesMap = new Object2ObjectArrayMap<>();
    private final List<String> warnList;
    private final YamlDocument document;
    private final String name;
    private final Location center;
    private final ItemStack flare;
    private final boolean broadcastCollect;
    private final boolean collectGlobalCooldown;
    private final String every;
    private final boolean useRewardPrefix;
    private final int minPlayers;
    private final boolean limitPredefined;
    private final boolean sendSpawnMessage;
    private final boolean onlyInGlobal;
    private ObjectArrayList<Calendar> warns = new ObjectArrayList<>();
    private Calendar next = Calendar.getInstance();
    private BukkitTask bukkitTask;
    private boolean active;
    private boolean randomSpawns;
    private boolean predefinedSpawns;
    private boolean flareEnabled;
    private int collectCooldown;
    private int flareCooldown;
    private int timeoutTime;
    private int crateAmount;
    private final int minCrateAmount;
    private final int maxCrateAmount;
    private int minDistance;
    private int maxDistance;
    private int minHeight;
    private int maxHeight;
    private long startTime;
    private int minDistanceBetweenCrates;

    public Envoy(@NotNull YamlDocument config) {
        this.document = config;
        this.active = false;
        this.name = config.getFile().getName().replace(".yml", "").replace(".yaml", "");
        this.center = Utils.deserializeLocation(config, "random-spawn.center") != null ? Utils.deserializeLocation(config, "random-spawn.center") : new ArrayList<>(this.document.getStringList("pre-defined-spawns.locations", new ArrayList<>()).stream().map(Utils::deserializeLocation).toList()).stream().findFirst().get();
        this.randomSpawns = config.getBoolean("random-spawn.enabled", false);
        this.predefinedSpawns = config.getBoolean("pre-defined-spawns.enabled", false);
        this.flareEnabled = config.getBoolean("flare.enabled", false);
        this.broadcastCollect = config.getBoolean("broadcast-collect", false);
        this.collectGlobalCooldown = config.getBoolean("collect-global-cooldown", false);
        this.useRewardPrefix = config.getBoolean("rewards.use-prefix", true);
        this.limitPredefined = config.getBoolean("limit-predefined", true);
        this.sendSpawnMessage = config.getBoolean("send-spawn-message", false);
        this.onlyInGlobal = config.getBoolean("only-in-global", false);
        this.crateAmount = config.getInt("amount", 30);
        this.collectCooldown = config.getInt("collect-cooldown", 10);
        this.minPlayers = config.getInt("min-players", 2);
        this.timeoutTime = config.getInt("timeout-time", -1);
        this.minDistance = config.getInt("random-spawn.min-distance", 20);
        this.maxDistance = config.getInt("random-spawn.max-distance", 100);
        this.minHeight = config.getInt("random-spawn.min-height", 10);
        this.maxHeight = config.getInt("random-spawn.max-height", 200);
        this.flareCooldown = config.getInt("flare.cooldown", 30);
        this.warnList = config.getStringList("alert-times", new ArrayList<>());
        this.every = config.getString("every", "");
        this.minDistanceBetweenCrates = config.getInt("min-distance-between-crates", 0);

        if (!config.getString("amount").contains("-")) {
            this.minCrateAmount = this.maxCrateAmount = config.getInt("amount", 30);
        } else {
            String[] s = config.getString("amount").split("-");
            this.minCrateAmount = Integer.parseInt(s[0]);
            this.maxCrateAmount = Integer.parseInt(s[1]);
        }

        if (flareEnabled) {
            flare = Utils.createItem(config.getSection("flare.item"), name);
        } else {
            flare = null;
        }

        System.out.println("Loaded envoy " + name + " with center: ");
        System.out.println(center);

        for (Object crates : config.getSection("crates").getKeys()) {
            for (Crate crate : CrateLoader.crates) {
                if (!crate.getName().equals(crates)) continue;
                cratesMap.put(crate, config.getSection("crates").getDouble((String) crates));
            }
        }

        config.getOptionalStringList("random-spawn.not-on-blocks").ifPresent(list -> {
            ObjectArrayList<Pattern> patterns = new ObjectArrayList<>(list.size());
            for (String s : list) {
                patterns.add(Pattern.compile(s));
            }

            material:
            for (Material value : Material.values()) {
                for (Pattern pattern : patterns) {
                    Matcher matcher = pattern.matcher(value.name().toLowerCase(Locale.ENGLISH));
                    if (!matcher.find()) continue;

                    this.notOnMaterials.add(value);

                    // Don't waste time looking through all patterns if we've already found one that matches
                    continue material;
                }
            }
        });

        if (!every.isEmpty()) {
            updateNext();
        }
    }

    public ObjectArrayList<SpawnedCrate> getSpawnedCrates() {
        return spawnedCrates;
    }

    public Object2ObjectArrayMap<Crate, Double> getCratesMap() {
        return cratesMap;
    }

    public YamlDocument getDocument() {
        return document;
    }

    public Location getCenter() {
        return center;
    }

    public void updateNext() {
        next = Calendar.getInstance();
        setCalendar(next, null, this.every);
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

    private ObjectArrayList<Calendar> updateWarns() {
        ObjectArrayList<Calendar> calendars = new ObjectArrayList<>();

        for (String warn : this.warnList) {
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
        this.updateNext();
        startTime = System.currentTimeMillis();
        this.crateAmount = ThreadLocalRandom.current().nextInt(minCrateAmount, maxCrateAmount + 1);

        if (predefinedSpawns) {
            List<Location> locations = new ArrayList<>(this.document.getStringList("pre-defined-spawns.locations", new ArrayList<>()).stream().map(Utils::deserializeLocation).toList());

            if (limitPredefined) {
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

        if (randomSpawns) {
            int count = crateAmount - this.spawnedCrates.size();
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    Location location = null;
                    int tries = 0;
                    while (location == null || tries < 500) {
                        tries++;
                        location = Utils.getNextLocation(this, center.clone());
                    }
                    new SpawnedCrate(this, Utils.randomCrate(cratesMap), location.clone());
                }
            }
        }

        if (player == null) {
            if (crateAmount > 1) {
                String message = String.format("%s%s", StringUtils.format(getMessage("prefix")), getMessage("start.multiple").replace("%world%", getCenter() != null ? getCenter().getWorld().getName() : "world")).replace("%x%", String.valueOf(getCenter() != null ? getCenter().getBlockX() : "x")).replace("%y%", String.valueOf(getCenter() != null ? getCenter().getBlockY() : "y")).replace("%z%", String.valueOf(getCenter() != null ? getCenter().getBlockZ() : "z")).replace("%amount%", String.valueOf(spawnedCrates.size())).replace("%location%", this.getMessage("location-format").replace("%world%", getCenter() != null ? getCenter().getWorld().getName() : "world").replace("%x%", String.valueOf(getCenter() != null ? getCenter().getBlockX() : "x")).replace("%y%", String.valueOf(getCenter() != null ? getCenter().getBlockY() : "y")).replace("%z%", String.valueOf(getCenter() != null ? getCenter().getBlockZ() : "z")));
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.sendMessage(message);
                }
            } else {
                String message = String.format("%s%s", StringUtils.format(getMessage("prefix")), getMessage("start.one").replace("%world%", spawnedCrates.get(0).getFinishLocation().getWorld().getName())).replace("%x%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockX())).replace("%y%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockY())).replace("%z%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockZ())).replace("%amount%", String.valueOf(spawnedCrates.size())).replace("%location%", this.getMessage("location-format").replace("%world%", spawnedCrates.get(0).getFinishLocation().getWorld().getName()).replace("%x%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockX())).replace("%y%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockY())).replace("%z%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockZ())));
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.sendMessage(message);
                }
            }
        } else {
            if (crateAmount > 1) {
                String message = String.format("%s%s", StringUtils.format(getMessage("prefix")), getMessage("flare-start.multiple").replace("%world%", getCenter() != null ? getCenter().getWorld().getName() : "world")).replace("%x%", String.valueOf(getCenter() != null ? getCenter().getBlockX() : "x")).replace("%y%", String.valueOf(getCenter() != null ? getCenter().getBlockY() : "y")).replace("%z%", String.valueOf(getCenter() != null ? getCenter().getBlockZ() : "z")).replace("%amount%", String.valueOf(spawnedCrates.size())).replace("%location%", this.getMessage("location-format").replace("%world%", getCenter() != null ? getCenter().getWorld().getName() : "world").replace("%x%", String.valueOf(getCenter() != null ? getCenter().getBlockX() : "x")).replace("%y%", String.valueOf(getCenter() != null ? getCenter().getBlockY() : "y")).replace("%z%", String.valueOf(getCenter() != null ? getCenter().getBlockZ() : "z")));
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.sendMessage(message);
                }
            } else {
                String message = String.format("%s%s", StringUtils.format(getMessage("prefix")), getMessage("flare-start.one").replace("%world%", spawnedCrates.get(0).getFinishLocation().getWorld().getName())).replace("%x%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockX())).replace("%y%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockY())).replace("%z%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockZ())).replace("%amount%", String.valueOf(spawnedCrates.size())).replace("%location%", this.getMessage("location-format").replace("%world%", spawnedCrates.get(0).getFinishLocation().getWorld().getName()).replace("%x%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockX())).replace("%y%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockY())).replace("%z%", String.valueOf(spawnedCrates.get(0).getFinishLocation().getBlockZ())));
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.sendMessage(message);
                }
            }
        }

//        List<String> locations = ConfigManager.getTempData().getStringList(String.format("%s.locations", this.getName()), new ArrayList<>());
//        for (SpawnedCrate spawnedCrate : this.spawnedCrates) {
//            locations.add(Utils.serializeLocation(spawnedCrate.getFinishLocation()));
//        }
//
//        ConfigManager.getTempData().set(String.format("%s.locations", this.getName()), locations);
//
//        try {
//            ConfigManager.getTempData().save();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        if (this.isSendSpawnMessage()) {
            for (SpawnedCrate spawnedCrate : spawnedCrates) {
                String message = String.format("%s%s", this.getMessage("prefix"), this.getMessage("crate-spawn-message").replace("%location%", this.getMessage("location-format").replace("%world%", spawnedCrate.getFinishLocation().getWorld().getName()).replace("%x%", String.valueOf(spawnedCrate.getFinishLocation().getBlockX())).replace("%y%", String.valueOf(spawnedCrate.getFinishLocation().getBlockY())).replace("%z%", String.valueOf(spawnedCrate.getFinishLocation().getBlockZ()))));

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (!onlinePlayer.getPersistentDataContainer().has(AxEnvoyPlugin.MESSAGE_KEY, PersistentDataType.BYTE)) {
                        onlinePlayer.sendMessage(StringUtils.format(message));
                    }
                }
            }
        }

        if (this.timeoutTime > 0) {
            bukkitTask = Bukkit.getScheduler().runTaskLater(AxEnvoyPlugin.getInstance(), () -> {
                if (!active) return;

                stop();
            }, this.timeoutTime * 20L);
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
        if (this.bukkitTask != null) {
            this.bukkitTask.cancel();
            this.bukkitTask = null;
        }
    }

    public String getMessage(String path) {
        return document.getOptionalString("messages.%s".formatted(path)).map(StringUtils::format).orElseGet(() -> StringUtils.format(ConfigManager.getLang().getString("messages.%s".formatted(path))));
    }

    public String getMessage(String path, Player player) {
        return document.getOptionalString("messages.%s".formatted(path)).map(message -> StringUtils.format(message.replace("%player%", player.getName()))).orElseGet(() -> StringUtils.format(ConfigManager.getLang().getString("messages.%s".formatted(path)).replace("%player%", player.getName())));
    }

    public ObjectArrayList<Material> getNotOnMaterials() {
        return notOnMaterials;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isRandomSpawns() {
        return randomSpawns;
    }

    public void setRandomSpawns(boolean randomSpawns) {
        this.randomSpawns = randomSpawns;
    }

    public boolean isPredefinedSpawns() {
        return predefinedSpawns;
    }

    public void setPredefinedSpawns(boolean predefinedSpawns) {
        this.predefinedSpawns = predefinedSpawns;
    }

    public boolean isFlareEnabled() {
        return flareEnabled;
    }

    public void setFlareEnabled(boolean flareEnabled) {
        this.flareEnabled = flareEnabled;
    }

    public int getFlareCooldown() {
        return flareCooldown;
    }

    public void setFlareCooldown(int flareCooldown) {
        this.flareCooldown = flareCooldown;
    }

    public int getCollectCooldown() {
        return collectCooldown;
    }

    public void setCollectCooldown(int collectCooldown) {
        this.collectCooldown = collectCooldown;
    }

    public int getTimeoutTime() {
        return timeoutTime;
    }

    public void setTimeoutTime(int timeoutTime) {
        this.timeoutTime = timeoutTime;
    }

    public int getCrateAmount() {
        return crateAmount;
    }

    public void setCrateAmount(int crateAmount) {
        this.crateAmount = crateAmount;
    }

    public int getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(int minDistance) {
        this.minDistance = minDistance;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(int maxDistance) {
        this.maxDistance = maxDistance;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(int minHeight) {
        this.minHeight = minHeight;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public String getName() {
        return name;
    }

    public boolean isBroadcastCollect() {
        return broadcastCollect;
    }

    public ItemStack getFlare() {
        return flare;
    }

    public boolean isCollectGlobalCooldown() {
        return collectGlobalCooldown;
    }

    public long getStartTime() {
        return startTime;
    }

    public BukkitTask getBukkitTask() {
        return bukkitTask;
    }

    public void setBukkitTask(BukkitTask bukkitTask) {
        this.bukkitTask = bukkitTask;
    }

    public boolean isUseRewardPrefix() {
        return useRewardPrefix;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public String getEvery() {
        return every;
    }

    public ObjectArrayList<Calendar> getWarns() {
        return warns;
    }

    public int getMinDistanceBetweenCrates() {
        return minDistanceBetweenCrates;
    }

    public List<String> getWarnList() {
        return warnList;
    }

    public boolean isSendSpawnMessage() {
        return sendSpawnMessage;
    }

    public boolean isOnlyInGlobal() {
        return onlyInGlobal;
    }
}
