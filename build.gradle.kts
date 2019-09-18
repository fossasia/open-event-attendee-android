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
        classpath("com.android.tools.build:gradle:3.5.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${extra.get("KOTLIN_VERSION")}")
        classpath("android.arch.navigation:navigation-safe-args-gradle-plugin:1.0.0")
        classpath("gradle.plugin.com.github.b3er.local.properties:local-properties-plugin:1.1")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
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
