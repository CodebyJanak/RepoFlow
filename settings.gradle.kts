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
include(":feature-repository-detail")
include(":feature-git-status")
include(":feature-commit")
include(":feature-diff-viewer")
include(":feature-issues")
include(":feature-pull-requests")
include(":feature-actions")
include(":feature-pc-bridge")
