plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("xyz.jpenilla.run-paper") version "2.0.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"

}

group = "net.onelitefeather"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = uri("https://papermc.io/repo/repository/maven-public/"))
    maven(url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/"))

}

dependencies {
    // Paper
    compileOnly("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")
    implementation("cloud.commandframework:cloud-annotations:1.7.1")
    annotationProcessor("cloud.commandframework:cloud-annotations:1.7.1")

    implementation ("cloud.commandframework:cloud-paper:1.8.0")

}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

bukkit {
    main = "net.onelitefeather.butterfly.Butterfly"
    apiVersion = "1.19"

    softDepend = listOf("LuckPerms")
}


tasks {
    runServer {
        minecraftVersion("1.19.3")
    }

    compileJava {
        options.release.set(17)
        options.encoding = "UTF-8"
    }
}

