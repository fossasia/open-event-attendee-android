// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    extra.set("KOTLIN_VERSION", "1.3.50")
    extra.set("KEYSTORE_FILE", rootProject.file("scripts/key.jks"))
    extra.set("TRAVIS_BUILD", System.getenv("TRAVIS") == "true" && (extra.get("KEYSTORE_FILE") as File).exists())

    repositories {
        google()
        mavenCentral()
        jcenter()
        maven(url = "https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath(Libs.com_android_tools_build_gradle)
        classpath(Libs.kotlin_gradle_plugin)
        classpath(Libs.navigation_safe_args_gradle_plugin)
        classpath(Libs.local_properties_plugin)

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}
plugins {
    id("de.fayard.buildSrcVersions") version "0.6.1"
}

allprojects {
    repositories {
        google()
        maven(url = "https://jitpack.io")
        mavenCentral()
        jcenter()
    }
}
tasks {
    val clean by registering(Delete::class) {
        delete(buildDir)
    }
}
