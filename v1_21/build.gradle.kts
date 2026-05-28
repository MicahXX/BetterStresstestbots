plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    id("com.gradleup.shadow") version "9.0.0"
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
    implementation(project(":core"))
}

tasks.shadowJar {
    archiveClassifier.set("dev-all")
}

tasks.assemble {
    dependsOn(tasks.reobfJar)
}