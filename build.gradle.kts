plugins {
    kotlin("jvm") version "1.7.0"
    kotlin("plugin.serialization") version "1.6.20"
    id("org.jetbrains.compose") version "1.2.0-alpha01-dev753"
}

group = "de.miraculixx"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    //JDA - Discord API Wrapper
    implementation("net.dv8tion", "JDA", "5.0.0-alpha.13")
    implementation("com.github.minndevelopment", "jda-ktx","0.9.2-alpha.13")

    //JetBrains Libraries
    implementation("org.jetbrains.kotlinx", "kotlinx-datetime", "0.4.0")
    implementation(compose.desktop.linux_x64)

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
    jar {
        manifest {
            attributes["Main-Class"] = "de.miraculixx.mcord.MainKt"
        }

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(sourceSets.main.get().output)
        dependsOn(configurations.runtimeClasspath)
        from({
            configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
        })
    }
}
