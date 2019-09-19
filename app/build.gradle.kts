import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import java.util.Properties

plugins {
    id("com.diffplug.gradle.spotless") version "3.24.2"

    id("com.android.application")
    id("com.github.b3er.local.properties")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-android-extensions")
    id("androidx.navigation.safeargs.kotlin")
}

val local = Properties()
val localProperties: File = rootProject.file("local.properties")
if (localProperties.exists()) {
    localProperties.inputStream().use { local.load(it) }
}

val KEYSTORE_FILE = rootProject.extra.get("KEYSTORE_FILE") as File
val TRAVIS_BUILD = rootProject.extra.get("TRAVIS_BUILD") as Boolean

val kotlin_version = rootProject.extra.get("KOTLIN_VERSION") as String

val STRIPE_API_TOKEN = System.getenv("STRIPE_API_TOKEN") ?: "YOUR_API_KEY"
val MAPBOX_KEY = System.getenv("MAPBOX_KEY") ?: "pk.eyJ1IjoiYW5nbWFzMSIsImEiOiJjanNqZDd0N2YxN2Q5NDNuNTBiaGt6eHZqIn0.BCrxjW6rP_OuOuGtbhVEQg"
val PAYPAL_CLIENT_ID= System.getenv("PAYPAL_CLIENT_ID") ?: "YOUR_API_KEY"
val LOCAL_KEY_PRESENT = project.hasProperty("SIGNING_KEY_FILE") && rootProject.file(local["SIGNING_KEY_FILE"]!!).exists()


