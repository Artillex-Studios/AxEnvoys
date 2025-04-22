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
package com.artillexstudios.axenvoy.config;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.libs.boostedyaml.block.Block;
import com.artillexstudios.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.common.FlowStyle;
import com.artillexstudios.axapi.libs.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axenvoy.utils.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AbstractConfig {
    private Config config;
    private AbstractConfig parent;

    public Config getConfig() {
        return this.config;
    }

    protected void reload(final Path path, Class<? extends AbstractConfig> clazz, AbstractConfig instance, AbstractConfig parent) {
        this.config = new Config(path.toFile(), InputStream.nullInputStream(), GeneralSettings.builder().build(), LoaderSettings.builder().setDetailedErrors(true).build(), DumperSettings.builder().setEndMarker(false).setStartMarker(false).setFlowStyle(FlowStyle.BLOCK).build(), UpdaterSettings.builder().setAutoSave(true).build());
        this.parent = parent;

        Logger logger = LoggerFactory.getLogger(clazz);

        if (path.toFile().exists()) {
            if (!FileUtils.getSuggestions(path.toFile(), logger)) {
                return;
            }
        }

        try {
            getConfig().getBackingDocument().reload();
        } catch (Exception exception) {
            logger.error("Could not load yaml file: {}", path.toFile(), exception);
            return;
        }

        Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
            Key key = field.getDeclaredAnnotation(Key.class);
            Comment comment = field.getDeclaredAnnotation(Comment.class);
            if (key == null) {
                return;
            }

            try {
                Object value = getValue(key.value(), field.get(instance));
                if (field.getType() == String.class && !(value instanceof String)) {
                    value = String.valueOf(value);
                }

                field.set(instance, value instanceof String str ? StringEscapeUtils.unescapeJava(str) : value);

                if (comment != null) {
                    setComment(key.value(), Arrays.asList(comment.value()));
                } else {
                    setComment(key.value(), null);
                }
            } catch (Throwable e) {
                logger.error("An issue occurred while loading file: {}", path.toFile(), e);
            }
        });

        // save yaml to disk
        try {
            getConfig().save();
        } catch (Exception e) {
            logger.error("An issue occurred while loading file: {}", path.toFile(), e);
        }
    }

    protected @Nullable Object getValue(String path, @Nullable Object def) {
        if (getConfig().get(path) == null) {
            if (parent != null) {
                return parent.get(path, def);
            }

            set(path, def);
        }

        return get(path, def);
    }

    protected void setComment(String path, @Nullable List<String> comment) {
        Block<?> block = getConfig().getBackingDocument().getBlock(path);
        if (block == null) {
            return;
        }

        if (comment != null) {
            block.setComments(comment);
        }
    }

    protected @Nullable Object get(String path, @Nullable Object def) {
        Object val = get(path);
        return val == null ? def : val;
    }

    protected @Nullable Object get(String path) {
        Object value = getConfig().get(path);
        if (!(value instanceof Section section)) {
            return value;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        for (Object key : section.getKeys()) {
            Object rawValue = section.get(key.toString());
            if (rawValue == null) {
                continue;
            }
            map.put(key.toString(), rawValue);
        }
        return map;
    }

    protected void set(String path, @Nullable Object value) {
        getConfig().set(path, value);
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Key {
        String value();
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Comment {
        String[] value();
    }
}