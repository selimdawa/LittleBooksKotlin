pluginManagement {
    repositories {
        //jcenter()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        //jcenter()
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://maven.aliyun.com/repository/jcenter")
    }
}

rootProject.name = "Little Books"
include(":app")