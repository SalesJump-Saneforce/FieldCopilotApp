// swift-tools-version:5.5
import PackageDescription

let package = Package(
    name: "FieldCopilot",
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(name: "FieldCopilot", targets: ["FieldCopilot"])
    ],
    targets: [
        .target(name: "FieldCopilot")
    ]
)