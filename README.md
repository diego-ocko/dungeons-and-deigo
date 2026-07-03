# Dungeons And Deigo

A Kotlin Multiplatform app targeting Android, iOS, and Web.

## Deployment status

| Platform | Status | Link |
|----------|--------|------|
| Web | [![Netlify Status](https://api.netlify.com/api/v1/badges/e4c1d49d-4dff-4ae4-b3f1-0d986aec299b/deploy-status)](https://app.netlify.com/projects/dungeons-and-deigo/deploys) |  https://dungeons-and-deigo.netlify.app/ |
| Android | Comming Soon | - |
| iOS | Comming Soon | - |

## Project Structure

| Module | Purpose | Target |
|--------|---------|--------|
| `shared/` | Common code shared across all platforms | Android, iOS, Web |
| `androidApp/` | Android application entry point | Android |
| `iosApp/` | iOS SwiftUI app | iOS |
| `webApp/` | Kotlin/Wasm browser app | Web |

## Running the App

### Web (IntelliJ)

Via Gradle tool window: `webApp` → Tasks → `kotlin browser` → `wasmJsBrowserDevelopmentRun`

Or via terminal:

```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

Opens a browser at `localhost:8080`.

### Android

Use **Android Studio**:
1. Open the project
2. Select the `androidApp` run configuration
3. Pick an emulator/device and hit Run

### iOS

Option A: Install the [Kotlin Multiplatform Mobile plugin](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform) in IntelliJ/Android Studio for iOS simulator run configurations.

Option B: Build the shared framework and run from Xcode:

```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

Then open `iosApp/` in Xcode and run.

## Running Tests

Add tests in `shared/src/commonTest/`, then run:

```bash
./gradlew :shared:allTests
```

Or via Gradle tool window: `shared` → Tasks → `verification` → `allTests`
