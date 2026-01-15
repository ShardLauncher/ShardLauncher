# ShardLauncher 项目文档

## 项目概述

ShardLauncher 是一个基于 Jetpack Compose 的 Minecraft Java 版 Android 启动器应用。它提供了现代化的用户界面和丰富的自定义选项，支持在 Android 设备上运行 Minecraft Java 版。

### 核心功能
- **多种登录方式**: 支持微软账号和离线登录
- **游戏管理**: 自动下载和管理 Minecraft 游戏文件，包括客户端、资源和库
- **高度可定制的 UI**: 深色模式、多种主题颜色、自定义背景、动画速度、侧边栏位置等
- **集成资源系统**: Java 运行时和渲染器库文件集成在 APK 中，支持零网络依赖
- **版本管理**: 支持多个 Minecraft 版本的管理和切换

### 技术栈
- **语言**: Kotlin
- **UI 框架**: Jetpack Compose
- **设计语言**: Material Design 3
- **构建工具**: Gradle (Kotlin DSL)
- **本地代码**: C/C++ (JNI) 用于游戏桥接和系统调用
- **数据库**: Room
- **网络**: Retrofit, OkHttp, Ktor
- **多媒体**: ExoPlayer (media3)

### 主要依赖库
- `androidx.navigation:navigation-compose` - 页面导航
- `io.coil-kt:coil-compose` - 图片加载
- `com.squareup.retrofit2:retrofit` - 网络请求
- `androidx.media3:media3-exoplayer` - 视频背景播放
- `com.bytedance:bytehook` - JNI Hook
- `dev.chrisbanes.haze:haze` - 模糊效果
- `androidx.room:room-*` - 本地数据库

## 构建与运行

### 环境要求
- Android Studio (推荐最新稳定版)
- Android SDK (API 36)
- JDK 11
- NDK 25.2.9519653

### 构建步骤

1. **克隆项目仓库**
   ```bash
   git clone https://github.com/LanRhyme/ShardLauncher.git
   cd ShardLauncher
   ```

2. **配置 Microsoft Client ID (可选)**
   - 在项目根目录创建 `local.properties` 文件
   - 添加以下内容（用于微软登录）:
     ```properties
     MICROSOFT_CLIENT_ID=your_client_id_here
     ```
   - 或者通过环境变量设置: `MICROSOFT_CLIENT_ID`

3. **在 Android Studio 中打开项目**
   - 等待 Gradle 同步完成

4. **构建项目**
   ```bash
   # Debug 构建
   ./gradlew assembleDebug

   # Release 构建
   ./gradlew assembleRelease
   ```

5. **运行应用**
   - 连接 Android 设备或启动模拟器
   - 点击 Android Studio 中的 "Run" 按钮或使用快捷键 `Shift + F10`
   - 或使用命令行:
     ```bash
     ./gradlew installDebug
     ```

### 多 ABI 构建
项目支持多架构构建，会生成以下 APK:
- `x86`
- `x86_64`
- `armeabi-v7a`
- `arm64-v8a`
- `universal` (包含所有架构)

### 清理构建
```bash
./gradlew clean
```

## 开发约定

### UI 开发
- **声明式 UI**: 使用 Jetpack Compose 进行所有 UI 开发
- **组件化**: 将可复用的 UI 组件放在 `ui/components/` 目录
- **主题系统**: 使用 Material Design 3 主题系统，支持深色模式和多种主题颜色
- **动画**: 使用 Compose 动画 API，支持动画速度自定义

### 状态管理
- 使用 Compose 的 `State` 和 `ViewModel` 管理 UI 状态
- 使用 `remember` 和 `rememberSaveable` 管理组件状态
- 使用 `LaunchedEffect` 和 `SideEffect` 处理副作用

### 导航
- 使用 `androidx.navigation:navigation-compose` 进行页面导航
- 单 Activity 架构，所有页面都是 Composable
- 导航路由定义在 `ui/navigation/Screen.kt`

### 设置存储
- 使用 `SharedPreferences` (通过 `SettingsRepository` 封装) 持久化用户设置
- 设置定义在 `settings/AllSettings.kt`
- 使用 `LocalSettingsProvider` 在 Compose 中访问设置

### 数据层
- 使用 Room 数据库进行本地数据持久化
- 数据实体定义在 `database/` 目录
- 使用 Ktor Client 进行网络请求
- 使用 Retrofit 进行 API 调用

### 本地代码 (JNI)
- JNI 代码位于 `src/main/jni/` 目录
- 使用 Android.mk 构建脚本
- 主要模块:
  - `pojavexec` - 主要执行桥接
  - `driver_helper` - 驱动助手
  - `linkerhook` - 链接器 Hook
  - `exithook` - 退出 Hook

