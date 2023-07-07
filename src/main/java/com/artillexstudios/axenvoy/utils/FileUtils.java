package com.artillexstudios.axenvoy.utils;

import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtils {
    public static final Path MAIN_DIRECTORY = AxEnvoyPlugin.getInstance().getDataFolder().toPath();

    @NotNull
    public static InputStream getResource(@NotNull String name) {
        InputStream resource = AxEnvoyPlugin.getInstance().getResource(name);
        if (resource == null) {
            throw new RuntimeException("Could not find file %s in the plugin''s resources! Please notify the developers of this plugin!".formatted(name));
        }

        return resource;
    }

    public static void copyFromResource(@NotNull String path) {
        final File folder = new File(FileUtils.MAIN_DIRECTORY.toFile(), "/" + path);

        if (folder.mkdirs()) {
            System.out.println("aasd");
            try (ZipFile file = new ZipFile(AxEnvoyPlugin.getInstance().getClass().getProtectionDomain().getCodeSource().getLocation().getPath())) {
                for (Iterator<? extends ZipEntry> it = file.entries().asIterator(); it.hasNext();) {
                    ZipEntry entry = it.next();
                    if (entry.getName().startsWith(path + "/")) {
                        if (!entry.getName().endsWith(".yaml") && !entry.getName().endsWith(".yml")) continue;
                        Files.copy(FileUtils.getResource(entry.getName()), new File(MAIN_DIRECTORY.toFile(), "/" + entry.getName()).toPath());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
