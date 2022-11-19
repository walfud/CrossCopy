pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "CrossCopy"

include(":Android")
include(":Cmd")
include(":Server")
include(":ClientShare")
include(":ProjectShare")
include(":common")
