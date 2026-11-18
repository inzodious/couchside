// couchside: runs an Android TV app's own source on the desktop. `runtime` is the simulated TV (Android's framework
// rebuilt on Compose for Desktop and Skia); `app` compiles the target project (-Ptarget=<dir>) against it.
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
    }
}
rootProject.name = "couchside"
include(":runtime", ":app")
