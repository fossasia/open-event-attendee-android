import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import java.util.Properties

plugins {
    id("com.diffplug.gradle.spotless") version "3.24.2"
    id("com.android.application")
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

lateinit var KEYSTORE_FILE: File
var TRAVIS_BUILD: Boolean = false
val LOCAL_KEY_PRESENT = project.hasProperty(Strings.SIGNING_KEY_FILE)
    && rootProject.file(local.getProperty(Strings.SIGNING_KEY_FILE)).exists()

rootProject.extra.get(Strings.KEYSTORE_FILE).let {
    if (it is File) {
        KEYSTORE_FILE = it
    }
}

rootProject.extra.get(Strings.TRAVIS_BUILD).let {
    if (it is Boolean) {
        TRAVIS_BUILD = it
    }
}

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
                storePassword = System.getenv(Strings.STORE_PASS)
                keyAlias = System.getenv(Strings.ALIAS)
                keyPassword = System.getenv(Strings.KEY_PASS)
            }
        } else if (LOCAL_KEY_PRESENT) {
            create("release") {
                storeFile = rootProject.file(local.getProperty(Strings.SIGNING_KEY_FILE))
                storePassword = local.getProperty(Strings.STORE_PASS)
                keyAlias = local.getProperty(Strings.ALIAS)
                keyPassword = local.getProperty(Strings.KEY_PASS)
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            buildConfigField("String", Strings.DEFAULT_BASE_URL, Constants.RELEASE_DEFAULT_BASE_URL)
            buildConfigField("String", Strings.MAPBOX_KEY, Constants.MAPBOX_KEY)
            buildConfigField("String", Strings.STRIPE_API_KEY, Constants.STRIPE_API_TOKEN)
            buildConfigField("String", Strings.PAYPAL_CLIENT_ID, Constants.PAYPAL_CLIENT_ID)
            resValue("string",  Strings.FRONTEND_HOST, Constants.RELEASE_FRONTEND_HOST)
            if (LOCAL_KEY_PRESENT || TRAVIS_BUILD)
                signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug") {
            buildConfigField("String", Strings.DEFAULT_BASE_URL, Constants.DEBUG_DEFAULT_BASE_URL)
            buildConfigField("String", Strings.MAPBOX_KEY, Constants.MAPBOX_KEY)
            buildConfigField("String", Strings.STRIPE_API_KEY, Constants.STRIPE_API_TOKEN)
            buildConfigField("String", Strings.PAYPAL_CLIENT_ID, Constants.PAYPAL_CLIENT_ID)
            resValue("string", Strings.FRONTEND_HOST, Constants.DEBUG_FRONTEND_HOST)
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
        if (this is KotlinJvmOptions) {
            this.jvmTarget = "1.8"
        }
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

val playStoreImplementation by configurations

dependencies {

    implementation(fileTree("dir" to "libs", "include" to listOf("*.jar")))
    implementation(Libs.multidex)
    implementation(Libs.appcompat)
    implementation(Libs.constraintlayout)
    implementation(Libs.cardview)
    implementation(Libs.recyclerview)
    implementation(Libs.material)
    implementation(Libs.browser)
    implementation(Libs.exifinterface)
    implementation(Libs.lifecycle_extensions)
    implementation(Libs.lifecycle_common_java8)
    implementation(Libs.lifecycle_reactivestreams)
    implementation(Libs.room_runtime)
    implementation(Libs.room_rxjava2)
    kapt(Libs.room_compiler)
    testImplementation(Libs.room_testing)
    implementation(Libs.preference)
    implementation(Libs.kotlin_stdlib_jdk7)

    // KTX
    implementation(Libs.core_ktx)
    implementation(Libs.fragment_ktx)
    implementation(Libs.collection_ktx)
    implementation(Libs.lifecycle_reactivestreams_ktx)

    // Koin
    implementation(Libs.koin_android)
    implementation(Libs.koin_androidx_scope)
    implementation(Libs.koin_androidx_viewmodel)

    // Location Play Service
    playStoreImplementation(Libs.play_services_location)

    //Smart Auth Play Service
    playStoreImplementation(Libs.play_services_auth)

    // Timber
    implementation(Libs.timber)
    implementation(Libs.threetenabp)

    implementation(Libs.jackson_module_kotlin)
    implementation(Libs.jsonapi_converter)
    implementation(Libs.logging_interceptor)
    implementation(Libs.retrofit)
    implementation(Libs.converter_jackson)

    // Cards Shimmer Animation
    implementation(Libs.shimmer)

    // RxJava
    implementation(Libs.rxandroid)
    implementation(Libs.rxkotlin)
    implementation(Libs.rxjava)
    implementation(Libs.adapter_rxjava2)

    // Picasso
    implementation(Libs.picasso)

    // Stripe
    implementation(Libs.stripe_android)

    // QR Code
    implementation(Libs.zxing_android_embedded)

    //Navigation
    implementation(Libs.navigation_fragment_ktx) // For Kotlin use navigation-fragment-ktx
    implementation(Libs.navigation_ui_ktx) // For Kotlin use navigation-ui-ktx

    //Anko
    implementation(Libs.anko_commons)
    implementation(Libs.anko_design)

    //Mapbox java sdk
    implementation(Libs.mapbox_sdk_services)

    // SimpleCropView
    implementation(Libs.simplecropview)

    // Stetho
    debugImplementation(Libs.stetho)
    debugImplementation(Libs.stetho_okhttp3)
    releaseImplementation(Libs.stetho_noop)
    testImplementation(Libs.stetho_noop)

    //LeakCanary
    debugImplementation(Libs.leakcanary_android)

    // Paging
    implementation(Libs.paging_runtime)
    implementation(Libs.paging_rxjava2)

    // Searchable Spinner
    implementation(Libs.searchablespinnerlibrary)

    //ExpandableTextView
    implementation(Libs.expandabletextview)

    //ExpandableView
    implementation(Libs.expandablelayout)

    //PayPal
    implementation(Libs.paypal_android_sdk)

    testImplementation(Libs.junit)
    testImplementation(Libs.threetenbp)
    testImplementation(Libs.koin_test)
    testImplementation(Libs.core_testing)
    androidTestImplementation(Libs.androidx_test_runner)
    androidTestImplementation(Libs.espresso_core)
}
