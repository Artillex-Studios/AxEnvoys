import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.gradleup.shadow") version "8.3.11"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

group = "com.artillexstudios.axenvoy"
version = "2.4.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.oraxen.com/releases")
    maven("https://repo.artillex-studios.com/releases")
    maven("https://repo.alessiodp.com/releases/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.12.2")
    compileOnly("org.apache.commons:commons-math3:3.6.1")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.9")
    compileOnly("com.github.LoneDev6:api-itemsadder:3.6.1")
    compileOnly("com.github.oraxen:oraxen:1.161.0") {
        exclude(group = "gs.mclo", module = "mclogs")
        exclude(group = "com.ticxo", module = "PlayerAnimator")
        exclude(group = "me.gabytm.util", module = "actions-spigot")
        exclude(group = "net.kyori", module = "*")
        exclude(group = "com.jeff_media", module = "*")
    }
    implementation("com.artillexstudios.axapi:axapi:2.1.0-DEV-3:all")
    compileOnly("org.apache.commons:commons-text:1.11.0")
    compileOnly("commons-io:commons-io:2.15.0")
    compileOnly("org.slf4j:slf4j-api:2.0.9")
    compileOnly("net.kyori:adventure-api:4.25.0")
}

tasks.processResources {
    filesMatching("**/*.yml") {
        expand(project.properties)
    }
    filesMatching("**/*.properties") {
        expand(project.properties)
    }
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")

    relocate("org.bstats", "com.artillexstudios.axenvoy.libs.bstats")
    relocate("net.byteflux", "com.artillexstudios.axenvoy.libs.libby")
    relocate("com.artillexstudios.axapi", "com.artillexstudios.axenvoy.libs.axapi")
    relocate("revxrsal.commands", "com.artillexstudios.axenvoy.libs.lamp")
    relocate("org.apache.commons.math3", "com.artillexstudios.axenvoy.libs.axapi.libs.math3")
}

tasks.named("jar") { enabled = false }
tasks.named("build") { dependsOn(tasks.named("shadowJar")) }
