# Field Copilot — iOS

Native `WKWebView` wrapper that shows the SalesJump AI chatbot
(`FieldCopilotApp.aspx`) in a floating chat window. It mirrors the Android
module's public API (`FieldCopilot` + `FieldCopilotConfig`).

## Building

Requires **macOS** with Xcode + Swift toolchain. From this folder:

```bash
# Swift Package
swift build -c release

# Or as a framework
xcodebuild -scheme FieldCopilot -destination 'generic/platform=iOS Simulator' build
```

## Keyboard safety

`FieldCopilotViewController` observes keyboard frame changes and raises the
web view's bottom scroll inset, so the focused chat input stays visible above
the on-screen keyboard (the web page also scrolls to the focused element).

## Usage in a host app

```swift
import FieldCopilot

FieldCopilot.present(from: self, config: FieldCopilotConfig(
    sfCode: "...",
    divCode: "...",
    sfName: "...",
    hoId: "...",
    designation: "..."
))
```

`FieldCopilotConfig.buildURL` encodes all six parameters (`sf_code`, `div_code`,
`sf_name`, `sf_type`, `ho_id`, `designation`) and supports a custom `baseUrl`.

> The chatbot page is plain `http`, so iOS requires an
> [App Transport Security](https://developer.apple.com/documentation/bundleresources/information_property_list/nsapptransportsecurity)
> exception (e.g. `NSAllowsArbitraryLoads` in dev) or a domain allow-list for
> `sjui.salesjump.in`.

## Distribution from the private GitHub repo

Two supported routes (both backed by the `SalesJump-Saneforce` GitHub org so
they respect its private-repo access):

1. **Private CocoaPods repo** — commit `FieldCopilot.podspec` here, then in the
   host app's `Podfile`:
   ```ruby
   source 'https://github.com/SalesJump-Saneforce/iOS-Specs.git'
   pod 'FieldCopilot'
   ```
2. **Binary XCFramework in GitHub Releases** — build `FieldCopilot.xcframework`
   and attach it to a Release; host apps download it with your own script or a
   Swift Package `binaryTarget(url:checkout)`.

Build the podspec on a GitHub Actions **macOS** runner in the same repo for a
fully automated CI release.
