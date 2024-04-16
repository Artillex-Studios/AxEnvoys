package com.artillexstudios.axenvoy.locale;

import com.artillexstudios.axenvoy.AxEnvoyPlugin;
import org.bukkit.entity.HumanEntity;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Manages the languages files for translation support.
 */
public class LocaleManager {

    private final AxEnvoyPlugin plugin;
    public static final Locale[] SUPPORTED = new Locale[] {Locale.ENGLISH};
    public static HashMap<Locale, EnumMap<LocaleString, String>> locales = new HashMap<>();

    public static Locale defaultLocale = Locale.ENGLISH;

    public LocaleManager(AxEnvoyPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads a specific locale.
     * @param locale the locale type to load.
     */
    public void loadLocale(Locale locale) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("axEnvoys", locale);
            EnumMap<LocaleString, String> l = new EnumMap<>(LocaleString.class);

            for(LocaleString string : LocaleString.values()) {
                if(bundle.containsKey(string.getKey())) {
                    l.put(string, bundle.getString(string.getKey()));
                }
            }

            locales.put(locale, l);
        } catch (Exception e) {
            this.plugin.getLogger().warning("Could not load locale " + locale + e);
        }
    }

    /**
     * Gets the message from the locale.
     * @param id the id of the stored string in the locale files.
     * @return the translated message, or the key if he couldn't be translated.
     */
    public static String getMessage(LocaleString id) {
        String t = locales.get(defaultLocale).get(id);
        if(t == null) return id.getKey();
        return t;
    }

    /**
     * Gets the message from the locale.
     * @param id the id of the stored string in the locale files.
     * @param locale the locale to get the message from.
     * @return the translated message, or the key if he couldn't be translated.
     */
    public static String getMessage(LocaleString id, Locale locale) {
        String t = locales.get(locale).get(id);
        if(t == null) return id.getKey();
        return t;
    }

    /**
     * Loads the locales from the plugin's resources.
     */
    public void loadLocales() {
        for(Locale locale : SUPPORTED) {
            loadLocale(locale);
        }
    }

}
