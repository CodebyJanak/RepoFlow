pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "RepoFlow"

include(":app")
include(":core")
include(":feature-home")
include(":feature-repositories")
include(":feature-workspace")
include(":feature-activity")
include(":feature-settings")
