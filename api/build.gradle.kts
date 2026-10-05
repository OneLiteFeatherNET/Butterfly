plugins {
    java
}

dependencies {
    compileOnly(platform(libs.mycelium.bom))
    compileOnly(libs.luckperms.api)
    // provided by Paper and Minestom at runtime
    compileOnly(libs.adventure.api)
    compileOnly(libs.adventure.minimessage)
    // provided by Paper and Minestom at runtime
    compileOnly(libs.slf4j.api)
    implementation(libs.avaje.config)

    testImplementation(platform(libs.mycelium.bom))
    testImplementation(libs.slf4j.api)
    testImplementation(libs.adventure.api)
    testImplementation(libs.adventure.minimessage)
    testImplementation(libs.adventure.plain)
    testImplementation(libs.luckperms.api)
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.platform.launcher)
    testImplementation(libs.junit.params)
    testRuntimeOnly(libs.junit.engine)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks {
    jar {
        archiveClassifier.set("unshaded")
    }
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
