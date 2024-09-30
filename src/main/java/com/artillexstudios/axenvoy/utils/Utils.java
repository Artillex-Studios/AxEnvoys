package com.artillexstudios.axenvoy.utils;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.envoy.Envoys;
import com.artillexstudios.axenvoy.envoy.SpawnedCrate;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import org.apache.commons.math3.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Point2D;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    public static @NotNull Location deserializeLocation(@NotNull String locationString) {
        String[] split = locationString.split(";");
        World world = Bukkit.getWorld(split[0]);
        return new Location(world, Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]), 0, 0);
    }

    public static String serializeLocation(@NotNull Location location) {
        return "%s;%s;%s;%s".formatted(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static Location getNextLocation(Envoy envoy, Location loc) {
        Location center = loc.clone();
        loc.setX(loc.getBlockX() + ThreadLocalRandom.current().nextInt(envoy.getConfig().RANDOM_SPAWN_MAX_DISTANCE_X * -1, envoy.getConfig().RANDOM_SPAWN_MAX_DISTANCE_X));
        loc.setZ(loc.getBlockZ() + ThreadLocalRandom.current().nextInt(envoy.getConfig().RANDOM_SPAWN_MAX_DISTANCE_Z * -1, envoy.getConfig().RANDOM_SPAWN_MAX_DISTANCE_Z));
        if (loc.distanceSquared(center) < envoy.getConfig().RANDOM_SPAWN_MIN_DISTANCE * envoy.getConfig().RANDOM_SPAWN_MIN_DISTANCE) {
            return null;
        }

        if (envoy.getConfig().ONLY_IN_GLOBAL && AxEnvoyPlugin.getInstance().isWorldGuard()) {
            ApplicableRegionSet regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(loc.getWorld())).getApplicableRegions(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()));
            if (!regions.getRegions().isEmpty()) {
                return null;
            }
        }

        if (envoy.getConfig().RANDOM_SPAWN_MIN_DISTANCE_BETWEEN_CRATES > 0) {
            for (SpawnedCrate spawnedCrate : envoy.getSpawnedCrates()) {
                // We only care about 2D distance!
                if (Point2D.distanceSq(spawnedCrate.getFinishLocation().getX(), spawnedCrate.getFinishLocation().getZ(), loc.getX(), loc.getZ()) < envoy.getConfig().RANDOM_SPAWN_MIN_DISTANCE_BETWEEN_CRATES * envoy.getConfig().RANDOM_SPAWN_MIN_DISTANCE_BETWEEN_CRATES) {
                    return null;
                }
            }
        }


        Location loc2 = topBlock(loc, envoy.heightMap());
        if (loc2.getY() < envoy.getConfig().RANDOM_SPAWN_MIN_HEIGHT) {
            return null;
        }
        if (loc2.getY() > envoy.getConfig().RANDOM_SPAWN_MAX_HEIGHT) {
            return null;
        }

        if (!loc.getChunk().isLoaded() && !loc.getChunk().load()) {
            return null;
        }

        for (SpawnedCrate spawnedCrate : envoy.getSpawnedCrates()) {
            if (spawnedCrate.getFinishLocation().getBlockX() == loc2.getBlockX() && spawnedCrate.getFinishLocation().getBlockY() == loc2.getBlockY() && spawnedCrate.getFinishLocation().getBlockZ() == loc2.getBlockZ()) {
                // There's a crate at that location!
                return null;
            }
        }

        Location tempLoc = loc2.clone();
        if (envoy.getBlacklistMaterials().contains(tempLoc.add(0, -1, 0).getBlock().getType())) {
            return null;
        }

        return loc2;
    }

    public static void sendMessage(CommandSender sender, String prefix, String message) {
        if (!message.isBlank()) {
            sender.sendMessage(StringUtils.formatToString(prefix + message));
        }
    }

    public static CompletableFuture<Location> getNextLocationFolia(@NotNull Envoy envoy, @NotNull Location loc) {
        CompletableFuture<Location> locationCompletableFuture = new CompletableFuture<>();
        Location center = loc.clone();
        loc.setX(loc.getBlockX() + ThreadLocalRandom.current().nextInt(envoy.getConfig().RANDOM_SPAWN_MAX_DISTANCE_X * -1, envoy.getConfig().RANDOM_SPAWN_MAX_DISTANCE_X));
        loc.setZ(loc.getBlockZ() + ThreadLocalRandom.current().nextInt(envoy.getConfig().RANDOM_SPAWN_MAX_DISTANCE_Z * -1, envoy.getConfig().RANDOM_SPAWN_MAX_DISTANCE_Z));
        if (loc.distanceSquared(center) < envoy.getConfig().RANDOM_SPAWN_MIN_DISTANCE * envoy.getConfig().RANDOM_SPAWN_MIN_DISTANCE) {
            locationCompletableFuture.complete(null);
        }

        if (envoy.getConfig().ONLY_IN_GLOBAL && AxEnvoyPlugin.getInstance().isWorldGuard()) {
            ApplicableRegionSet regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(loc.getWorld())).getApplicableRegions(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()));
            if (!regions.getRegions().isEmpty()) {
                locationCompletableFuture.complete(null);
            }
        }

        if (envoy.getConfig().RANDOM_SPAWN_MIN_DISTANCE_BETWEEN_CRATES > 0) {
            for (SpawnedCrate spawnedCrate : envoy.getSpawnedCrates()) {
                if (spawnedCrate.getFinishLocation().distanceSquared(loc) < envoy.getConfig().RANDOM_SPAWN_MIN_DISTANCE_BETWEEN_CRATES * envoy.getConfig().RANDOM_SPAWN_MIN_DISTANCE_BETWEEN_CRATES) {
                    locationCompletableFuture.complete(null);
                }
            }
        }

        Scheduler.get().runAt(loc, task -> {
            Location loc2 = topBlock(loc, envoy.heightMap());
            if (loc2.getY() < envoy.getConfig().RANDOM_SPAWN_MIN_HEIGHT) {
                locationCompletableFuture.complete(null);
            }
            if (loc2.getY() > envoy.getConfig().RANDOM_SPAWN_MAX_HEIGHT) {
                locationCompletableFuture.complete(null);
            }

            if (!loc.getChunk().isLoaded() && !loc.getChunk().load()) {
                locationCompletableFuture.complete(null);
            }

            Location tempLoc = loc2.clone();
            if (envoy.getBlacklistMaterials().contains(tempLoc.add(0, -1, 0).getBlock().getType())) {
                locationCompletableFuture.complete(null);
            }

            locationCompletableFuture.complete(loc2);
        });

        return locationCompletableFuture;
    }

    @NotNull
    public static Location topBlock(@NotNull Location loc, HeightMap heightMap) {
        return loc.getWorld().getHighestBlockAt(loc, heightMap).getLocation().add(0, 1, 0);
    }

