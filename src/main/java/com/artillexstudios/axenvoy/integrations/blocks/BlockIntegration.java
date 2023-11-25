package com.artillexstudios.axenvoy.integrations.blocks;

import com.artillexstudios.axenvoy.integrations.blocks.impl.DefaultBlockIntegration;
import com.artillexstudios.axenvoy.integrations.blocks.impl.ItemsAdderBlockIntegration;
import com.artillexstudios.axenvoy.integrations.blocks.impl.OraxenBlockIntegration;
import org.bukkit.Location;

public interface BlockIntegration {

    void place(String id, Location location);

    void remove(Location location);

    class Companion {
        private static final BlockIntegration oraxenIntegration = new OraxenBlockIntegration();
        private static final BlockIntegration itemsAdderIntegration = new ItemsAdderBlockIntegration();
        private static final BlockIntegration defaultIntegration = new DefaultBlockIntegration();

        public static void place(String id, Location location) {
            if (id.startsWith("itemsadder:")) {
                itemsAdderIntegration.place(id, location);
            } else if (id.startsWith("oraxen:")) {
                oraxenIntegration.place(id, location);
            } else {
                defaultIntegration.place(id, location);
            }
        }
    }
}
