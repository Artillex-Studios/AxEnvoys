package com.artillexstudios.axenvoy.envoy;

import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.config.ConfigManager;
import com.artillexstudios.axenvoy.rewards.Reward;
import com.artillexstudios.axenvoy.utils.FallingBlockChecker;
import com.artillexstudios.axenvoy.utils.StringUtils;
import com.artillexstudios.axenvoy.utils.Utils;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import io.papermc.lib.PaperLib;
import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vex;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpawnedCrate {
    public static final NamespacedKey FIREWORK_KEY = new NamespacedKey(AxEnvoyPlugin.getInstance(), "axenvoy_firework");
    private final Envoy parent;
    private final Crate handle;
    private Location finishLocation;
    private FallingBlock fallingBlock;
    private Vex vex;
    private Hologram hologram;
    private int tick = 0;

    public SpawnedCrate(@NotNull Envoy parent, @NotNull Crate handle, @NotNull Location location) {
        this.parent = parent;
        this.handle = handle;
        this.finishLocation = location;

        PaperLib.getChunkAtAsync(location).thenAccept(chunk -> {
            this.parent.getSpawnedCrates().add(this);

            if (!handle.isFallingBlock() || location.getWorld().getNearbyEntities(location, Bukkit.getServer().getSimulationDistance() * 16, Bukkit.getServer().getSimulationDistance() * 16, Bukkit.getServer().getSimulationDistance() * 16).isEmpty()) {
                land(location);
                return;
            }

            Location spawnAt = location.clone();
            spawnAt.add(0.5, this.handle.getFallingBlockHeight(), 0.5);
            vex = location.getWorld().spawn(spawnAt, Vex.class, ent -> {
                ent.setInvisible(true);
                ent.setSilent(true);
                ent.setInvulnerable(true);
                ent.setGravity(true);
                ent.setAware(false);
                ent.setPersistent(false);
                if (ent.getEquipment() != null) {
                    ent.getEquipment().clear();
                }
            });

            vex.setGravity(true);

            fallingBlock = location.getWorld().spawnFallingBlock(spawnAt, this.handle.getFallingBlockType().createBlockData());
            vex.addPassenger(fallingBlock);
            fallingBlock.setPersistent(false);
            FallingBlockChecker.addToCheck(this);
            vex.setVelocity(new Vector(0, handle.getFallingBlockSpeed(), 0));
        });
    }

    public void land(@NotNull Location location) {
        this.finishLocation = location;
        location.getWorld().getBlockAt(location).setType(this.handle.getMaterial());
        this.spawnHologram(location);
        this.spawnFirework(location);
    }

    private void spawnHologram(@NotNull Location location) {
        if (!handle.isHologram()) return;
        Location hologramLocation = location.clone().add(0.5, 0, 0.5);
        hologramLocation.add(0, handle.getHologramHeight(), 0);

        ArrayList<String> formatted = new ArrayList<>(handle.getHologramLines().size());
        for (String hologramLine : handle.getHologramLines()) {
            formatted.add(StringUtils.format(hologramLine));
        }

        Hologram tempHolo = DHAPI.getHologram(Utils.serializeLocation(hologramLocation).replace(";", ""));
        if (tempHolo != null) {
            tempHolo.delete();
        }

        hologram = DHAPI.createHologram(Utils.serializeLocation(hologramLocation).replace(";", ""), hologramLocation, formatted);
    }

    public void claim(@Nullable Player player, Envoy envoy) {
        this.claim(player, envoy, true);
    }

    public void spawnFirework(Location location) {
        if (!this.handle.isFirework()) return;

        Location loc2 = location.clone();
        loc2.add(0.5, 0.5, 0.5);
        Firework fw = (Firework) location.getWorld().spawnEntity(loc2, EntityType.FIREWORK);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder().with(this.handle.getFireworkType()).withColor(org.bukkit.Color.fromRGB(this.handle.getFireworkColor().getRed(), this.handle.getFireworkColor().getGreen(), this.handle.getFireworkColor().getBlue())).build());
        meta.setPower(0);
        fw.setFireworkMeta(meta);
        fw.getPersistentDataContainer().set(FIREWORK_KEY, PersistentDataType.BYTE, (byte) 0);
        fw.detonate();
    }


    public void claim(@Nullable Player player, Envoy envoy, boolean remove) {
        if (fallingBlock != null) {
            fallingBlock.remove();
            fallingBlock = null;
        }

        if (player != null) {
            Reward reward = Utils.randomReward(this.handle.getRewards());
            reward.execute(player, envoy);
        }

        finishLocation.getWorld().getBlockAt(finishLocation).setType(Material.AIR);
        if (hologram != null) {
            hologram.delete();
        }

        if (remove) {
            this.parent.getSpawnedCrates().remove(this);
        }

        if (envoy != null) {
            boolean broadcast;
            if (this.handle.isBroadcastCollect() == TriState.NOT_SET) {
                broadcast = envoy.isBroadcastCollect();
            } else {
                broadcast = this.handle.isBroadcastCollect().name().equalsIgnoreCase("TRUE");
            }

            if (broadcast && player != null) {
                String message = String.format("%s%s", StringUtils.format(envoy.getMessage("prefix")), envoy.getMessage("collect", player).replace("%crate%", StringUtils.format(this.handle.getDisplayName())).replace("%amount%", String.valueOf(envoy.getSpawnedCrates().size())));
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.sendMessage(message);
                }
            }

            List<String> locations = ConfigManager.getTempData().getStringList(String.format("%s.locations", parent.getName()), new ArrayList<>());
            locations.remove(Utils.serializeLocation(finishLocation));
            ConfigManager.getTempData().set(String.format("%s.locations", parent.getName()), locations);

            if (this.parent.getSpawnedCrates().isEmpty()) {
                envoy.updateNext();
                envoy.setActive(false);
                if (envoy.getBukkitTask() != null) {
                    envoy.getBukkitTask().cancel();
                    envoy.setBukkitTask(null);
                }
                String message = String.format("%s%s", StringUtils.format(envoy.getMessage("prefix")), envoy.getMessage("ended"));
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.sendMessage(message);
                }

                try {
                    ConfigManager.getTempData().save();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void tickFlare() {
        if (this.handle.getFlareTicks() == 0) return;
        tick++;

        if (tick == this.handle.getFlareTicks()) {
            if (!getFinishLocation().getWorld().isChunkLoaded(getFinishLocation().getChunk())) return;
            Location loc2 = finishLocation.clone();
            loc2.add(0.5, 0.5, 0.5);
            Firework fw = (Firework) loc2.getWorld().spawnEntity(loc2, EntityType.FIREWORK);
            FireworkMeta meta = fw.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder().with(this.handle.getFlareFireworkType()).withColor(org.bukkit.Color.fromRGB(this.handle.getFlareFireworkColor().getRed(), this.handle.getFlareFireworkColor().getGreen(), this.handle.getFlareFireworkColor().getBlue())).build());
            meta.setPower(0);
            fw.setFireworkMeta(meta);
            fw.getPersistentDataContainer().set(FIREWORK_KEY, PersistentDataType.BYTE, (byte) 0);
            fw.detonate();
            tick = 0;
        }
    }

    public Vex getVex() {
        return vex;
    }

    public void setVex(Vex vex) {
        this.vex = vex;
    }

    public Crate getHandle() {
        return handle;
    }

    public FallingBlock getFallingBlock() {
        return fallingBlock;
    }

    public void setFallingBlock(FallingBlock fallingBlock) {
        this.fallingBlock = fallingBlock;
    }

    public Location getFinishLocation() {
        return finishLocation;
    }
}