android {
    dataBinding.isEnabled = true
    compileSdkVersion(28)
    defaultConfig {
        applicationId = "com.eventyay.attendee"
        minSdkVersion(21)
        targetSdkVersion(28)
        versionCode = 14
        versionName = "0.7.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
    }
    signingConfigs {
        if (TRAVIS_BUILD) {
            create("release") {
                storeFile = KEYSTORE_FILE
                storePassword = System.getenv("STORE_PASS")
                keyAlias = System.getenv("ALIAS")
                keyPassword = System.getenv("KEY_PASS")
            }
        } else if (LOCAL_KEY_PRESENT) {
            create("release") {
                storeFile = rootProject.file(local.getProperty("SIGNING_KEY_FILE"))
                storePassword = local.getProperty("STORE_PASS")
                keyAlias = local.getProperty("ALIAS")
                keyPassword = local.getProperty("KEY_PASS")
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            buildConfigField("String", "DEFAULT_BASE_URL", "\"https://api.eventyay.com/v1/\"")
            buildConfigField("String", "MAPBOX_KEY", "\"$MAPBOX_KEY\"")
            buildConfigField("String", "STRIPE_API_KEY", "\"$STRIPE_API_TOKEN\"")
            buildConfigField("String", "PAYPAL_CLIENT_ID", "\"$PAYPAL_CLIENT_ID\"")
            resValue("string",  "FRONTEND_HOST", "eventyay.com")
            if (LOCAL_KEY_PRESENT || TRAVIS_BUILD)
                signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug") {
            buildConfigField("String", "DEFAULT_BASE_URL", "\"https://open-event-api-dev.herokuapp.com/v1/\"")
            buildConfigField("String", "MAPBOX_KEY", "\"$MAPBOX_KEY\"")
            buildConfigField("String", "STRIPE_API_KEY", "\"$STRIPE_API_TOKEN\"")
            buildConfigField("String", "PAYPAL_CLIENT_ID", "\"$PAYPAL_CLIENT_ID\"")
            resValue("string", "FRONTEND_HOST", "open-event-fe.netlify.com")
        }
    }

    flavorDimensions("default")
    productFlavors {
        create("fdroid") {
            setDimension("default")
        }

        create("playStore") {
            setDimension("default")
        }
    }
    compileOptions {
        targetCompatibility = JavaVersion.VERSION_1_8
        sourceCompatibility = JavaVersion.VERSION_1_8
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
    packagingOptions {
        pickFirst("kotlin/**")
    }
    lintOptions {
        disable("MissingTranslation")
        warning("InvalidPackage")
    }
    androidExtensions {
        isExperimental = true
    }
    aaptOptions {
        cruncherEnabled = false
    }
    kotlinOptions {
        val options = this as KotlinJvmOptions
        options.jvmTarget = "1.8"
    }
}

spotless {
    kotlin {
        ktlint().userData(mapOf("android" to "true", "color" to "true", "max_line_length" to "120", "reporter" to "checkstyle"))
        target("**/*.kt")
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {

    implementation(fileTree("dir" to "libs", "include" to listOf("*.jar")))
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.0-beta2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.1.0-beta04")
    implementation("com.google.android.material:material:1.1.0-alpha10")
    implementation("androidx.browser:browser:1.0.0")
    implementation("androidx.exifinterface:exifinterface:1.0.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0-alpha04")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.2.0-alpha04")
    implementation("androidx.lifecycle:lifecycle-reactivestreams:2.2.0-alpha04")
    implementation("androidx.room:room-runtime:2.1.0")
    implementation("androidx.room:room-rxjava2:2.1.0")
    kapt("androidx.room:room-compiler:2.1.0")
    testImplementation("androidx.room:room-testing:2.1.0")
    implementation("androidx.preference:preference:1.1.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.50")

    // KTX
    implementation("androidx.core:core-ktx:1.1.0")
    implementation("androidx.fragment:fragment-ktx:1.1.0")
    implementation("androidx.collection:collection-ktx:1.1.0")
    implementation("androidx.lifecycle:lifecycle-reactivestreams-ktx:2.1.0")

    // Koin
    implementation("org.koin:koin-android:2.0.1")
    implementation("org.koin:koin-androidx-scope:2.0.1")
    implementation("org.koin:koin-androidx-viewmodel:2.0.1")

    // Location Play Service
    "playStoreImplementation"("com.google.android.gms:play-services-location:17.0.0")

    //Smart Auth Play Service
    "playStoreImplementation"("com.google.android.gms:play-services-auth:17.0.0")

    // Timber
    implementation("com.jakewharton.timber:timber:4.7.1")
    implementation("com.jakewharton.threetenabp:threetenabp:1.2.1")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.6")
    implementation("com.github.jasminb:jsonapi-converter:0.9")
    implementation("com.squareup.okhttp3:logging-interceptor:4.2.0")
    implementation("com.squareup.retrofit2:retrofit:2.6.1")
    implementation("com.squareup.retrofit2:converter-jackson:2.6.1")

    // Cards Shimmer Animation
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    // RxJava
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")
    implementation("io.reactivex.rxjava2:rxjava:2.2.12")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.6.1")

    // Picasso
    implementation("com.squareup.picasso:picasso:2.71828")

    // Stripe
    implementation("com.stripe:stripe-android:11.1.1")

    // QR Code
    implementation("com.journeyapps:zxing-android-embedded:3.6.0")

    //Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.1.0") // For Kotlin use navigation-fragment-ktx
    implementation("androidx.navigation:navigation-ui-ktx:2.1.0") // For Kotlin use navigation-ui-ktx

    //Anko
    implementation("org.jetbrains.anko:anko-commons:0.10.8")
    implementation("org.jetbrains.anko:anko-design:0.10.8")

    //Mapbox java sdk
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-services:4.8.0")

    // SimpleCropView
    implementation("com.isseiaoki:simplecropview:1.1.8")

    // Stetho
    debugImplementation("com.facebook.stetho:stetho:1.5.1")
    debugImplementation("com.facebook.stetho:stetho-okhttp3:1.5.1")
    releaseImplementation("com.github.iamareebjamal:stetho-noop:1.2.1")
    testImplementation("com.github.iamareebjamal:stetho-noop:1.2.1")

    //LeakCanary
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.0-beta-3")

    // Paging
    implementation("androidx.paging:paging-runtime:2.1.0")
    implementation("androidx.paging:paging-rxjava2:2.1.0")

    // Searchable Spinner
    implementation("com.toptoche.searchablespinner:searchablespinnerlibrary:1.3.1")

    //ExpandableTextView
    implementation("at.blogc:expandabletextview:1.0.5")

    //ExpandableView
    implementation("net.cachapa.expandablelayout:expandablelayout:2.9.2")


    //PayPal
    implementation("com.paypal.sdk:paypal-android-sdk:2.16.0")

    testImplementation("junit:junit:4.12")
    testImplementation("org.threeten:threetenbp:1.4.0")
    testImplementation("org.koin:koin-test:2.0.1")
    testImplementation("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation("androidx.test:runner:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}
