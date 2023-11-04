plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    // Minimessage
    implementation("net.kyori:adventure-text-minimessage:4.14.0")
    // LuckPerms API
    compileOnly("net.luckperms:api:5.4")
    // API
    implementation(project(":api"))
    // Minestom
    compileOnly("net.onelitefeather.microtus:Minestom:1.1.1-SNAPSHOT+a8e43bb")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks {

    compileJava {
        options.release.set(17)
        options.encoding = "UTF-8"
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
    shadowJar {
        archiveFileName.set("${rootProject.name}.${archiveExtension.getOrElse("jar")}")
    }
}