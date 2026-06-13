import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    java
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.plugin.yml)
    `maven-publish`
    jacoco
}

dependencies {
    // LuckPerms API
    compileOnly(libs.luckperms.api)
    // Togglz
    implementation(libs.togglz)
    // API
    implementation(project(":api"))
    // Paper API
    compileOnly(libs.paper)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks {
    runServer {
        minecraftVersion("1.21.7")
    }
    jar {
        archiveClassifier.set("unshaded")
    }
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("butterfly-paper.jar")
        mergeServiceFiles()
    }
    test {
        finalizedBy(project.tasks.jacocoTestReport)
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
    jacocoTestReport {
        reports {
            xml.required.set(true)
            html.required.set(true)
            csv.required.set(false)
        }
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifact(project.tasks.getByName("shadowJar"))
        version = rootProject.version as String
        artifactId = "butterfly-paper"
        groupId = rootProject.group as String
        pom {
            name = "Bunterfly Paper Plugin"
            description = "A simple paper plugins to support LuckPerms prefix, suffix and more in Paper."
            url = "https://github.com/OneLiteFeatherNET/Butterfly"
            licenses {
                license {
                    name = "The Apache License, Version 2.0"
                    url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                }
            }
            developers {
                developer {
                    id = "themeinerlp"
                    name = "Phillipp Glanz"
                    email = "p.glanz@madfix.me"
                }
            }
            scm {
                connection = "scm:git:git://github.com:OneLiteFeatherNET/Butterfly.git"
                developerConnection = "scm:git:ssh://git@github.com:OneLiteFeatherNET/Butterfly.git"
                url = "https://github.com/OneLiteFeatherNET/Butterfly"
            }
        }
    }

    repositories {
        maven {
            authentication {
                credentials(PasswordCredentials::class) {
                    // Those credentials need to be set under "Settings -> Secrets -> Actions" in your repository
                    username = System.getenv("ONELITEFEATHER_MAVEN_USERNAME")
                    password = System.getenv("ONELITEFEATHER_MAVEN_PASSWORD")
                }
            }

            name = "OneLiteFeatherRepository"
            val releasesRepoUrl = uri("https://repo.onelitefeather.dev/onelitefeather-releases")
            val snapshotsRepoUrl = uri("https://repo.onelitefeather.dev/onelitefeather-snapshots")
            url = if (version.toString().contains("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
}

paper {
    name = "Butterfly"
    main = "net.onelitefeather.butterfly.bukkit.Butterfly"
    apiVersion = "1.19"
    // version = /*publishData.getVersion(true)*/
    author = "TheMeinerLP"
    authors = listOf("theShadowsDust")
    serverDependencies {
        register("LuckPerms") {
            load = PaperPluginDescription.RelativeLoadOrder.AFTER
        }
    }
}

