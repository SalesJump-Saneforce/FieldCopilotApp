# Field Copilot module

Self-contained module that shows the SalesJump AI chatbot
(`FieldCopilotApp.aspx`) in a floating chat window.

- **Android**: Kotlin library that renders the chat in a `WebView`
  (`Activity`) — on both phones above the soft keyboard.
- **iOS**: native `WKWebView` wrapper (see `ios/`).

---

## Android — adding to a project

### Option 1 — consume the published artifact from GitHub Packages (recommended)

This removes the need to keep the local source folder in every app. Once the
artifact is published (see **Publishing**), add the private repo and the
dependency:

**Root `settings.gradle.kts`:**
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/SalesJump-Saneforce/FieldCopilotApp")
            // The repo and package are now PUBLIC, so no credentials are
            // needed. Only add the credentials block below if the host app
            // must also resolve OTHER private GitHub Packages.
            // credentials {
            //     username = providers.gradleProperty("GITHUB_USER").orNull
            //     password = providers.gradleProperty("GITHUB_TOKEN").orNull
            // }
        }
    }
}
```

**App module `build.gradle.kts`:**
```kotlin
dependencies {
    implementation("com.saneforce.fieldcopilot:fieldcopilot:1.0.0")
}
```

Set the version via `-PfieldcopilotVersion=...` (defaults to `1.0.0`).

> **Alternative (public repo only): JitPack.** Because the repo is public,
> JitPack needs no configuration. Tag a release, then:
> ```kotlin
> // settings.gradle.kts
> maven { url = uri("https://jitpack.io") }
> // app build.gradle.kts
> implementation("com.github.SalesJump-Saneforce:FieldCopilotApp:<version>")
> ```
> JitPack builds the module from the tagged source, so no `publish` step is needed.

### Option 2 — copy the module folder (for development)

1. Copy this folder into the project root.
2. In `settings.gradle(.kts)`: `include(":fieldcopilot")`
3. In the app module's `build.gradle(.kts)`:
   ```kotlin
   implementation(project(":fieldcopilot"))
   ```

### Option 3 — ship a prebuilt AAR (no source)

Build once: `./gradlew :fieldcopilot:assembleRelease`, then drop
`outputs/aar/fieldcopilot-release.aar` into `app/libs/` and add:
```kotlin
implementation(files("libs/fieldcopilot-release.aar"))
implementation("com.google.android.material:material:1.12.0")
```

> The chatbot URL is plain `http`, so Android apps need
> `android:usesCleartextTraffic="true"` (or a network security config
> allowing `sjui.salesjump.in`).

## Publishing to GitHub Packages (Android)

GitHub Packages only accepts artifacts in a repo already created on GitHub —
here that is `SalesJump-Saneforce/FieldCopilotApp`. Publish with a token that
has `write:packages`:

```powershell
$env:GITHUB_ACTOR="<your-github-username>"
$env:GITHUB_TOKEN="<PAT-with-write:packages>"
$env:GH_PACKAGES_REPO="FieldCopilotApp"
./gradlew publish
```

The Maven coordinates are `com.saneforce.fieldcopilot:fieldcopilot:<version>`.

> For a fully automated pipeline, add a GitHub Actions workflow that runs this
> on a tag push (see `ios/` notes for running the iOS pod in the same job on
> a macOS runner).

## Usage

You own FAB in XML, then:
```kotlin
binding.fabFieldCopilot.setOnClickListener {
    FieldCopilot.show(
        requireActivity(),
        FieldCopilotConfig(sfCode="...", divCode="...", sfName="...")
    )
}
```

Or inject a ready-made FAB with zero layout changes:
```kotlin
FieldCopilot.addFabTo(binding.root, this) {
    FieldCopilotConfig(sfCode="...", divCode="...", sfName="...")
}
```

`FieldCopilotConfig.baseUrl` can be overridden per project; all six query
parameters (`sf_code`, `div_code`, `sf_name`, `sf_type`, `ho_id`,
`designation`) are URL-encoded automatically.

## Keyboard safety

The dialog window uses `SOFT_INPUT_ADJUST_RESIZE`, so opening the keyboard
shrinks the WebView and the input box stays visible instead of being hidden.

---

## iOS (`ios/`)

A native `WKWebView` wrapper mirroring the Android API. Open the `.xcodeproj`
on a Mac, build, and distribute the `FieldCopilot` framework/framework binary
through your own private CocoaPods repo or Swift Package (also backed by this
GitHub Packages organization). See `ios/README.md`.
