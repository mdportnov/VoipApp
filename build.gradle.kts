buildscript {
    repositories {
        jcenter()
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io" ) }
    }
    val kotlinVersion = "1.6.0"
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.4.0")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.8.1")
        classpath("com.squareup.sqldelight:gradle-plugin:1.5.3")
    }
    allprojects {
        configurations.all {
            resolutionStrategy {
                force("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0-native-mt")
            }
        }
    }
}

allprojects {
    repositories {
        jcenter()
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io" ) }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}