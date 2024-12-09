pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral() // Maven Central 저장소
//        maven { url = uri("https://api.mapbox.com/downloads/v2/releases/maven") } // Mapbox 저장소
//        maven { url = uri("https://jitpack.io")} // Kakao SDK를 위해 필요할 수 있음
        gradlePluginPortal()
        maven { url = uri("https://repository.map.naver.com/archive/maven") }
//        maven { url = uri("https://devrepo.kakao.com/nexus/repository/kakaomap-releases/") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://repository.map.naver.com/archive/maven") }
//        maven { url = uri("https://devrepo.kakao.com/nexus/repository/kakaomap-releases/") }
    }
}

rootProject.name = "My Application"
include(":app")
