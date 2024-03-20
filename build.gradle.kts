plugins {
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.serialization") version "1.7.20"
    application
}

group = "de.miraculixx"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
}

dependencies {
    implementation("net.dv8tion", "JDA", "5.0.0-beta.13")
    implementation("com.github.minndevelopment", "jda-ktx", "0.10.0-beta.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

    implementation("io.ktor", "ktor-client-core-jvm", "2.0.1")
    implementation("io.ktor", "ktor-client-cio", "2.0.1")

    implementation("org.slf4j", "slf4j-nop", "2.0.0-alpha7")
    implementation("ch.qos.logback", "logback-classic", "1.2.11")
}

application {
    mainClass.set("de.miraculixx.mcord.MainKt")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}