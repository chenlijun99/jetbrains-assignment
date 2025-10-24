plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "clj-zstd-util"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include("modules:zstd-jni")
