plugins {
    java
}

dependencies {
    compileOnly(libs.luckperms.api)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks {
    jar {
        archiveClassifier.set("unshaded")
    }
}
