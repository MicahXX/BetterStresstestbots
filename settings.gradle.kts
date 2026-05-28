rootProject.name = "BetterStresstestbots"

include("core", "v1_21", "v1_21_11", "v26")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}
