// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    extra.set(Strings.KEYSTORE_FILE , rootProject.file("scripts/key.jks"))
    val keystoreFile = extra.get(Strings.KEYSTORE_FILE)
    if (keystoreFile is File) {
        extra.set(Strings.TRAVIS_BUILD, keystoreFile.exists() && System.getenv("TRAVIS") == "true")
    }

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
