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

import com.artillexstudios.axenvoy.utils.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.file.YamlFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class AbstractConfig {
    private YamlFile config;
    private AbstractConfig parent;

    public YamlFile getConfig() {
        return this.config;
    }

    protected void reload(final Path path, Class<? extends AbstractConfig> clazz, AbstractConfig instance, AbstractConfig parent) {
        this.config = new YamlFile(path.toFile());
        this.parent = parent;

        Logger logger = LoggerFactory.getLogger(clazz);

        if (!FileUtils.getSuggestions(path.toFile(), logger)) {
            return;
        }

        try {
            getConfig().createOrLoadWithComments();
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
                    setComment(key.value(), comment.value());
                }
            } catch (Throwable e) {
                logger.error("An issue occurred while loading file: {}", path.toFile(), e);
            }
        });

        // save yaml to disk
        try {
            getConfig().save();
        } catch (IOException e) {
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

    protected void setComment(String path, @Nullable String comment) {
        getConfig().setComment(path, comment, CommentType.BLOCK);
    }

    protected @Nullable Object get(String path, @Nullable Object def) {
        Object val = get(path);
        return val == null ? def : val;
    }

    protected @Nullable Object get(String path) {
        Object value = getConfig().get(path);
        if (!(value instanceof ConfigurationSection section)) {
            return value;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            String rawValue = section.getString(key);
            if (rawValue == null) {
                continue;
            }
            map.put(key, addToMap(rawValue));
        }
        return map;
    }

    protected Object addToMap(String rawValue) {
        return rawValue;
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
        String value();
    }
}