package com.artillexstudios.axenvoy.locale;

/**
 * A String that is stored trough language files. Enums are nice for that because of the possibility of EnumMaps.
 */
public enum LocaleString {

    private String key;

    private LocaleString(String key) {
        this.key = key;
    }

}
