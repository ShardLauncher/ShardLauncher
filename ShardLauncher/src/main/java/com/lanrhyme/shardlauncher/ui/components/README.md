# UI 组件文档

本目录包含 ShardLauncher 项目的各种 Jetpack Compose UI 组件。这些组件遵循 Material Design 3 指南，并结合了 ShardLauncher 的独特视觉风格（如磨砂玻璃效果、圆角设计等）。

所有组件均以可重用性为设计理念，旨在简化整个应用的 UI 开发。

## 目录结构

组件库现已按照功能职责划分为以下子目录：

- `basic/`: 基础原子组件 (Theme, Buttons, Cards)
- `layout/`: 布局容器与配置 (Layout Cards, Config)
- `dialog/`: 业务对话框 (Dialogs)
- `business/`: 特定业务逻辑组件 (FAB, Dropdowns)
- `effect/`: 视觉特效 (Background Effects)

## 详细说明

- [1. 基础组件 (Basic Components)](#1-基础组件-basic-components---basic)
- [2. 布局卡片 (Layout Cards)](#2-布局卡片-layout-cards---layout)
- [3. 对话框 (Dialogs)](#3-对话框-dialogs---dialog)
- [4. 业务组件 (Business Components)](#4-业务组件-business-components---business)
- [5. 视觉特效 (Visual Effects)](#5-视觉特效-visual-effects---effect)

---

### 1. 基础组件 (Basic Components) - `basic/`

主要包含 `ShardThemeComponents.kt` 和 `CommonComponents.kt`。

#### `basic/ShardThemeComponents.kt`
提供符合 ShardTheme 主题规范的基础原子组件。

- **`ShardCard`**
  - 基础卡片容器，支持圆角和背景颜色配置。
  - **特性**: 当 `LocalCardLayoutConfig.isCardBlurEnabled` 开启且系统版本支持时，自动应用高斯模糊 (Haze) 效果。

- **`ShardButton`**
  - 统一风格的按钮组件。
  - **支持类型**: `FILLED` (填充), `OUTLINED` (描边), `TEXT` (文本)。
  - **特性**: 支持自定义颜色、形状，并集成了模糊效果支持。

- **`ShardDialog`**
  - 统一风格的对话框容器。
  - **特性**: 针对复杂布局进行了性能优化，支持全屏半透明背景和入场/出场动画。

- **`ShardDropdownMenu`**
  - 统一样式的下拉菜单。
  - **特性**: 支持磨砂玻璃背景效果。

- **`ShardInputField`**
  - 通用文本输入框。
  - **特性**: 基于 `BasicTextField` 封装，提供一致的边框、背景和内边距样式。

#### `basic/CommonComponents.kt`
通用的交互式组件。

- **`ScalingActionButton`**
  - 带点击缩放动画的按钮。
  - **用途**: 用于需要强调点击反馈的操作，如列表项的次要操作按钮。

- **`StyledFilterChip`**
  - 带样式的过滤标签芯片。
  - **用途**: 用于多选或单选过滤场景。

- **`SegmentedNavigationBar`**
  - 分段式导航栏。
  - **特性**: 包含发光标题和切换选项卡，用于页面内的一级导航。

- **`SubPageNavigationBar`**
  - 子页面导航栏。
  - **特性**: 包含返回按钮和标题，用于二级详情页面的顶部导航。

---

### 2. 布局卡片 (Layout Cards) - `layout/`

主要包含 `LayoutCards.kt` 和 `LocalLayoutConfig.kt`。

#### `layout/LayoutCards.kt`
封装了常见的设置项布局。

- **`SwitchLayoutCard`**
  - 带开关 (Switch) 的卡片。
  - **用途**: 用于布尔值设置（开/关）。

- **`IconSwitchLayoutCard`**
  - 带左侧图标和开关的卡片。
  - **用途**: 图形化的布尔值设置。

- **`SimpleListLayoutCard`**
  - 列表选择卡片。
  - **用途**: 点击后弹出选择列表（如 Dialog 或 Dropdown 逻辑需外部实现，此为触发入口布局）。

- **`SliderLayoutCard`**
  - 滑动条调节卡片。
  - **用途**: 数值范围调整（如音量、亮度）。
  - **特性**: 包含发光边框效果 (`glow`)。

- **`TextInputLayoutCard`**
  - 文本输入卡片。
  - **用途**: 用于修改名称、路径等文本信息。

- **`ButtonLayoutCard`**
  - 纯按钮功能的列表卡片。
  - **用途**: 执行特定操作（如“清除缓存”、“重置设置”）。

#### `layout/LocalLayoutConfig.kt`
- **`LocalCardLayoutConfig`**
  - 布局配置 CompositionLocal。
  - **功能**: 提供全局的卡片外观配置（如 `isCardBlurEnabled`），允许组件根据环境动态调整渲染策略（如降级为纯色背景）。

---

### 3. 对话框 (Dialogs) - `dialog/`

特定功能的业务对话框。

- **`MusicPlayerDialog`** (`dialog/MusicPlayerDialog.kt`)
  - 音乐播放器对话框。
  - **功能**: 展示音乐列表、播放控制、设置等，集成 ViewModel 数据。

- **`ResourceInstallDialog`** (`dialog/ResourceInstallDialog.kt`)
  - 资源安装对话框。
  - **功能**: 检测并引导用户安装缺失的游戏资源文件。

- **`TaskFlowDialog`** (`dialog/TaskFlowDialog.kt`)
  - 任务流进度对话框。
  - **功能**: 展示多步骤任务的执行进度（如：下载 -> 解压 -> 安装），支持终止和后台运行。

---

### 4. 业务组件 (Business Components) - `business/`

与特定业务逻辑强相关的组件。

- **`FluidFab`** (`business/FluidFab.kt`)
  - 流体动画悬浮按钮。
  - **特性**: 具有独特的“粘性”展开动画，用于主界面的核心操作入口。支持多个子菜单项。

- **`LoaderVersionDropdown`** (`business/LoaderVersionDropdown.kt`)
  - 加载器版本选择组件。
  - **功能**: 专门用于选择 Minecraft 加载器（Fabric/Forge）版本的下拉列表，支持分页加载。

- **`VersionItem`** (`business/VersionItem.kt`)
  - 游戏版本列表项。
  - **功能**: 在主页版本列表中展示单个游戏版本的详细信息（图标、名称、状态）。

---

### 5. 视觉特效 (Visual Effects) - `effect/`

- **`BackgroundLightEffect`** (`effect/BackgroundLightEffect.kt`)
  - 背景光斑动画。
  - **功能**: 在页面背景绘制缓慢移动和呼吸的彩色光斑，增强视觉层次感。
