# SafeOrbit — Family Safety & Location Sharing

[![SafeOrbit Android CI](https://github.com/OWNER/REPOSITORY/actions/workflows/android.yml/badge.svg)](https://github.com/OWNER/REPOSITORY/actions/workflows/android.yml)
**Version:** 1.0.0 (Stage 1 Architecture)  
**Developer Studio:** OG KEN Studio  
**Creator Credit:** Tanmay  
**Package ID:** `com.ogken.safeorbit`  
**Tagline:** *Stay connected. Share only when you choose.*  

---

## 🛡️ Project Overview

**SafeOrbit** is a modern, native Android family safety and consent-based live location-sharing application built with **Jetpack Compose** and **Material 3**. 

Unlike conventional family trackers that rely on continuous covert surveillance, SafeOrbit is founded upon a **strict consent-first architecture**:
- **Location sharing is OFF by default.**
- Coordinates are never broadcast without active, deliberate permission from the user.
- Zero battery-draining or unconsented background tracking.

---

## 🎨 Brand Identity & Design System

SafeOrbit features a bespoke visual identity tailored for modern Android:
- **Navy (`#102D3A`)**: Foundation color representing strength, privacy, and shelter.
- **Deep Background (`#091820`)**: High-contrast OLED dark background for battery savings and clarity.
- **Teal (`#42D9B0`)**: Primary accent representing active security and signal clarity.
- **Mint (`#72E9C4`)**: Secondary accent for highlights, orbital paths, and badges.
- **White (`#FFFFFF`)**: Primary typography and high-priority indicators.
- **Secondary Text (`#9BB3BA`)**: Subtle metadata, captions, and secondary guidance.

### Bespoke Branding
- **SafeOrbit Shield & Orbital Pin Logo:** An original geometric emblem pairing an outer orbital defense ring with a center location pin. (Completely distinct from Life360 or generic map icons).
- **Adaptive Launcher Icons:** Full support for Android 15 adaptive round and square icon masks (`ic_launcher` and `ic_launcher_round`).
- **Splash Screen:** Seamless Android 12+ system splash transition into the Compose experience.

---

## ⚙️ Selected Technology & Build Matrix

To support development on resource-constrained hardware (e.g. Intel Core i5-4590S, 8 GB RAM, 128 GB SSD), SafeOrbit is architected for **zero local emulator overhead**. The codebase compiles automatically in the cloud through **GitHub Actions**.

| Component | Selected Version | Note |
| :--- | :--- | :--- |
| **Android Gradle Plugin (AGP)** | `8.7.2` | Current stable Google Gradle plugin |
| **Gradle** | `8.10.2` | Official Gradle wrapper included |
| **Kotlin** | `2.0.21` | Modern compiler with built-in Compose Compiler plugin |
| **Compose BOM** | `2024.10.01` | Bill of Materials for synchronized UI dependencies |
| **Material 3** | `1.3.1` | Next-gen Material Design design tokens |
| **Minimum Android SDK** | `35` (Android 15) | Configured strictly per Stage 1 requirements |
| **Target SDK** | `35` (Android 15) | Compliant with Google Play submission standards |
| **Compile SDK** | `35` (Android 15) | Android 15 stable APIs |
| **Java / JDK** | `Java 17 (Temurin)` | Recommended enterprise LTS for AGP 8.7+ |

---

## 📂 Project Architecture

SafeOrbit adheres to **Clean Architecture** and the **MVVM (Model-View-ViewModel)** design pattern:

```text
LOCATION TRACKER/
├── .github/
│   └── workflows/
│       └── android.yml                # Automated GitHub Actions Cloud CI/CD build
├── app/
│   ├── build.gradle.kts               # App module build script (Kotlin DSL)
│   ├── google-services.json           # Preserved locally (excluded from Git in Stage 1)
│   ├── proguard-rules.pro             # ProGuard optimization and obfuscation rules
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml    # App manifest (Zero GPS permissions in Stage 1)
│       │   ├── java/com/ogken/safeorbit/
│       │   │   ├── MainActivity.kt    # Single activity entry point
│       │   │   ├── domain/            # Business logic and use cases
│       │   │   │   └── GetOrbitCircleUseCase.kt
│       │   │   ├── model/             # Core immutable data models
│       │   │   │   ├── CircleMember.kt
│       │   │   │   ├── LocationSharingState.kt
│       │   │   │   └── User.kt
│       │   │   ├── navigation/        # Jetpack Compose Navigation
│       │   │   │   ├── SafeOrbitNavGraph.kt
│       │   │   │   └── Screen.kt
│       │   │   ├── repository/        # Data repositories (In-Memory for Stage 1)
│       │   │   │   ├── FamilyCircleRepository.kt
│       │   │   │   └── LocationSharingRepository.kt
│       │   │   ├── theme/             # Material 3 theme & color tokens
│       │   │   │   ├── Color.kt
│       │   │   │   ├── Theme.kt
│       │   │   │   └── Type.kt
│       │   │   └── ui/                # UI screens and modular components
│       │   │       ├── components/
│       │   │       │   ├── RadarMapPlaceholder.kt
│       │   │       │   ├── SafeOrbitLogo.kt
│       │   │       │   └── StatusBadge.kt
│       │   │       ├── home/
│       │   │       │   ├── HomeScreen.kt
│       │   │       │   ├── HomeViewModel.kt
│       │   │       │   └── SettingsDialog.kt
│       │   │       └── welcome/
│       │   │           └── WelcomeScreen.kt
│       │   └── res/                   # Drawables, adaptive icons, colors, themes, strings
│       └── test/                      # Unit test suites verifying Stage 1 guarantees
│           └── java/com/ogken/safeorbit/FamilyCircleRepositoryTest.kt
├── gradle/
│   ├── libs.versions.toml             # Gradle Version Catalog
│   └── wrapper/
│       ├── gradle-wrapper.jar         # Verified Gradle wrapper binary
│       └── gradle-wrapper.properties  # Gradle 8.10.2 configuration
├── .gitignore                         # Android & IDE ignore rules
├── build.gradle.kts                   # Root build script
├── gradlew                            # Linux / macOS Gradle wrapper script
├── gradlew.bat                        # Windows Gradle wrapper script
├── README.md                          # Project documentation
└── settings.gradle.kts                # Repository and module declarations
```

---

## 🚀 How to Build and Download Your APK (Cloud Runners)

Because your computer has limited RAM (8 GB) and an older processor, you **do not** need to install heavy Android Studio emulators or run intensive builds locally. GitHub Actions builds your APK for free in Google/Microsoft cloud data centers!

### Step 1: Install Git (if not already installed)
If Git is not installed on your PC:
1. Download Git from [git-scm.com](https://git-scm.com/download/win).
2. Install it with default recommended settings.

### Step 2: Initialize Git and Commit
Open PowerShell in this project folder (`LOCATION TRACKER`) and run:
```powershell
# 1. Initialize local git repository
git init

# 2. Add all files
git add .

# 3. Create your initial commit
git commit -m "feat: SafeOrbit Stage 1 Android architecture and Compose UI"
```

### Step 3: Push to GitHub
1. Go to [github.com](https://github.com) and sign in.
2. Click **New Repository**.
3. Name it `SafeOrbit` (or any repository name you prefer), set it to **Public** or **Private**, and leave "Add README" unchecked.
4. Link and push your project:
```powershell
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/SafeOrbit.git
git push -u origin main
```

### Step 4: Download Your Built APK
1. As soon as you push, open your repository on GitHub in your web browser.
2. Click the **Actions** tab at the top.
3. You will see the **SafeOrbit Android CI - Cloud Build** workflow running.
4. In approximately 2 to 4 minutes, the workflow will turn green (Completed).
5. Click on the completed run and scroll to the bottom under **Artifacts**.
6. Download **SafeOrbit-v1.0.0-debug-apk.zip**!
7. Extract the ZIP to get your `app-debug.apk`, which you can directly install on your Android device (running Android 15 / API 35).

---

## 🔒 Stage 1 Restrictions & Guarantees

In accordance with Stage 1 requirements:
- ❌ **No Firebase SDK:** `google-services.json` is preserved without plugin activation.
- ❌ **No Google Maps API:** Visual radar placeholder UI created instead.
- ❌ **No GPS / Background Permissions:** `ACCESS_FINE_LOCATION` is purposefully omitted from `AndroidManifest.xml`.
- ❌ **No Fake Location Spoofing:** Realistic architecture with honest `OFF` status reporting.
- ✅ **100% Native Kotlin & Compose:** Built cleanly for future extension into Stage 2.

---

## 🗺️ SafeOrbit Roadmap

- [x] **Stage 1:** Project foundation, branding identity, Material 3 Jetpack Compose UI, MVVM architecture, navigation, and GitHub Actions cloud build.
- [ ] **Stage 2:** Firebase Authentication, circle invitations, encrypted cloud storage, and pairing.
- [ ] **Stage 3:** Live Google Maps integration, consent-based GPS location service, and geofencing safety alerts.
