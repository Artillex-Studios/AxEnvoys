package com.artillexstudios.axenvoy.config.impl;

import com.artillexstudios.axenvoy.config.AbstractConfig;
import com.artillexstudios.axenvoy.utils.FileUtils;

public class Messages extends AbstractConfig {

    @Key("messages.prefix")
    public String PREFIX = "<#00FFAA>&lAxEnvoy &7» ";

    @Key("messages.collect")
    public String COLLECT = "%player% collected a %crate% crate! But fear not, there is still %amount% left!";

    @Key("messages.start.one")
    public String SINGLE_START = "An envoy event began! A crate spawned at %location%!";

    @Key("messages.start.multiple")
    public String MULTIPLE_START= "An envoy event began! %amount% crates spawned around %location%!";

    @Key("messages.flare-start.one")
    public String SINGLE_START_FLARE = "%player% started an envoy! A crate spawned at %location%!";

    @Key("messages.flare-start.multiple")
    public String MULTIPLE_START_FLARE = "%player% started an envoy! %amount% crates spawned around %location%";

    @Key("messages.set-center")
    public String SET_CENTER = "Set center to your location!";

    @Key("messages.set-predefined")
    public String SET_PREDEFINED = "You added a predefined spawn to this envoy!";

    @Key("messages.remove-predefined")
    public String REMOVE_PREDEFINED = "You removed a predefined spawn from this envoy!";

    @Key("messages.stopped")
    public String STOPPED = "The envoy was forcefully stopped.";

    @Key("messages.cooldown")
    public String COOLDOWN = "Woah! Slow down! You need to wait %cooldown% seconds before opening %crate%!";

    @Key("messages.ended")
    public String ENDED = "The event has ended!";

    @Key("messages.already-active")
    public String ALREADY_ACTIVE = "This event is already running!";

    @Key("messages.flare-disabled")
    public String FLARE_DISABLED = "This envoy''s flare is disabled!";

    @Key("messages.reload")
    public String RELOAD = "Reloaded! Took: %time% ms!";

    @Key("messages.no-envoy-found")
    public String NO_ENVOY_FOUND = "There is no loaded envoy named that!";

    @Key("messages.alert")
    public String ALERT = "An envoy event will start in %time%! Get ready!";

    @Key("messages.not-enough-autostart")
    public String NOT_ENOUGH_AUTO_START = "There weren''t enough people to auto-start an envoy event! Rescheduling!";

    @Key("messages.crate-spawn-message")
    public String CRATE_SPAWN = "A crate spawned at %location%!";

    @Key("messages.location-format")
    public String LOCATION_FORMAT = "%world% %x% %y% %z%";

    @Key("messages.toggle.on")
    public String TOGGLE_ON = "&aYou have enabled the envoy collect messages!";

    @Key("messages.toggle.off")
    public String TOGGLE_OFF = "&cYou have disabled the envoy collect messages!";

    @Key("messages.editor.join")
    public String EDITOR_JOIN = "You have entered the editor!";

    @Key("messages.editor.leave")
    public String EDITOR_LEAVE = "You have left the editor!";

    @Key("messages.placeholder.running")
    public String PLACEHOLDER_RUNNING = "&aRunning!";

    @Key("messages.placeholder.not-running")
    public String PLACEHOLDER_NOT_RUNNING = "&cNot running";

    @Key("messages.placeholder.remaining")
    public String PLACEHOLDER_REMAINING = "%remaining% crates left";

    @Key("messages.placeholder.remaining-time")
    public String PLACEHOLDER_REMAINING_TIME = "%time%";

    @Key("messages.placeholder.until-next")
    public String PLACEHOLDER_UNTIL_NEXT = "%time%";

    protected final String fileName;

    public Messages(String fileName) {
        this.fileName = fileName;
    }

    public void reload() {
        this.reload(FileUtils.PLUGIN_DIRECTORY.resolve(fileName), Messages.class, this, null);
    }
}
