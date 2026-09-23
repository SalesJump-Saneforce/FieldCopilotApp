// Self-contained AI chatbot (Field Copilot) module.
// - Standalone (this repo): publishes the AAR via ./gradlew publish (GitHub
//   Packages) or publishToMavenLocal (JitPack). Plugin versions are pinned here
//   so the module builds by itself.
// - Dropped into a host project: the host usually applies its own AGP/Kotlin;
//   if the versions below clash with the host's, omit the "version" on the two
//   plugin IDs and let the host supply them.
plugins {
    id("com.android.library") version "8.11.1"
    id("org.jetbrains.kotlin.android") version "2.0.21"
    id("maven-publish")
}

// JitPack overrides these with -Pgroup / -Pversion; otherwise default to the
// GitHub Packages coordinates below.
group = providers.gradleProperty("group").getOrElse("com.saneforce.fieldcopilot")
version = providers.gradleProperty("version")
    .getOrElse(providers.gradleProperty("fieldcopilotVersion").getOrElse("1.0.0"))

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
