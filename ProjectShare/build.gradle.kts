import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.serialization") version "1.7.20"
}

group = "com.walfud.cc.projectshare"
version = "1.0.0"

val serializationVersion: String by project
val zxingVersion: String by project
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:$serializationVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$serializationVersion")
    implementation("com.google.zxing:zxing-parent:$zxingVersion")
    implementation("com.google.zxing:core:$zxingVersion")
    implementation(project(":common"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}