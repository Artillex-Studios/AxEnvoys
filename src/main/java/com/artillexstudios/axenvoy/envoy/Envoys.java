package com.artillexstudios.axenvoy.envoy;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axenvoy.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Envoys {
    private static final Logger LOGGER = LoggerFactory.getLogger(Crates.class);
    private static final HashMap<String, Envoy> TYPES = new HashMap<>();
    private static final File CRATE_TYPES_FOLDER = FileUtils.PLUGIN_DIRECTORY.resolve("envoys/").toFile();
    private static boolean isReloading;

    public static void reload() {
        Scheduler.get().runAsync(() -> {
            if (isReloading) return;

            isReloading = true;
            if (CRATE_TYPES_FOLDER.mkdirs()) {
                FileUtils.copyFromResource("envoys");
            }

            Collection<File> files = org.apache.commons.io.FileUtils.listFiles(CRATE_TYPES_FOLDER, new String[]{"yaml", "yml"}, true);

            for (File file : files) {
                Envoy envoy = TYPES.get(file.getName()
                        .replace(".yml", "")
                        .replace(".yaml", ""));

                if (envoy == null) {
                    new Envoy(file);
                } else {
                    envoy.reload();
                }
            }

            ArrayList<Envoy> removedTypes = new ArrayList<>();
            TYPES.entrySet().removeIf((entry) -> {
                boolean contains = files.contains(entry.getValue().getFile());

                if (!contains) {
                    removedTypes.add(entry.getValue());
                }

                isReloading = false;
                return !contains;
            });

            List<Envoy> envoyTypes = new ArrayList<>(Envoys.getTypes().values());
            int chestSize = envoyTypes.size();

            for (int i = 0; i < chestSize; i++) {
                Envoy envoy = envoyTypes.get(i);

                if (removedTypes.contains(envoy)) {
                    envoy.stop();
                } else {
                    envoy.reload();
                }
            }

            isReloading = false;
        });
    }

    public static Envoy valueOf(String name) {
        return TYPES.get(name.toLowerCase(Locale.ENGLISH));
    }

    public static void register(Envoy envoy) {
        if (TYPES.containsKey(envoy.getName())) {
            LOGGER.warn("An envoy type named {} has already been registered! Skipping!", envoy.getName());
            return;
        }

        TYPES.put(envoy.getName().toLowerCase(Locale.ENGLISH), envoy);
    }

    public static Map<String, Envoy> getTypes() {
        return Map.copyOf(TYPES);
    }
}
