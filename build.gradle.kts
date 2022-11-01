plugins {
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.serialization") version "1.7.20"
    //id("org.jetbrains.compose") version "1.2.0-alpha01-dev753"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "de.miraculixx"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
    google()
}

dependencies {
    //JDA - Discord API Wrapper
    implementation("net.dv8tion", "JDA", "5.0.0-alpha.22")
    implementation("com.github.minndevelopment", "jda-ktx","0.9.6-alpha.22")

    //JetBrains Libraries
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("org.jetbrains.kotlinx", "kotlinx-datetime", "0.4.0")
    implementation("org.apache.commons", "commons-text", "1.10.0")
    //implementation(compose.desktop.linux_x64)

    //Ktor - Web API Library
    implementation("io.ktor", "ktor-client-core-jvm", "2.0.1")
    implementation("io.ktor", "ktor-client-cio", "2.0.1")

    //Logging Libraries
    implementation("org.slf4j", "slf4j-nop", "2.0.0-alpha7")
    implementation("ch.qos.logback", "logback-classic", "1.2.11")

    //Data Storing Libraries (YAML + SQL)
    implementation("org.yaml", "snakeyaml", "1.21")
    implementation("org.mariadb.jdbc", "mariadb-java-client", "3.0.5")
}


tasks {
    assemble {
        dependsOn(shadowJar)
    }
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}