# Field Copilot module

Self-contained Android library that shows the SalesJump AI chatbot
(`FieldCopilotApp.aspx`) in a floating WebView chat window.

## Adding to a project

### Option 1 — copy the module folder (recommended)

1. Copy this `fieldcopilot` folder into the project root.
2. In `settings.gradle(.kts)`:
   ```kotlin
   include(":fieldcopilot")
   ```
3. In the app module's `build.gradle(.kts)`:
   ```kotlin
   implementation(project(":fieldcopilot"))
   ```
4. The chatbot URL is plain `http`, so the app manifest needs
   `android:usesCleartextTraffic="true"` (or a network security config
   allowing `sjui.salesjump.in`).

### Option 2 — ship a prebuilt AAR (no source)

Build once in this project:

```
gradlew :fieldcopilot:assembleRelease
```

Take `fieldcopilot/build/outputs/aar/fieldcopilot-release.aar`, drop it into
the other app's `app/libs/` folder and add:

```kotlin
implementation(files("libs/fieldcopilot-release.aar"))
implementation("com.google.android.material:material:1.12.0")
```

(The Material dependency is needed because AAR files do not carry their own
transitive dependencies.)

## Usage

Option A — your own FAB in XML (`@drawable/fieldcopilot_ic_bot` and
`@color/fieldcopilot_accent` are exported for it), then:

```kotlin
binding.fabFieldCopilot.setOnClickListener {
    FieldCopilot.show(
        childFragmentManager, // or supportFragmentManager in an Activity
        FieldCopilotConfig(
            sfCode = "...",
            divCode = "...",
            sfName = "...",
            sfType = "1",
            hoId = "...",
            designation = "...",
        )
    )
}
```

Option B — zero layout changes; inject a ready-made FAB into any container:

```kotlin
FieldCopilot.addFabTo(binding.root, supportFragmentManager) {
    FieldCopilotConfig(sfCode = "...", divCode = "...", sfName = "...")
}
```

`FieldCopilotConfig.baseUrl` can be overridden per project if the chatbot
is hosted elsewhere; all six query parameters (`sf_code`, `div_code`,
`sf_name`, `sf_type`, `ho_id`, `designation`) are URL-encoded automatically.
