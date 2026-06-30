package com.artillexstudios.axenvoy.integrations.mythicmobs;

import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.Location;

import java.util.List;

public class MythicMobsIntegration {

    public static void spawn(List<String> mobs, Location location) {
        if (mobs == null || mobs.isEmpty()) return;
        
        for (String mob : mobs) {
            try {
                MythicBukkit.inst().getAPIHelper().spawnMythicMob(mob, location);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
