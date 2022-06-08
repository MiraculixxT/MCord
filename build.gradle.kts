
plugins {
    kotlin("jvm") version "1.6.10"
    java
    id("org.jetbrains.kotlin.plugin.serialization") version "1.6.20"
}

group = "de.miraculixx"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("net.dv8tion:JDA:5.0.0-alpha.11")
    implementation("org.slf4j:slf4j-nop:2.0.0-alpha7")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("io.ktor:ktor-client-core-jvm:2.0.1")
    implementation("io.ktor:ktor-client-cio:2.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("club.minnced:discord-webhooks:0.8.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
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