//    @NotNull
//    public static String fancyTime(long time) {
//        Duration remainingTime = Duration.ofMillis(time);
//        long total = remainingTime.getSeconds();
//        long days = total / 84600;
//        long hours = (total % 84600) / 3600;
//        long minutes = (total % 3600) / 60;
//        long seconds = total % 60;
//
//        if (days > 0) return String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
//        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
//    }

    public static @NotNull String fancyTime(long time, Envoy envoy) {
        if (time < 0) return "---";

        final Duration remainingTime = Duration.ofMillis(time);
        long total = remainingTime.getSeconds();
        long days = total / 86400;
        long hours = (total % 86400) / 3600;
        long minutes = (total % 3600) / 60;
        long seconds = total % 60;

        if (envoy.getConfig().TIME_FORMAT == 1) {
            if (days > 0) return String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
            if (hours > 0) return String.format("%02d:%02d:%02d", hours, minutes, seconds);
            return String.format("%02d:%02d", minutes, seconds);
        } else if (envoy.getConfig().TIME_FORMAT == 2) {
            if (days > 0) return days + envoy.getConfig().DAY;
            if (hours > 0) return hours + envoy.getConfig().HOUR;
            if (minutes > 0) return minutes + envoy.getConfig().MINUTE;
            return seconds + envoy.getConfig().SECOND;
        } else {
            if (days > 0)
                return String.format("%02d" + envoy.getConfig().DAY + " %02d" + envoy.getConfig().HOUR + " %02d" + envoy.getConfig().MINUTE + " %02d" + envoy.getConfig().SECOND, days, hours, minutes, seconds);
            if (hours > 0)
                return String.format("%02d" + envoy.getConfig().HOUR + " %02d" + envoy.getConfig().MINUTE + " %02d" + envoy.getConfig().SECOND, hours, minutes, seconds);
            return String.format("%02d" + envoy.getConfig().MINUTE + " %02d" + envoy.getConfig().SECOND, minutes, seconds);
        }
    }

    public static Pair<Envoy, Long> getNextEnvoy() {
        AtomicReference<Envoy> next = new AtomicReference<>();
        Envoys.getTypes().values().forEach(envoy -> {
            Envoy currNext = next.get();
            if (currNext != null && envoy.getNext().before(currNext.getNext())) {
                next.set(envoy);
            } else if (currNext == null) {
                next.set(envoy);
            }
        });


        return Pair.create(next.get(), next.get().getNext().getTimeInMillis() - System.currentTimeMillis());
    }
}