### 资源管理
- Java 运行时集成在 `assets/runtimes/` 目录
- 使用 `AssetExtractor` 从 APK assets 提取资源
- 支持内置运行时安装和手动导入 tar.xz 文件
- 渲染器库文件集成在 `jniLibs/` 目录

### 日志记录
- 使用自定义 `Logger` 类进行日志记录
- 日志级别: `i` (信息), `d` (调试), `e` (错误)

### 代码风格
- Kotlin 代码遵循官方代码风格
- 使用 Kotlin 2.1.10 特性
- 使用 KSP (Kotlin Symbol Processing) 进行注解处理

### UI 设计风格

ShardLauncher 采用现代化的 Material Design 3 设计语言，结合丰富的视觉效果和动画，打造沉浸式的用户体验。

#### 设计原则
- **沉浸式体验**: 全屏无边框设计，隐藏系统状态栏和导航栏
- **高度可定制**: 支持用户自定义主题、颜色、背景、动画速度等
- **视觉层次**: 通过模糊、阴影、发光效果增强视觉层次感
- **流畅动画**: 所有交互都有平滑的动画过渡

#### 主题系统
基于 Material Design 3 的主题系统，支持多种预设主题和自定义主题：

**预设主题** (ThemeColor 枚举):
- **草碎影** (Green): 绿色系主题，清新自然
- **蓝璃梦** (Blue): 蓝色系主题，科技感强
- **紫晶泪** (Purple): 紫色系主题，优雅神秘
- **黄粱残** (Golden): 金色系主题，温暖明亮
- **动态** (Dynamic): Android 12+ 动态取色，跟随系统壁纸
- **自定义** (Custom): 用户自定义主题颜色

每个主题包含完整的浅色和深色配色方案，涵盖：
- Primary/Secondary/Tertiary 颜色
- Background/Surface 颜色
- Error 颜色
- Outline/OutlineVariant 颜色
- Surface 容器颜色（Lowest/Low/High/Highest）
- Inverse 颜色

#### 视觉效果

**1. 毛玻璃/模糊效果**
- 使用 `dev.chrisbanes.haze:haze` 库实现
- 卡片背景支持模糊效果（Android 12+）
- 可调节卡片透明度
- 通过 `LocalCardLayoutConfig` 全局配置

**2. 发光效果**
- 自定义 `Modifier.glow()` 扩展函数
- 可配置发光颜色和模糊半径
- 用于按钮、图标等交互元素
- 支持交互状态变化（按下时增强发光）

**3. 背景光效**
- `BackgroundLightEffect` 组件实现动态背景光效
- 多个光点在背景中移动和呼吸
- 可调节动画速度
- 使用 Canvas 绘制，性能优化

**4. 背景支持**
- 支持静态图片背景
- 支持视频背景（使用 ExoPlayer）
- 可调节背景模糊度
- 可调节背景亮度
- 支持随机背景切换
- 支持视差效果（Parallax）

#### 形状和圆角
遵循 Material Design 3 的圆角规范：
- `extraSmall`: 22dp - 用于小元素
- `small`: 4dp - 用于标签、按钮
- `medium`: 8dp - 用于卡片
- `large`: 0dp - 用于大容器

#### 动画系统
- **可调节速度**: 全局动画速度配置，影响所有动画持续时间
- **平滑过渡**: 使用 Compose 动画 API（animateFloatAsState, animateDpAsState 等）
- **缓动函数**: 使用 FastOutSlowInEasing 等标准缓动函数
- **交互动画**: 按钮按下、卡片展开等都有动画反馈
- **背景动画**: 背景光效使用无限循环动画

#### 组件设计
- **卡片式设计**: 主要内容区域使用卡片容器
- **圆角设计**: 大量使用圆角营造柔和感
- **阴影效果**: 卡片使用 elevation 创建阴影
- **可折叠卡片**: `CollapsibleCard` 组件支持展开/收起
- **自定义按钮**: `CustomButton` 支持发光效果
- **自定义对话框**: `CustomDialog` 支持模糊背景
- **浮动操作按钮**: `FluidFab` 支持模糊渲染效果

#### 排版
- 使用 Material 3 默认排版系统
- 系统默认字体族（FontFamily.Default）
- 可扩展自定义字体（预留 TODO）

#### 颜色使用规范
- **语义化颜色**: 使用 Material 3 的语义化颜色角色
- **颜色对比**: 确保文本与背景有足够的对比度
- **主题一致性**: 所有组件使用主题颜色，避免硬编码颜色
- **状态颜色**: 正确使用 onPrimary、onSurface 等状态颜色

#### 无障碍设计
- 支持系统深色模式
- 状态栏和导航栏颜色自动适配
- 文本和背景对比度符合 WCAG 标准
- 触摸目标大小符合 Material Design 规范

