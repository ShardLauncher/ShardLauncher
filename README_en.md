# ShardLauncher ✨

[中文版本 (README.md)](README.md) | [Official Website (shardlauncher.cn)](https://shardlauncher.cn)

[![Development Build Status](https://github.com/LanRhyme/ShardLauncher/actions/workflows/development.yml/badge.svg?branch=master)](https://github.com/LanRhyme/ShardLauncher/actions/workflows/development.yml)
[![License](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](LICENSE)

**ShardLauncher** is a modern Minecraft Java Edition launcher designed specifically for Android devices. Built with **Jetpack Compose** and **Material Design 3**, it aims to provide a premium visual experience and fluid user interaction.

---

## 🚀 Core Features

- **Modern UI Interaction**: Full Material Design 3 implementation, featuring dynamic color extraction, glassmorphism (Haze), and glow effects.
- **High-Performance Game Engine**: Integrated renderers including VirGL, OSMesa, and Zink. Supports multiple Java Runtimes (8, 17, 21) with deeply optimized startup performance.
- **Comprehensive Account Management**: Secure and convenient login via Microsoft Account (OAuth 2.0) or Offline mode.
- **Extreme Customization**: 
    - Custom theme colors with multiple presets.
    - Customizable backgrounds (supporting both static images and video backgrounds).
    - Global animation speed adjustment and sidebar position customization.
- **Zero Network Dependency**: Critical runtimes and renderer libraries are pre-integrated within the APK, supporting offline installation and usage.

## 🛠️ Build and Run

### Requirements
- **Android Studio**: Latest stable version recommended (Ladybug+)
- **Android SDK**: API 36 (Android 15+)
- **JDK**: 11
- **NDK**: 25.2.9519653

### Quick Start
1. **Clone the repository**:
   ```bash
   git clone https://github.com/LanRhyme/ShardLauncher.git
   cd ShardLauncher
   ```
2. **Configuration (Optional)**: Add `MICROSOFT_CLIENT_ID` to `local.properties` for Microsoft login support.
3. **Build and Run**: Click **Run** in Android Studio or execute via command line:
   ```bash
   ./gradlew :ShardLauncher:installDebug
   ```

## 📂 Project Structure

```text
ShardLauncher/
├── ShardLauncher/       # UI & Application Logic (Jetpack Compose)
│   ├── src/main/java    # Kotlin Source Code
│   ├── src/main/assets  # JRE Runtimes & Built-in Components
│   └── res/             # Android Resources
├── SL-GameCore/         # Game Core Logic & JNI Bridge
│   ├── src/main/java    # Launcher Core Code
│   └── src/main/jni     # C/C++ Native Code (PojavExec, etc.)
├── third_party/         # Third-party Reference Projects
└── gradle/              # Dependency Management (Version Catalog)
```

## 🤝 Contribution and Feedback

- **Feedback**: Please submit bugs or suggestions via [GitHub Issues](https://github.com/LanRhyme/ShardLauncher/issues).
- **Community**: Visit the [Official Website shardlauncher.cn](https://shardlauncher.cn) for more information.
- **Contribution**: Fork the project and submit a Pull Request. Please follow the development conventions in [Developer Documentation](https://shardlauncher.cn/docs/zh/dev_convention).

## 📄 License

This project is open-sourced under the **GPL-3.0** License. See the [LICENSE](LICENSE) file for details.

## ⭐ Star History

[![Star History Chart](https://api.star-history.com/svg?repos=ShardLauncher/ShardLauncher&type=date&legend=top-left)](https://www.star-history.com/#ShardLauncher/ShardLauncher&type=date&legend=top-left)

---
*Powered by Kotlin & Jetpack Compose. Inspired by the Minecraft community.*
