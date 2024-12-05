pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        jcenter() // Warning: this repository is going to shut down soon
        maven { setUrl("https://maven.aliyun.com/repository/public") }
        maven { setUrl("https://maven.aliyun.com/repository/google") }
        maven { setUrl("https://jitpack.io") }
        maven { setUrl("https://repo1.maven.org/maven2/") }
        flatDir {
            dirs("app/libs")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        jcenter() // Warning: this repository is going to shut down soon
        maven { setUrl("https://maven.aliyun.com/repository/public") }
        maven { setUrl("https://maven.aliyun.com/repository/google") }
        maven { setUrl("https://jitpack.io") }
        maven { setUrl("https://repo1.maven.org/maven2/") }
        flatDir {
            dirs("app/libs")
        }
    }
}

rootProject.name = "Glasses"
include(":app")
 