#### 性能优化
- 背景光效使用 Canvas 绘制，避免过度重绘
- 模糊效果仅在 Android 12+ 启用（硬件加速支持）
- 动画使用合理的持续时间和缓动函数
- 图片使用 Coil 异步加载和缓存

### Git Commit 格式
项目遵循 Conventional Commits 规范，并使用 Emoji 来增强可读性。提交消息格式如下：

```
<type>(<scope>): <emoji><description>
```

#### 提交类型 (type)
- `feat`: 新功能
- `fix`: 修复 bug
- `style`: 代码格式、样式或 UI 优化（不影响功能）
- `refactor`: 重构代码（既不是新功能也不是修复）
- `docs`: 文档更新
- `test`: 测试相关
- `chore`: 构建、工具或依赖更新

#### 作用域 (scope)
- `game`: 游戏核心相关（启动、版本管理、渲染器等）
- `ui`: 用户界面相关
- `components`: 可复用组件
- `setting`: 设置相关
- `versions`: 版本管理
- `account`: 账户管理
- `bg`: 背景相关
- `color`: 配色相关
- `haze`: 模糊效果相关

#### Emoji 使用
- ✨️: 新功能 (feat)
- 🪲️: 修复 bug (fix)
- 📚️: 样式/优化 (style)
- ♻️: 重构 (refactor)
- 📝: 文档 (docs)
- ✅: 测试 (test)
- 🔧: 构建/工具 (chore)

#### 示例
```
feat(game): ✨️游戏核心-游戏启动流程运行
fix(bg): 🪲️修复自定义背景预览效果和实际效果不符的问题
style(ui): 📚️主页的一些ui优化
feat(components): ✨️增加ScrollIndicator滚动导航指示条可复用组件
fix(HsvColorPicker): 🪲️修復拾色器在明度/飽和度面板調整顔色時色相滑塊移動的問題
```

#### 提交建议
- 使用中文描述（简短、清晰）
- 每个提交只做一件事
- 提交消息应该清晰地说明"做了什么"和"为什么"
- 避免使用模糊的描述，如"修复bug"、"更新"等

## 项目结构

```
ShardLauncher/
├── app/                              # 主应用模块
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/lanrhyme/shardlauncher/
│   │   │   │   ├── MainActivity.kt           # 应用主入口
│   │   │   │   ├── ShardLauncherApp.kt       # Application 类
│   │   │   │   ├── api/                      # 网络 API 接口定义
│   │   │   │   ├── bridge/                   # 桥接层
│   │   │   │   ├── common/                   # 公共数据类
│   │   │   │   ├── components/               # 自定义组件
│   │   │   │   ├── coroutine/                # 协程工具
│   │   │   │   ├── data/                     # 数据层
│   │   │   │   ├── database/                 # 数据库相关
│   │   │   │   ├── game/                     # 游戏相关逻辑
│   │   │   │   │   ├── account/              # 账户管理
│   │   │   │   │   ├── multirt/              # 多运行时管理
│   │   │   │   │   ├── path/                 # 游戏路径管理
│   │   │   │   │   ├── renderer/             # 渲染器管理
│   │   │   │   │   ├── resource/             # 资源管理
│   │   │   │   │   └── version/              # 版本管理
│   │   │   │   ├── info/                     # 应用信息
│   │   │   │   ├── model/                    # 数据模型
│   │   │   │   ├── path/                     # 路径管理
│   │   │   │   ├── service/                  # 服务 (如音乐播放)
│   │   │   │   ├── setting/                  # 设置相关
│   │   │   │   ├── settings/                 # 设置定义
│   │   │   │   ├── ui/                       # UI 层
│   │   │   │   │   ├── components/           # 可复用 UI 组件
│   │   │   │   │   ├── navigation/           # 导航相关
│   │   │   │   │   ├── settings/             # 设置界面
│   │   │   │   │   ├── downloads/            # 下载界面
│   │   │   │   │   ├── account/              # 账户界面
│   │   │   │   │   ├── home/                 # 主页界面
│   │   │   │   │   ├── notification/         # 通知系统
│   │   │   │   │   ├── developeroptions/     # 开发者选项
│   │   │   │   │   ├── crash/                # 崩溃处理
│   │   │   │   │   ├── custom/               # 自定义 XAML 解析器
│   │   │   │   │   └── theme/                # 主题定义
│   │   │   │   └── utils/                    # 工具类
│   │   │   │       ├── asset/                # 资源提取工具
│   │   │   │       ├── logging/              # 日志工具
│   │   │   │       └── ...                   # 其他工具
│   │   │   ├── jni/                          # JNI/C/C++ 本地代码
│   │   │   │   ├── Android.mk                # NDK 构建脚本
│   │   │   │   ├── Application.mk            # NDK 应用配置
│   │   │   │   ├── awt_bridge.c              # AWT 桥接
│   │   │   │   ├── bigcoreaffinity.c         # 大核亲和性
│   │   │   │   ├── egl_bridge.c              # EGL 桥接
│   │   │   │   ├── exit_hook.c               # 退出 Hook
│   │   │   │   ├── input_bridge_v3.c         # 输入桥接
│   │   │   │   ├── jre_launcher.c            # JRE 启动器
│   │   │   │   ├── lwjgl_dlopen_hook.c       # LWJGL 动态加载 Hook
│   │   │   │   ├── ctxbridges/               # 上下文桥接
│   │   │   │   ├── driver_helper/            # 驱动助手
│   │   │   │   ├── environ/                  # 环境变量
│   │   │   │   ├── linkerhook/               # 链接器 Hook
│   │   │   │   └── logger/                   # 日志记录
│   │   │   ├── assets/                       # 资源文件
│   │   │   │   ├── home.xaml                 # 主页布局
│   │   │   │   ├── components/               # 组件资源 (JAR 文件)
│   │   │   │   └── runtimes/                 # 集成的 Java 运行时
│   │   │   └── res/                          # Android 资源
│   │   │       ├── drawable/                 # 图片资源
│   │   │       ├── values/                   # 值资源
│   │   │       └── xml/                      # XML 配置
│   │   ├── AndroidManifest.xml               # 应用清单
│   │   └── build.gradle.kts                  # 应用模块构建脚本
│   └── build.gradle.kts                      # 应用模块构建脚本
├── gradle/                                  # Gradle 配置
│   ├── libs.versions.toml                    # 版本目录
│   └── wrapper/                              # Gradle Wrapper
├── third_party/                             # 第三方库
│   └── ZalithLauncher2/                      # ZalithLauncher 参考项目
├── build.gradle.kts                         # 项目级构建脚本
├── settings.gradle.kts                      # 项目设置
├── gradle.properties                        # Gradle 属性
├── gradlew                                   # Gradle Wrapper (Unix)
├── gradlew.bat                               # Gradle Wrapper (Windows)
├── .gitignore                                # Git 忽略文件
├── .gitmodules                               # Git 子模块
├── README.md                                 # 项目说明 (中文)
├── README_en.md                              # 项目说明 (英文)
└── INTEGRATED_RESOURCES_IMPLEMENTATION.md    # 集成资源实现文档
```

