package com.artillexstudios.axenvoy.listeners;

import com.artillexstudios.axenvoy.envoy.Crate;
import com.artillexstudios.axenvoy.envoy.CrateLoader;
import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.envoy.EnvoyLoader;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.jetbrains.annotations.NotNull;

public class BlockPhysicsListener implements Listener {

    @EventHandler
    public void onBlockPhysicsEvent(@NotNull BlockPhysicsEvent e) {
        ObjectArrayList<Envoy> envoys = EnvoyLoader.envoys;
        if (envoys.isEmpty()) return;

        int size = envoys.size();
        for (int i = 0; i < size; i++) {
            Envoy envoy = envoys.get(i);
            if (!envoy.isActive()) continue;
            if (!envoy.getCenter().getWorld().equals(e.getBlock().getWorld())) continue;
            ObjectArrayList<Crate> crates = CrateLoader.crates;
            if (crates.isEmpty()) continue;
            int crateSize = crates.size();

            for (int i1 = 0; i1 < crateSize; i1++) {
                Crate crate = crates.get(i);
                if (crate.getMaterial() != e.getSourceBlock().getType() || crate.getMaterial() != e.getBlock().getType()) continue;

                e.setCancelled(true);
                return;
            }
        }
    }
}
