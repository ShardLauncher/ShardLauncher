# ShardLauncher UI 组件库

本文档列出了 `ui/components` 目录下的所有可复用 UI 组件，按功能分类。

## 目录结构

```
ui/components/
├── basic/          # 基础组件
├── layout/         # 布局组件
├── dialog/         # 对话框组件
├── business/       # 业务组件
├── tiles/          # 磁贴组件
├── effect/         # 视觉效果
├── color/          # 颜色相关
└── filemanager/    # 文件管理器
```

## 基础组件 (Basic)

位于 `ui/components/basic` 目录下。这些是构建其他组件的基础，**优先使用这些组件**而不是直接使用 Material Design 3 组件。

### Cards.kt - 卡片组件

**主要组件：**

- **`ShardCard`** - 统一的卡片组件
  - 支持多种样式：`DEFAULT`, `GLASS`, `GRADIENT`, `ACCENT`, `BORDERED`
  - 内置模糊效果支持（Android 12+）
  - 可选点击效果和边框

- **`ExpandableCard`** - 可展开/折叠的卡片
  - 支持受控和非受控模式
  - 内置展开/收起动画

- **`InfoCard`** - 信息展示卡片
  - 显示标题、数值、图标组合
  - 支持多种样式

- **`ActionCard`** - 操作卡片
  - 用于可点击的操作项
  - 图标 + 标题 + 副标题布局

- **`CombinedCard`** - 组合卡片
  - 标题 + 摘要 + 内容区域

### Buttons.kt - 按钮组件

**主要组件：**

- **`ShardButton`** - 统一的按钮组件
  - 类型：`FILLED`, `OUTLINED`, `TEXT`, `GRADIENT`, `GLASS`
  - 尺寸：`SMALL`, `MEDIUM`, `LARGE`
  - 内置缩放动画反馈

- **`ShardIconButton`** - 图标按钮
  - 圆形按钮，常用于卡片内部
  - 支持发光效果

- **`ShardButtonWithIcon`** - 带图标的文字按钮（便捷组件）

### Dialogs.kt - 对话框组件

**主要组件：**

- **`ShardDialog`** - 统一的对话框组件
  - 尺寸：`SMALL`, `MEDIUM`, `LARGE`, `FULL`
  - 支持自定义宽高
  - 内置进入/退出动画

- **`ShardAlertDialog`** - 警告/确认对话框
  - 标题 + 正文 + 操作按钮
  - 支持字符串或自定义内容

- **`PopupContainer`** - 弹出式容器
  - 轻量级弹出内容
  - 比 Dialog 更灵活

### CommonComponents.kt - 通用组件

- **`TitleAndSummary`** - 标题和摘要文本组合
- **`SearchTextField`** - 搜索输入框
- **`StyledFilterChip`** - 样式化的过滤芯片
- **`SegmentedNavigationBar`** - 分段式导航栏
- **`SubPageNavigationBar`** - 子页面导航栏（带返回）
- **`ScrollIndicator`** - 滚动指示器
- **`CapsuleTextField`** - 胶囊风格输入框
- **`BackgroundTextTag`** - 带背景的文字标签
- **`TitledDivider`** - 带标题的分割线

### 修饰符扩展 (Modifier Extensions)

- **`Modifier.glow()`** - 发光效果
- **`Modifier.animatedAppearance()`** - 入场动画
- **`Modifier.selectableCard()`** - 可选卡片效果
- **`Modifier.shimmer()`** - 微光/骨架屏效果
- **`Modifier.glassBackground()`** - 玻璃态背景

## 布局组件 (Layout)

位于 `ui/components/layout` 目录下。

### LayoutCards.kt - 布局卡片

- **`SwitchLayoutCard`** - 带开关的卡片
- **`IconSwitchLayoutCard`** - 带图标和开关的卡片
- **`SimpleListLayoutCard`** - 列表选择卡片
- **`SliderLayoutCard`** - 滑块卡片
- **`TextInputLayoutCard`** - 文本输入卡片
- **`ButtonLayoutCard`** - 带按钮的卡片

### LocalLayoutConfig.kt - 布局配置

- **`CardLayoutConfig`** - 卡片布局配置数据类
- **`LocalCardLayoutConfig`** - CompositionLocal 配置

## 对话框组件 (Dialog)

位于 `ui/components/dialog` 目录下。

- **`TaskFlowDialog`** - 任务流进度对话框
- **`ResourceInstallDialog`** - 资源安装对话框
- **`MusicPlayerDialog`** - 音乐播放器对话框

## 业务组件 (Business)

位于 `ui/components/business` 目录下。

- **`VersionItem`** - 版本列表项
- **`FluidFab`** - 流体动画悬浮按钮
- **`LoaderVersionDropdown`** - 加载器版本下拉选择
- **`MusicCard`** - 音乐卡片

## 磁贴组件 (Tiles)

位于 `ui/components/tiles` 目录下。

- **`TileCard`** - 磁贴卡片（基于 ShardCard）
- **`InfoTile`** - 信息磁贴
- **`ActionTile`** - 操作磁贴
- **`ContentTile`** - 内容磁贴
- **`TileGrid`** - 磁贴网格布局

## 视觉效果 (Effect)

位于 `ui/components/effect` 目录下。

- **`BackgroundLightEffect`** - 背景光斑动画效果

## 颜色相关 (Color)

位于 `ui/components/color` 目录下。

- **`HsvColorPicker`** - HSV 颜色选择器
- **`ThemeColorEditor`** - 主题颜色编辑器
- **`ColorExtensions`** - 颜色扩展函数

## 使用指南

### 1. 优先使用基础组件

```kotlin
// ✅ 推荐
ShardCard(
    style = CardStyle.GLASS,
    onClick = { /* ... */ }
) {
    // 内容
}

// ❌ 不推荐直接使用 Material Card
Card(
    // 需要手动配置所有样式
) { }
```

### 2. 使用布局卡片快速构建设置界面

```kotlin
SwitchLayoutCard(
    checked = isEnabled,
    onCheckedChange = { isEnabled = !isEnabled },
    title = "启用功能",
    summary = "功能的详细描述"
)
```

### 3. 统一使用 ShardDialog

```kotlin
ShardDialog(
    visible = showDialog,
    onDismissRequest = { showDialog = false },
    size = DialogSize.MEDIUM
) {
    // 对话框内容
}
```

### 4. 样式系统

所有组件都支持通过 `CardStyle` 设置样式：

- `DEFAULT` - 默认样式，使用 Surface 颜色
- `GLASS` - 毛玻璃效果（Android 12+ 自动启用模糊）
- `GRADIENT` - 渐变背景（主色到第三色）
- `ACCENT` - 强调色背景
- `BORDERED` - 带发光边框

## 废弃组件

以下组件已废弃，请使用替代组件：

| 废弃组件 | 替代组件 |
|---------|---------|
| `CollapsibleCard` | `ExpandableCard` |
| `ShardGlassCard` | `ShardCard(style = CardStyle.GLASS)` |
| `ScalingActionButton` | `ShardButton(type = ButtonType.GRADIENT)` |
| `TileButton` | `ShardIconButton` |
| `SimpleCard` | `ShardCard` |

## 最佳实践

1. **一致性**：始终使用基础组件库中的组件，保持 UI 一致性
2. **样式**：优先使用 `CardStyle` 枚举而不是手动设置颜色
3. **响应式**：使用组件提供的 `enabled` 参数控制交互状态
4. **可访问性**：为所有图标按钮提供 `contentDescription`
5. **性能**：避免在Composable中创建大量临时对象