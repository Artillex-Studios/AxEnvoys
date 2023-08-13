package com.artillexstudios.axenvoy.envoy;

import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import com.artillexstudios.axenvoy.utils.ConfigUtils;
import com.artillexstudios.axenvoy.utils.FileUtils;
import com.artillexstudios.axenvoy.utils.StringUtils;
import dev.dejvokep.boostedyaml.YamlDocument;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.io.File;

public class EnvoyLoader {
    public static final Object2ObjectArrayMap<String, Envoy> envoys = new Object2ObjectArrayMap<>();

    public static void loadAll() {
        for (Envoy value : envoys.values()) {
            value.stop();
        }

        envoys.clear();
        File[] files = new File(FileUtils.MAIN_DIRECTORY.toFile(), "envoys/").listFiles();
        if (files == null) return;

        for (File file : files) {
            if (!ConfigUtils.testFile(file)) continue;

            YamlDocument document;
            try {
                document = YamlDocument.create(file);
            } catch (Exception exception) {
                AxEnvoyPlugin.getInstance().getLogger().warning(StringUtils.format("<color:#ff0000>Could not load file %s! Please check if there were any YAML syntax errors!</color>".formatted(file.getName())));
                continue;
            }

            envoys.put(file.getName().replace(".yml", "").replace(".yaml", ""), new Envoy(document));
        }
    }
}