## 关键技术点

### 游戏启动流程
1. 用户选择 Minecraft 版本
2. 检查并下载必要的游戏文件（客户端、库、资源）
3. 检查 Java 运行时是否可用
4. 初始化渲染器（VirGL、OSMesa 等）
5. 通过 JNI 启动 Java 进程
6. 桥接输入、图形和系统调用

### 资源集成系统
- Java 运行时（JRE 8, 17, 21）集成在 APK assets 中
- 使用 `AssetExtractor` 从 APK 提取资源到应用数据目录
- 支持设备架构自动检测和兼容性检查
- 支持手动导入外部 tar.xz 运行时文件

### 渲染器支持
- **VirGL**: 虚拟化 OpenGL，用于大多数设备
- **OSMesa**: 软件渲染，用于兼容性
- **Zink**: OpenGL over Vulkan（实验性）

### 输入桥接
- 触摸事件转换为鼠标/键盘事件
- 支持虚拟摇杆和按键映射
- 通过 JNI 传递到 Java 进程

### 账户系统
- 微软账号登录（OAuth 2.0）
- 离线模式支持
- 账户信息本地存储

### 主题系统
- 支持深色/浅色模式切换
- 多种预设主题颜色（绿色、蓝色、紫色等）
- 自定义主题颜色
- 动态主题切换

### 性能优化
- 使用 Coil 异步图片加载
- ExoPlayer 视频背景播放
- 懒加载和虚拟滚动
- JNI Hook 优化性能

## 测试

### 运行测试
```bash
# 单元测试
./gradlew test

# Android 仪器测试
./gradlew connectedAndroidTest
```

### 调试
- 使用 Android Studio 的调试器
- 查看 Logcat 日志
- 使用 `Logger` 类记录自定义日志

## 常见问题

### 构建失败
- 确保 JDK 11 已安装并配置正确
- 检查 Android SDK 和 NDK 版本
- 清理构建缓存: `./gradlew clean`

### 运行时崩溃
- 检查 Logcat 日志
- 确认 Java 运行时已正确安装
- 检查设备架构兼容性

### 资源下载失败
- 检查网络连接
- 确认存储权限已授予
- 查看下载管理器日志

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

本项目采用 GPL-3.0 许可证。详见 [LICENSE](LICENSE) 文件。

## 联系方式

- GitHub: https://github.com/LanRhyme/ShardLauncher
- Issues: https://github.com/LanRhyme/ShardLauncher/issues

## 更新日志

当前版本: **a0.25.1221 - NEBULA**

详见 GitHub Releases 页面。