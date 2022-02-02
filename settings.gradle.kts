pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io" ) }
        jcenter() {
            content {
                includeModule("com.polyak", "icon-switch")
                includeModule("com.rbddevs.splashy", "splashy")
            }
        }
    }
}

rootProject.name = "VoipApp"
include(":shared")
include(":androidVoip")