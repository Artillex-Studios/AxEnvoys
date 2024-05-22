/*
 * MIT License
 *
 * Copyright (c) 2020-2023 William Blake Galbreath
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.artillexstudios.axenvoy.utils;

import com.artillexstudios.axapi.libs.yamlassist.YamlAssist;
import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtils {
    public static final Path PLUGIN_DIRECTORY = AxEnvoyPlugin.getInstance().getDataFolder().toPath();
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    public static boolean getSuggestions(File file, Logger logger) {
        List<String> suggestions = YamlAssist.getSuggestions(file);
        if (suggestions.isEmpty()) return true;

        logger.error("Can't load yaml file: {}", file.toPath());
        logger.error("Possible solutions:");
        for (String suggestion : suggestions) {
            logger.error(" - {}", suggestion);
        }

        return false;
    }

    public static void extractFile(Class<?> clazz, String filename, Path outDir, boolean replace) {
        try (InputStream in = clazz.getResourceAsStream("/" + filename)) {
            if (in == null) {
                Exception exception = new RuntimeException("Could not read file from jar! (" + filename + ")");
                LOGGER.error("Could not find file {} in the plugin's assets!", filename, exception);
                return;
            }

            Path path = outDir.resolve(filename);
            if (!Files.exists(path) || replace) {
                Files.createDirectories(path.getParent());
                Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            LOGGER.error("An unexpected error occurred while extracting file {} from plugin's assets!", filename, exception);
        }
    }

    public static void copyFromResource(@NotNull String path) {
        try (ZipFile zip = new ZipFile(Paths.get(AxEnvoyPlugin.getInstance().getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).toFile())) {
            for (Iterator<? extends ZipEntry> it = zip.entries().asIterator(); it.hasNext(); ) {
                ZipEntry entry = it.next();
                if (entry.getName().startsWith(path + "/")) {
                    if (!entry.getName().endsWith(".yaml") && !entry.getName().endsWith(".yml")) continue;
                    InputStream resource = AxEnvoyPlugin.getInstance().getResource(entry.getName());
                    if (resource == null) {
                        LOGGER.error("Could not find file {} in plugin's assets!", entry.getName());
                        continue;
                    }

                    Files.copy(resource, PLUGIN_DIRECTORY.resolve(entry.getName()));
                }
            }
        } catch (IOException | URISyntaxException exception) {
            LOGGER.error("An unexpected error occurred while extracting directory {} from plugin's assets!", path, exception);
        }
    }
}