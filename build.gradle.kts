import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.1.0"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
    id("org.sonarqube") version "4.3.0.3225"
    jacoco
}

group = "net.onelitefeather"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    // Paper
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

paper {
    main = "net.onelitefeather.butterfly.ButterflyPlugin"
    apiVersion = "1.20"
    author = "TheMeinerLP"
    authors = listOf("theShadowsDust")
    serverDependencies {
        register("LuckPerms") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
    }
}


tasks {
    runServer {
        minecraftVersion("1.20.1")
    }

    compileJava {
        options.release.set(17)
        options.encoding = "UTF-8"
    }
    jacocoTestReport {
        dependsOn(rootProject.tasks.test)
        reports {
            xml.required.set(true)
        }
    }

    test {
        finalizedBy(rootProject.tasks.jacocoTestReport)
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
    getByName<org.sonarqube.gradle.SonarTask>("sonar") {
        dependsOn(rootProject.tasks.test)
    }
    shadowJar {
        archiveFileName.set("${rootProject.name}.${archiveExtension.getOrElse("jar")}")
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "onelitefeather_projects_butterfly_AYZ5KHlm528bDWin57d_")
    }
}
