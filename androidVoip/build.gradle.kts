plugins {
    id("kotlin-android")
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("androidx.navigation.safeargs")
    id("com.google.gms.google-services")
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "ru.mephi.voip"
        minSdk = 23
        targetSdk = 30 // 31 causes error with pendingIntents and abto
        versionCode = 14
        versionName = "0.7.0"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs.getByName("debug")
    }

    buildTypes {
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isDebuggable = false
        }

        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        compose = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.1.0-beta04"
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }
}

dependencies {
    implementation("androidx.preference:preference-ktx:1.2.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.6.10")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-native-mt")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime-jvm:0.3.2")

    implementation("androidx.compose.compiler:compiler:1.2.0-alpha02")
    implementation("androidx.compose.ui:ui-tooling:1.1.1")
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("androidx.compose.material:material:1.1.1")
    implementation("androidx.compose.material:material-icons-extended:1.1.1")

    implementation("io.coil-kt:coil:1.4.0")
    implementation("io.coil-kt:coil-compose:1.3.2")
    implementation("io.insert-koin:koin-android:3.1.5")
    implementation("io.insert-koin:koin-androidx-compose:3.1.5")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    implementation("androidx.navigation:navigation-fragment-ktx:2.4.1")
    implementation("androidx.navigation:navigation-ui-ktx:2.4.1")
    implementation("androidx.navigation:navigation-runtime-ktx:2.4.1")

    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.1")
    implementation("androidx.compose.runtime:runtime-livedata:1.2.0-alpha02")

    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("androidx.preference:preference-ktx:1.2.0")

    implementation("io.ktor:ktor-client-okhttp:1.6.2")
    implementation("io.ktor:ktor-client-serialization:1.6.7")
    implementation("io.ktor:ktor-client-core:1.6.8")
    implementation("io.ktor:ktor-client-json:1.6.7")

    implementation(platform("com.google.firebase:firebase-bom:29.0.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")

    implementation("org.greenrobot:eventbus:3.3.1")

    implementation("com.google.accompanist:accompanist-systemuicontroller:0.17.0")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.0")

    implementation("com.squareup.sqldelight:runtime:1.5.3")
    implementation("com.squareup.sqldelight:coroutines-extensions:1.5.3")

    implementation("com.vmadalin:easypermissions-ktx:1.0.0")
    implementation("com.polyak:icon-switch:1.0.0")
    implementation("com.github.leonardoxh:keystore-ultimate:1.3.0")
    implementation("com.github.xabaras:RecyclerViewSwipeDecorator:1.3")

    implementation(project(":shared"))
    implementation(files("aars/abto_android_voip_sdk.aar"))
}