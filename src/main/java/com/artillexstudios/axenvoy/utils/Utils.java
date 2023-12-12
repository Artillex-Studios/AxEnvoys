package com.artillexstudios.axenvoy.utils;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.envoy.CrateType;
import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.envoy.SpawnedCrate;
import com.artillexstudios.axenvoy.rewards.Reward;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    private static final CompletableFuture<Location> NULL_FUTURE = CompletableFuture.completedFuture(null);

    public static @NotNull Location deserializeLocation(@NotNull String locationString) {
        String[] split = locationString.split(";");
        World world = Bukkit.getWorld(split[0]);
        return new Location(world, Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]), 0, 0);
    }

    public static String serializeLocation(@NotNull Location location) {
        return "%s;%s;%s;%s".formatted(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static CrateType randomCrate(@NotNull HashMap<CrateType, Double> map) {
        List<Pair<CrateType, Double>> list = new ArrayList<>();
        map.forEach((key, value) -> list.add(new Pair<>(key, value)));

        EnumeratedDistribution<CrateType> e = new EnumeratedDistribution<>(list);

        return e.sample();
    }

    public static Reward randomReward(@NotNull List<Reward> rewards) {
        List<Pair<Reward, Double>> list = new ArrayList<>();
        for (Reward reward : rewards) {
            list.add(new Pair<>(reward, reward.chance()));
        }

        EnumeratedDistribution<Reward> e = new EnumeratedDistribution<>(list);

        return e.sample();
    }

    public static CompletableFuture<Location> getNextLocation(@NotNull Envoy envoy, @NotNull Location loc) {
        CompletableFuture<Location> locationCompletableFuture = new CompletableFuture<>();
        Location center = loc.clone();
        loc.setX(loc.getBlockX() + ThreadLocalRandom.current().nextInt(envoy.getConfig().RANDOM_SPAWN_MAX_DISTANCE * -1, envoy.getConfig().RANDOM_SPAWN_MAX_DISTANCE));
        loc.setZ(loc.getBlockZ() + ThreadLocalRandom.current().nextInt(envoy.getConfig().RANDOM_SPAWN_MAX_DISTANCE * -1, envoy.getConfig().RANDOM_SPAWN_MAX_DISTANCE));
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
            Location loc2 = topBlock(loc);
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

            locationCompletableFuture.complete(loc);
        });

        return locationCompletableFuture;
    }

    @NotNull
    public static Location topBlock(@NotNull Location loc) {
        return loc.getWorld().getHighestBlockAt(loc).getLocation();
    }

    @NotNull
    public static String fancyTime(long time) {
        Duration remainingTime = Duration.ofMillis(time);
        long total = remainingTime.getSeconds();
        long days = total / 84600;
        long hours = (total % 84600) / 3600;
        long minutes = (total % 3600) / 60;
        long seconds = total % 60;

        if (days > 0) return String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
