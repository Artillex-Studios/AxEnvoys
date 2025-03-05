package com.artillexstudios.axenvoy.libraries;

import revxrsal.zapper.Dependency;

public enum Libraries {
    SLF4J("org.slf4j:slf4j-api:2.0.9"),
    COMMONS_IO("commons-io:commons-io:2.15.0"),
    COMMONS_TEXT("org{}apache{}commons:commons-text:1.11.0");

    private final Dependency library;

    Libraries(String library) {
        String[] split = library.split(":");

        this.library = new Dependency(split[0].replace("{}", "."),
                split[1].replace("{}", "."),
                split[2].replace("{}", ".")
        );
    }

    public Dependency library() {
        return this.library;
    }
}
