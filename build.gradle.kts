// Self-contained AI chatbot (Field Copilot) module.
// Drop this folder into any project, add `include(":fieldcopilot")` in settings.gradle
// and `implementation(project(":fieldcopilot"))` in the app module.
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

group = "com.saneforce.fieldcopilot"
version = providers.gradleProperty("fieldcopilotVersion").getOrElse("1.0.0")

afterEvaluate {
    publishing {
        publications {
            // Publishes the Android artifact (AAR) to a Maven repository.
            // Typical use: GitHub Packages (ghp) or an internal Nexus/Artifactory.
            register<MavenPublication>("fieldcopilot") {
                from(components["release"])
                artifactId = "fieldcopilot"
            }
        }
        repositories {
            maven {
                name = "GitHubPackages"
                // Hosts the artifact in the repository named by the environment
                // variable GH_PACKAGES_REPO (defaults to this repo's name).
                url =
                    uri("https://maven.pkg.github.com/SalesJump-Saneforce/${System.getenv("GH_PACKAGES_REPO") ?: "FieldCopilotApp"}")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
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
