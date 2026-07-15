# WaDeepDark 🌙
A minimalist, ultra-lightweight (~20KB) LSPosed module that forces a pure AMOLED Deep Dark Mode on the official WhatsApp client. No bloated features, no heavy UI injection—just true black pixels to save battery and reduce eye strain.
[![Build Status](https://github.com/Dhangofa/WaDeepDark/actions/workflows/build.yml/badge.svg)](https://github.com/Dhangofa/WaDeepDark/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
## ✨ Features
* **Pure AMOLED Black:** Replaces WhatsApp's default greenish-dark backgrounds (`#111b21`, `#0b141a`) with true pitch black (`#000000`).
* **Optimized Contrast:** UI elements like the top app bar, bottom nav, and chat bubbles are mapped to a premium dark gray (`#151515`), preserving visual depth without blinding you.
* **Outgoing Bubble Neutralization:** Automatically detects and darkens WhatsApp's dark teal outgoing chat bubbles.
* **Update-Proof Engine:** Instead of parsing highly obfuscated WhatsApp UI classes, this module intercepts Android's lowest-level rendering engine (`Canvas`, `Paint`, and `Activity` creation). It survives almost all WhatsApp updates without needing a module update.
* **Anti-Ban Safe:** Because it hooks the Android OS hardware-accelerated drawing framework rather than modifying WhatsApp's internal security or network logic, it operates entirely under the radar. You are using the untouched, official WhatsApp client.
* **Zero Battery Drain:** Built entirely in pure Java. At roughly 20KB, it has zero background services or overhead.


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
6. **Force Stop** WhatsApp from your Android App Settings, then reopen it to clear the UI cache or restart your phone.
## 🛠️ How it Works (For Developers)
Heavy WhatsApp mods often inject CSS, hook specific obfuscated `View` classes, or rely on `Resources.getColor()`. Those methods break constantly because modern WhatsApp uses Jetpack Compose, dynamic Material You theming, and heavily cached `ColorStateLists`. 
WaDeepDark takes a "God Mode", system-level approach. It hooks directly into `android.graphics.Canvas`, `android.graphics.Paint`, and `android.app.Activity`. 
Using targeted mathematical color formulas, the module monitors the exact hex codes WhatsApp attempts to push to the screen. Milliseconds before the greenish-gray pixels light up, the module intercepts the `Paint` object, preserves the original Alpha (transparency) channel, and injects pure `#000000` or `#151515` directly into the drawing pipeline.
## 🏗️ Building from Source
This project uses GitHub Actions for automated building. Every push to the `main` or `dev` branch compiles the APK automatically using a pure Java environment.
If you want to build it locally:
1. Clone the repository: `git clone https://github.com/Dhangofa/WaDeepDark.git`
2. Open the project folder in terminal or Android Studio.
3. Run the Gradle build command:
   
```bash
   gradle assembleRelease
