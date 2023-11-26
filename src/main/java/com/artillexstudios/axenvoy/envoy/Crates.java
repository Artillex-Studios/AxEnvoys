package com.artillexstudios.axenvoy.envoy;

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

public final class Crates {
    private static final Logger LOGGER = LoggerFactory.getLogger(Crates.class);
    private static final HashMap<String, CrateType> TYPES = new HashMap<>();
    private static final File CRATE_TYPES_FOLDER = FileUtils.PLUGIN_DIRECTORY.resolve("crates/").toFile();

    public static void reload() {
        if (CRATE_TYPES_FOLDER.mkdirs()) {
            FileUtils.copyFromResource("crates");
        }

        Collection<File> files = org.apache.commons.io.FileUtils.listFiles(CRATE_TYPES_FOLDER, new String[]{"yaml", "yml"}, true);

        for (File file : files) {
            CrateType crateType = TYPES.get(file.getName()
                    .replace(".yml", "")
                    .replace(".yaml", ""));

            if (crateType == null) {
                new CrateType(file);
            } else {
                crateType.reload();
            }
        }

        ArrayList<CrateType> removedTypes = new ArrayList<>();
        TYPES.entrySet().removeIf((entry) -> {
            boolean contains = files.contains(entry.getValue().getFile());

            if (!contains) {
                removedTypes.add(entry.getValue());
            }

            return !contains;
        });

        List<CrateType> crateTypes = new ArrayList<>(Crates.getTypes().values());
        int chestSize = crateTypes.size();

        for (int i = 0; i < chestSize; i++) {
            CrateType crateType = crateTypes.get(i);

            if (removedTypes.contains(crateType)) {
                crateType.remove();
            } else {
                crateType.reload();
            }
        }
    }

    public static CrateType valueOf(String name) {
        return TYPES.get(name.toLowerCase(Locale.ENGLISH));
    }

    public static void register(CrateType crateType) {
        if (TYPES.containsKey(crateType.getName())) {
            LOGGER.warn("A crate type named {} has already been registered! Skipping!", crateType.getName());
            return;
        }

        TYPES.put(crateType.getName().toLowerCase(Locale.ENGLISH), crateType);
    }

    public static Map<String, CrateType> getTypes() {
        return Map.copyOf(TYPES);
    }
}
