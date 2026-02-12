# ShardLauncher ✨

[English Version (README_en.md)](README_en.md) | [官网 (shardlauncher.cn)](https://shardlauncher.cn)

[![开发构建状态](https://github.com/LanRhyme/ShardLauncher/actions/workflows/development.yml/badge.svg?branch=master)](https://github.com/LanRhyme/ShardLauncher/actions/workflows/development.yml)
[![License](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](LICENSE)

**ShardLauncher** 是一款专为 Android 设备设计的现代化 Minecraft Java 版启动器。基于 **Jetpack Compose** 和 **Material Design 3** 构建，旨在提供极致的视觉体验和流畅的操作感受

---

## 🚀 核心特性

- **现代 UI 交互**: 全面采用 Material Design 3 设计，支持动态取色、毛玻璃模糊 (Haze)、发光动效等视觉特性
- **高性能游戏引擎**: 集成 VirGL、OSMesa、Zink 等渲染器，支持多版本 Java 运行时 (8, 17, 21)，深度优化启动性能
- **全能账户管理**: 支持微软账号 (OAuth 2.0) 和离线模式登录，安全便捷
- **极致自定义**: 
    - 自定义主题色彩（支持草碎影、蓝璃梦等多种预设）
    - 自定义背景（支持静态图片及视频背景）
    - 全局动画速度调节，侧边栏位置自定义
- **零网络依赖**: 关键运行时和渲染器库已集成在 APK 中，支持离线安装和使用

## 🛠️ 构建与运行

### 环境要求
- **Android Studio**: 推荐最新稳定版 (Ladybug+)
- **Android SDK**: API 36 (Android 15+)
- **JDK**: 11
- **NDK**: 25.2.9519653

### 快速开始
1. **克隆仓库**:
   ```bash
   git clone https://github.com/LanRhyme/ShardLauncher.git
   cd ShardLauncher
   ```
2. **配置 (可选)**: 在 `local.properties` 中添加 `MICROSOFT_CLIENT_ID` 以支持微软登录
3. **编译运行**: 在 Android Studio 中点击 **Run**，或在命令行执行：
   ```bash
   ./gradlew :ShardLauncher:installDebug
   ```

## 📂 项目结构

```text
ShardLauncher/
├── ShardLauncher/       # UI 与应用逻辑 (Jetpack Compose)
│   ├── src/main/java    # Kotlin 源代码
│   ├── src/main/assets  # JRE 运行时与内置组件
│   └── res/             # Android 资源文件
├── SL-GameCore/         # 游戏核心逻辑与 JNI 桥接
│   ├── src/main/java    # 启动器核心代码
│   └── src/main/jni     # C/C++ Native 代码 (PojavExec 等)
├── third_party/         # 第三方参考项目
└── gradle/              # 依赖版本管理 (Version Catalog)
```

## 🤝 贡献与反馈

- **问题反馈**: 请通过 [GitHub Issues](https://github.com/LanRhyme/ShardLauncher/issues) 提交 Bug 或建议
- **社区交流**: 访问 [官网 shardlauncher.cn](https://shardlauncher.cn) 获取更多资讯
- **代码贡献**: 欢迎 Fork 项目并提交 Pull Request，请遵循 [开发者文档](https://shardlauncher.cn/docs/zh/dev_convention) 中的开发约定

## 📄 许可证

本项目采用 **GPL-3.0** 许可证开源。详情请参阅 [LICENSE](LICENSE) 文件

## ⭐ Star History

[![Star History Chart](https://api.star-history.com/svg?repos=ShardLauncher/ShardLauncher&type=date&legend=top-left)](https://www.star-history.com/#ShardLauncher/ShardLauncher&type=date&legend=top-left)

---
*Powered by Kotlin & Jetpack Compose. Inspired by the Minecraft community.*
