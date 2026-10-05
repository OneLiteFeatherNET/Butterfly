plugins {
    java
}

dependencies {
    compileOnly(libs.luckperms.api)
    implementation(libs.avaje.config)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks {
    jar {
        archiveClassifier.set("unshaded")
    }
}
