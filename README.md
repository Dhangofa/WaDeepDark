# WaDeepDark 🌙

A minimalist LSPosed module that forces a pure AMOLED Deep Dark Mode on the official WhatsApp client. No bloated features, no heavy UI injection—just true black pixels to save battery and reduce eye strain.

[![Build Status](https://github.com/Dhangofa/WaDeepDark/actions/workflows/build.yml/badge.svg)](https://github.com/Dhangofa/WaDeepDark/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## ✨ Features

* **Pure AMOLED Black:** Replaces WhatsApp's default greenish-dark background (`#111b21`) with true pitch black (`#000000`).
* **Optimized Contrast:** UI elements like the top app bar and chat bubbles are mapped to dark gray (`#0A0A0A`), preserving visual depth without blinding you.
* **Update-Proof:** Instead of parsing highly obfuscated WhatsApp UI classes, this module intercepts Android's native `Resources.getColor()` requests. It survives almost all WhatsApp updates without needing a module update.
* **Anti-Ban Safe:** Because it hooks the Android OS resource framework rather than modifying WhatsApp's internal security or network logic, it operates entirely under the radar. You are using the untouched, official WhatsApp client.
* **Zero Battery Drain:** Extremely lightweight with no background services.


| Default WhatsApp Dark | WaDeepDark AMOLED |
|:---:|:---:|
| <img src="link_to_image_1" width="250"/> | <img src="link_to_image_2" width="250"/> |

## ⚙️ Requirements

1. **Rooted Device** (Magisk, KernelSU, or APatch)
2. **LSPosed Framework** installed and active
3. Official **WhatsApp** (Targeting Android 9.0+)

## 🚀 Installation

1. Go to the [Releases](https://github.com/Dhangofa/WaDeepDark/releases) page and download the latest `WaDeepDark.apk`.
2. Install the APK on your device.
3. Open the **LSPosed Manager** app.
4. Go to Modules -> Enable **WaDeepDark**.
5. Ensure **WhatsApp** (`com.whatsapp`) is checked in the module scope.
6. **Force Stop** WhatsApp from your Android App Settings, then reopen it.

## 🛠️ How it Works (For Developers)

Heavy WhatsApp mods often inject CSS or hook specific `View` classes, which breaks every time Meta updates the app or changes their obfuscation dictionaries. 

WaDeepDark takes a lighter, system-level approach. It hooks into `android.content.res.Resources`, monitoring the exact hex codes WhatsApp requests to draw its interface. When WhatsApp asks the OS to render its signature green-gray tint, the module intercepts the request and hands back `#000000` instead. The text colors (which are already light gray/white) are left untouched, naturally increasing the contrast ratio against the new black background.

## 🏗️ Building from Source

This project uses GitHub Actions for automated building. Every push to the `main` or `dev` branch compiles the APK automatically.

If you want to build it locally:
1. Clone the repository: `git clone https://github.com/Dhangofa/WaDeepDark.git`
2. Open the project folder in terminal or Android Studio.
3. Run the Gradle build command:
   
```bash
   ./gradlew assembleRelease