plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.2"
    id("application")
}

group = "ru.s21.rogue_game"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.googlecode.lanterna:lanterna:3.1.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
}

application {
    mainClass.set("ru.s21.rogue_game.Main")
}

tasks.jar {
    manifest.attributes["Main-Class"] = "ru.s21.rogue_game.Main"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.shadowJar {
    archiveBaseName.set("Rogue")
    archiveClassifier.set("")
    archiveVersion.set("1.0")
}

tasks.named("wrapper", Wrapper::class) {
    gradleVersion = "8.5"
}

tasks.register("Rogue", Exec::class) {
    group = "application"
    dependsOn("shadowJar")
    executable = "java"
    setArgs(listOf("-jar", "build/libs/Rogue-1.0.jar"))
}
