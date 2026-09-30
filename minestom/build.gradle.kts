plugins {
    java
    alias(libs.plugins.shadow)
    `maven-publish`
    jacoco
}

dependencies {
    // API
    implementation(project(":api"))
    // Minestom
    implementation(platform(libs.mycelium.bom))
    // Extension system: provided by the host at runtime, the processor generates extension.json
    annotationProcessor(platform(libs.minestom.extensions.bom))
    annotationProcessor(libs.minestom.extensions.processor)
    compileOnly(platform(libs.minestom.extensions.bom))
    compileOnly(libs.minestom.extensions)
    compileOnly(libs.minestom.extensions.processor)
    // Togglz
    implementation(libs.togglz)
    // LuckPerms API
    compileOnly(libs.luckperms.api)
    compileOnly(libs.minestom)
    compileOnly(libs.adventure.minimessage)

    testImplementation(libs.minestom)
    testImplementation(libs.minestom.testing)
    testImplementation(libs.luckperms.api)
    testImplementation(platform(libs.minestom.extensions.bom))
    testImplementation(libs.minestom.extensions)
    testImplementation(libs.adventure.minimessage)
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.platform.launcher)
    testImplementation(libs.junit.params)
    testRuntimeOnly(libs.junit.engine)
}

// Smoke test: loads the built shadow jar through Minestom's extension manager. It needs its own source set
// because Minestom's ExtensionClassLoader is parent-first, so Butterfly's own classes must not be on the
// test classpath (a real host does not have them either).
val smokeTest: SourceSet by sourceSets.creating {
    java.srcDir("src/test/java")
    java.include(
        "**/ExtensionLoadingTest.java",
        "**/Stubs.java",
        "**/FakeLuckPerms.java",
        "**/LuckPermsRegistration.java"
    )
}
dependencies {
    "smokeTestImplementation"(platform(libs.mycelium.bom))
    "smokeTestImplementation"(platform(libs.minestom.extensions.bom))
    "smokeTestImplementation"(libs.minestom)
    "smokeTestImplementation"(libs.minestom.testing)
    "smokeTestImplementation"(libs.minestom.extensions)
    "smokeTestImplementation"(libs.luckperms.api)
    "smokeTestImplementation"(libs.adventure.minimessage)
    "smokeTestImplementation"(libs.junit.api)
    "smokeTestImplementation"(libs.junit.platform.launcher)
    "smokeTestRuntimeOnly"(libs.junit.engine)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

// The processor generates extension.json but cannot know the project version.
tasks.compileJava {
    options.compilerArgs.add("-Aminestom.extension.version=${rootProject.version}")
}

tasks {
    jar {
        archiveClassifier.set("unshaded")
    }
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("butterfly-minestom.jar")
        mergeServiceFiles()
    }
    test {
        useJUnitPlatform()
        finalizedBy(project.tasks.jacocoTestReport)
        jvmArgs("-Dminestom.inside-test=true")
        systemProperty("butterfly.expected.version", rootProject.version.toString())
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
    val smokeTestTask = register<Test>("smokeTest") {
        description = "Loads the shadow jar through Minestom's extension manager"
        group = "verification"
        testClassesDirs = smokeTest.output.classesDirs
        classpath = smokeTest.runtimeClasspath
        useJUnitPlatform()
        dependsOn(shadowJar)
        jvmArgs("-Dminestom.inside-test=true")
        systemProperty("butterfly.extension.jar", layout.buildDirectory.file("libs/butterfly-minestom.jar").get().asFile.absolutePath)
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
    check {
        dependsOn(smokeTestTask)
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
        artifactId = "butterfly-minestom"
        groupId = rootProject.group as String
        pom {
            name = "Butterfly Minestom Library"
            description = "A simple library to support luckperms prefix, suffix and more in Minestom."
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