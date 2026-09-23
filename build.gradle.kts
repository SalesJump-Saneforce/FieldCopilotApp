// Self-contained AI chatbot (Field Copilot) module.
// Drop this folder into any project, add `include(":fieldcopilot")` in settings.gradle
// and `implementation(project(":fieldcopilot"))` in the app module.
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.saneforce.fieldcopilot"
    compileSdk = 36
    resourcePrefix = "fieldcopilot_"

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
}
