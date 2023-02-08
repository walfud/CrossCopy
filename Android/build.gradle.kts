import java.util.*

plugins {
    id("com.android.application").version("7.2.0").apply(true)
    id("org.jetbrains.kotlin.android").version("1.7.10").apply(true)
}

val composeVersion: String by project
android {
    compileSdk = 33

    defaultConfig {
        applicationId = "com.walfud.cc.android"
        minSdk = 30
        targetSdk = 33
        versionCode = 1
        versionName = "1.0.2"
    }

    signingConfigs {
        val localPropertiesFile = file("local.properties")
        if (localPropertiesFile.exists()) {
            create("release") {
                val properties = Properties().apply {
                    localPropertiesFile.inputStream().use {
                        load(it)
                    }
                }

                storeFile = file("${projectDir}/keystore.jks")
                storePassword = properties["KEYSTORE_PASSWORD"] as String
                keyAlias = "cc"
                keyPassword = properties["KEYSTORE_PASSWORD"] as String
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            val localPropertiesFile = file("local.properties")
            if (localPropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeVersion
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

val material3Version: String by project
dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.compose.material3:material3:$material3Version")
    implementation("androidx.compose.animation:animation:$composeVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.27.0")
    implementation("com.github.getActivity:XXPermissions:16.2")

    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$composeVersion")

    implementation(project(":ClientShare"))
    implementation(project(":ProjectShare"))
    implementation(project(":common"))
}
