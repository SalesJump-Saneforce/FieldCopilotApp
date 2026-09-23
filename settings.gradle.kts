// Standalone build so this module can be published (`./gradlew publish`)
// to GitHub Packages without being nested inside a host app.
// - When copied into a host app, the host controls this via `include(":fieldcopilot")`.
// - When built alone (this is the root project), it publishes the AAR directly.
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "fieldcopilot"