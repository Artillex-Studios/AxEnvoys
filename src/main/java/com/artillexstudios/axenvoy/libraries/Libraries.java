package com.artillexstudios.axenvoy.libraries;

import com.artillexstudios.axapi.libs.libby.Library;
import com.artillexstudios.axapi.libs.libby.relocation.Relocation;

public enum Libraries {
    SLF4J("org.slf4j:slf4j-api:2.0.9"),
    COMMONS_IO("commons-io:commons-io:2.15.0"),
    COMMONS_TEXT("org{}apache{}commons:commons-text:1.11.0"),
    MATH_3("org{}apache{}commons:commons-math3:3.6.1");

    private final Library library;

    Libraries(String library, Relocation relocation) {
        String[] split = library.split(":");

        this.library = Library.builder()
                .groupId(split[0])
                .artifactId(split[1])
                .version(split[2])
                .relocate(relocation)
                .build();
    }

    Libraries(String library) {
        String[] split = library.split(":");

        this.library = Library.builder()
                .groupId(split[0])
                .artifactId(split[1])
                .version(split[2])
                .build();
    }

    public Library getLibrary() {
        return library;
    }
}
