package com.artillexstudios.axenvoy.envoy;

import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.rewards.CommandReward;
import com.artillexstudios.axenvoy.utils.FallingBlockChecker;
import com.artillexstudios.axenvoy.utils.StringUtils;
import com.artillexstudios.axenvoy.utils.Utils;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import net.kyori.adventure.util.TriState;
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

import java.awt.*;
import java.util.ArrayList;

public class SpawnedCrate {
    private final Envoy parent;
    private final Crate handle;
    private Location finishLocation;
    private FallingBlock fallingBlock;
    private Hologram hologram;

    public SpawnedCrate(@NotNull Envoy parent, @NotNull Crate handle, @NotNull Location location) {
        this.parent = parent;
        this.handle = handle;
        this.finishLocation = location;

        location.getWorld().getChunkAtAsync(location).thenAccept(chunk -> {
            this.parent.getSpawnedCrates().add(this);

            if (!handle.isFallingBlock()) {
                land(location);
                return;
            }

            Location spawnAt = location.clone();
            spawnAt.add(0, this.handle.getFallingBlockHeight(), 0);
            fallingBlock = location.getWorld().spawnFallingBlock(spawnAt, this.handle.getFallingBlockType().createBlockData());
            fallingBlock.setDropItem(false);
            fallingBlock.setPersistent(false);
            FallingBlockChecker.addToCheck(this);
            fallingBlock.setVelocity(new Vector(0, handle.getFallingBlockSpeed(), 0));
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
        Location hologramLocation = location.clone().toCenterLocation();
        hologramLocation.add(0, handle.getHologramHeight(), 0);

        ArrayList<String> formatted = new ArrayList<>(handle.getHologramLines().size());
        for (String hologramLine : handle.getHologramLines()) {
            formatted.add(StringUtils.formatToString(hologramLine));
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

        String hex = this.handle.getFireworkHex();
        Location loc2 = location.clone();
        loc2.add(0.5, 0.5, 0.5);
        Firework fw = (Firework) location.getWorld().spawnEntity(loc2, EntityType.FIREWORK);
        FireworkMeta meta = fw.getFireworkMeta();
        Color color = new Color(Integer.valueOf(hex.substring(1, 3), 16), Integer.valueOf(hex.substring(3, 5), 16), Integer.valueOf(hex.substring(5, 7), 16));
        meta.addEffect(FireworkEffect.builder().with(this.handle.getFireworkType()).withColor(org.bukkit.Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue())).build());
        meta.setPower(0);
        fw.setFireworkMeta(meta);
        fw.setMetadata("axenvoy", new FixedMetadataValue(AxEnvoyPlugin.getInstance(), true));
        fw.detonate();
    }


    public void claim(@Nullable Player player, Envoy envoy, boolean remove) {
        if (fallingBlock != null) {
            fallingBlock.remove();
            fallingBlock = null;
        }

        if (player != null) {
            CommandReward reward = Utils.randomReward(this.handle.getRewards());
            reward.execute(player);
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
                broadcast = Boolean.TRUE.equals(this.handle.isBroadcastCollect().toBoolean());
            }

            if (broadcast && player != null) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.sendMessage(envoy.getMessage("prefix").append(envoy.getMessage("collect").replaceText(text -> {
                        text.match("%crate%");
                        text.replacement(StringUtils.format(this.handle.getDisplayName()));
                    }).replaceText(replace -> {
                        replace.match("%amount%");
                        replace.replacement(String.valueOf(envoy.getSpawnedCrates().size()));
                    })));
                }
            }

            if (this.parent.getSpawnedCrates().isEmpty()) {
                envoy.setActive(false);
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.sendMessage(envoy.getMessage("prefix").append(envoy.getMessage("ended")));
                }
            }
        }
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
