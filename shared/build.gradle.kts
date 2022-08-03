plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization")
    id("com.squareup.sqldelight")
}

version = "1.0"

val coroutinesVersion = "1.5.0-native-mt"
val serializationVersion = "1.2.2"
val ktorVersion = "1.6.1"
val sqlDelightVersion = "1.5.3"
val kodeinDbVersion = "0.9.0-beta"
val koinVersion = "3.1.2"

kotlin {
    android()

    listOf(
        iosX64(),
        iosArm64(),
        //iosSimulatorArm64() sure all ios dependencies support this target
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-logging:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion")
                implementation("io.ktor:ktor-client-serialization:$ktorVersion")


                implementation("io.insert-koin:koin-core:${koinVersion}")

                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
                implementation("com.squareup.sqldelight:runtime:$sqlDelightVersion")
                implementation("com.squareup.sqldelight:coroutines-extensions:$sqlDelightVersion")
                implementation("org.kodein.db:kodein-db:$kodeinDbVersion")
                implementation("org.kodein.db:kodein-db-serializer-kotlinx:$kodeinDbVersion")
            }
        }
        val androidMain by getting {
            dependencies {
//                implementation("org.kodein.db:kodein-db-jvm:${kodeinDbVersion}")
                implementation("com.squareup.sqldelight:android-driver:$sqlDelightVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
                implementation("io.ktor:ktor-client-android:$ktorVersion")
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
//        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependencies {
                implementation("io.ktor:ktor-client-ios:$ktorVersion")
                implementation("com.squareup.sqldelight:native-driver:$sqlDelightVersion")
            }
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
//            iosSimulatorArm64Main.dependsOn(this)
        }
    }
}


sqldelight {
    database("AppDatabase") {
        packageName = "ru.mephi.shared"
        sourceFolders = listOf("sqldelight")
        schemaOutputDirectory = file("src/commonMain/sqldelight")
        deriveSchemaFromMigrations = true
        migrationOutputFileFormat = ".sqm"
        verifyMigrations = true
    }
}

android {
    compileSdkVersion(32)
    buildToolsVersion("30.0.3")
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(32)
    }
}