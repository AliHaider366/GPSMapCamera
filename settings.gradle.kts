pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io") // ← Add this
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven(url = "https://jitpack.io") // ← Add this

        mavenCentral()
    }
}

rootProject.name = "GPSMapCamera"
include(":app")
 