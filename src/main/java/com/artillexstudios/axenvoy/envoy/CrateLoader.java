package com.artillexstudios.axenvoy.envoy;

import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.utils.ConfigUtils;
import com.artillexstudios.axenvoy.utils.FileUtils;
import com.artillexstudios.axenvoy.utils.StringUtils;
import dev.dejvokep.boostedyaml.YamlDocument;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.File;

public final class CrateLoader {
    public static final ObjectArrayList<Crate> crates = new ObjectArrayList<>();

    public static void loadAll() {
        crates.clear();
        File[] files = new File(FileUtils.MAIN_DIRECTORY.toFile(), "crates/").listFiles();
        if (files == null) return;

        for (File file : files) {
            if (!ConfigUtils.testFile(file)) continue;

            YamlDocument document;
            try {
                document = YamlDocument.create(file);
            } catch (Exception exception) {
                AxEnvoyPlugin.getInstance().getComponentLogger().error(StringUtils.format("<color:#ff0000>Could not load file %s! Please check if there were any YAML syntax errors!</color>".formatted(file.getName())));
                continue;
            }

            crates.add(new Crate(document));
        }
    }
}
