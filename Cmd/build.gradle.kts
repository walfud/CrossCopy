import java.util.*

plugins {
    application
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.serialization") version "1.7.20"
    id("com.github.breadmoirai.github-release") version "2.4.1"
}

group = "com.walfud.cc.cmd"
version = "1.0.1"

val cliVersion: String by project
val serializationVersion: String by project
val ktorVersion: String by project
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-cli:$cliVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:$serializationVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$serializationVersion")
    implementation(project(":ClientShare"))
    implementation(project(":ProjectShare"))
    implementation(project(":common"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Jar>() {
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
}

application {
    applicationName = "cc"
    mainClass.set("MainKt")
}

/**
 * local.properties
 * GITHUB_TOKEN=xxxx
 */
val localPropertiesFile = file("local.properties")
if (localPropertiesFile.exists()) {
    val properties = Properties().apply {
        localPropertiesFile.inputStream().use {
            load(it)
        }
    }


    // https://github.com/BreadMoirai/github-release-gradle-plugin
    githubRelease {
        token(properties["GITHUB_TOKEN"] as String) // This is your personal access token with Repo permissions
        owner("walfud")                             // default is the last part of your group. Eg group: "com.github.breadmoirai" => owner: "breadmoirai"
        repo(rootProject.name)                      // by default this is set to your project name
        overwrite(true)                             // by default false; if set to true, will delete an existing release with the same tag and name
        releaseAssets(tasks.getByName("distZip").outputs.files) // this points to which files you want to upload as assets with your release, by default this is empty
        body("")                                    // by default this is empty
    }
    tasks.getByName("githubRelease").dependsOn("distZip")
}
