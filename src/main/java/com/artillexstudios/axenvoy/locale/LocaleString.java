package com.artillexstudios.axenvoy.locale;

/**
 * A String that is stored trough language files. Enums are nice for that because of the possibility of EnumMaps.
 */
public enum LocaleString {

    CRATE_COLLECT("crate.collect"),
    ENVOY_START("envoy.start"),
    ENVOY_START_MULTIPLE("envoy.start.multiple"),
    FLARE_START("flare.start"),
    FLARE_START_MULTIPLE("flare.start.multiple"),
    FLARE_DISABLED("flare.disabled"),


    CENTER_SET("center.set"),
    PREDEFINED_SET("predefined.set"),
    PREDEFINED_REMOVE("predefined.remove"),
    ENVOY_STOP("envoy.stop"),
    ENVOY_COOLDOWN("envoy.cooldown"),
    ENVOY_NOT_FOUND("envoy.found.none"),

    EVENT_END("event.end"),
    EVENT_ALREADY_RUNNING("event.running.already"),

    RELOAD("reload"),
    ALERT("alert"),
    AUTOSTART_NOT_ENOUGH("autostart.not"),
    LOCATION("location"),
    TIME_START("time.start"),

    ENVOY_START_MESSAGE_ON("messages.envoy.start.on"),
    ENVOY_START_MESSAGE_OFF("messages.envoy.start.off"),

    CRATE_SPAWN("crate.spawn"),

    EDITOR_JOIN("editor.join"),
    EDITOR_LEAVE("editor.leave"),

    PLACEHOLDER_RUNNING("placeholder.running"),
    PLACEHOLDER_IDLE("placeholder.idle"),
    PLACEHOLDER_REMAIN("placeholder.crates.remaining"),
    PLACEHOLDER_TIME_REMAIN("placeholder.time.remaining"),

    TIME_DAY("time.day"),
    TIME_HOUR("time.hour"),
    TIME_MINUTE("time.minute"),
    TIME_SECOND("time.second");




    private String key;

    private LocaleString(String key) {
        this.key = key;
    }

}
