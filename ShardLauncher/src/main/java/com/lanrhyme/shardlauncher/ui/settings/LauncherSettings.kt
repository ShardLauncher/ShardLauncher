package com.lanrhyme.shardlauncher.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lanrhyme.shardlauncher.common.SidebarPosition
import com.lanrhyme.shardlauncher.ui.components.basic.CollapsibleCard
import com.lanrhyme.shardlauncher.ui.components.basic.ShardDialog
import com.lanrhyme.shardlauncher.ui.components.basic.ShardDropdownMenu
import com.lanrhyme.shardlauncher.ui.components.layout.IconSwitchLayoutCard
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
import com.lanrhyme.shardlauncher.ui.components.dialog.MusicPlayerDialog
import com.lanrhyme.shardlauncher.ui.components.basic.PopupContainer
import com.lanrhyme.shardlauncher.ui.components.basic.ScrollIndicator
import com.lanrhyme.shardlauncher.ui.components.layout.SimpleListLayoutCard
import com.lanrhyme.shardlauncher.ui.components.layout.SliderLayoutCard
import com.lanrhyme.shardlauncher.ui.components.layout.SwitchLayoutCard
import com.lanrhyme.shardlauncher.ui.components.basic.TitleAndSummary
import com.lanrhyme.shardlauncher.ui.components.basic.animatedAppearance
import com.lanrhyme.shardlauncher.ui.components.color.HsvColorPicker
import com.lanrhyme.shardlauncher.ui.components.color.ThemeColorEditor
import com.lanrhyme.shardlauncher.ui.music.MusicPlayerViewModel
import com.lanrhyme.shardlauncher.ui.theme.ColorPalettes
import com.lanrhyme.shardlauncher.ui.theme.ThemeColor
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.abs

data class BackgroundItem(
        val uri: String,
        val isVideo: Boolean,
        val blur: Float = 0f,
        val brightness: Float = 0f
)

/**
 * 视频播放器组件
 * 用于播放背景视频
 *
 * @param uri 视频URI
 * @param modifier 修饰符
 */
@Composable
fun VideoPlayer(uri: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exoPlayer =
            remember(uri, context) {
                androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
                    setMediaItem(MediaItem.fromUri(uri))
                    prepare()
                    playWhenReady = true
                    volume = 0f // Mute video
                }
            }

    DisposableEffect(uri) { onDispose { exoPlayer.release() } }

    AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false
                }
            },
            modifier = modifier
    )
}

private const val PREFS_NAME = "launcher_settings"
private const val KEY_BACKGROUND_ITEMS = "background_items"
private const val KEY_RANDOM_BACKGROUND = "random_background"

/**
 * 启动器设置内容组件
 * 包含所有启动器相关的设置项
 *
 * @param isDarkTheme 是否为深色主题
 * @param onThemeToggle 切换主题回调
 * @param sidebarPosition 侧边栏位置
 * @param onPositionChange 侧边栏位置变更回调
 * @param themeColor 主题颜色
 * @param onThemeColorChange 主题颜色变更回调
 * @param customPrimaryColor 自定义主色
 * @param onCustomPrimaryColorChange 自定义主色变更回调
 * @param lightColorScheme 浅色主题配色方案
 * @param darkColorScheme 深色主题配色方案
 * @param onLightColorSchemeChange 浅色主题配色方案变更回调
 * @param onDarkColorSchemeChange 深色主题配色方案变更回调
 * @param enableBackgroundLightEffect 是否启用背景光效
 * @param onEnableBackgroundLightEffectChange 背景光效开关回调
 * @param lightEffectAnimationSpeed 光效动画速度
 * @param onLightEffectAnimationSpeedChange 光效动画速度变更回调
 * @param enableBackgroundLightEffectCustomColor 是否启用自定义光效颜色
 * @param onEnableBackgroundLightEffectCustomColorChange 自定义光效颜色开关回调
 * @param backgroundLightEffectCustomColor 自定义光效颜色
 * @param onBackgroundLightEffectCustomColorChange 自定义光效颜色变更回调
 * @param animationSpeed 全局动画速度
 * @param onAnimationSpeedChange 全局动画速度变更回调
 * @param launcherBackgroundUri 启动器背景URI
 * @param onLauncherBackgroundUriChange 启动器背景URI变更回调
 * @param launcherBackgroundBlur 背景模糊度
 * @param onLauncherBackgroundBlurChange 背景模糊度变更回调
 * @param launcherBackgroundBrightness 背景亮度
 * @param onLauncherBackgroundBrightnessChange 背景亮度变更回调
 * @param enableParallax 是否启用视差效果
 * @param onEnableParallaxChange 视差效果开关回调
 * @param parallaxMagnitude 视差强度
 * @param onParallaxMagnitudeChange 视差强度变更回调
 * @param enableVersionCheck 是否启用版本检查
 * @param onEnableVersionCheckChange 版本检查开关回调
 * @param uiScale UI缩放比例
 * @param onUiScaleChange UI缩放比例变更回调
 * @param isGlowEffectEnabled 是否启用发光效果
 * @param onIsGlowEffectEnabledChange 发光效果开关回调
 * @param onIsCardBlurEnabledChange 卡片模糊效果开关回调
 * @param onCardAlphaChange 卡片透明度变更回调
 * @param isMusicPlayerEnabled 是否启用音乐播放器
 * @param onIsMusicPlayerEnabledChange 音乐播放器开关回调
 * @param musicPlayerViewModel 音乐播放器ViewModel
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun LauncherSettingsContent(
        isDarkTheme: Boolean,
        onThemeToggle: () -> Unit,
        sidebarPosition: SidebarPosition,
        onPositionChange: (SidebarPosition) -> Unit,
        themeColor: ThemeColor,
        onThemeColorChange: (ThemeColor) -> Unit,
        customPrimaryColor: Color,
        onCustomPrimaryColorChange: (Color) -> Unit,
        lightColorScheme: ColorScheme,
        darkColorScheme: ColorScheme,
        onLightColorSchemeChange: (ColorScheme) -> Unit,
        onDarkColorSchemeChange: (ColorScheme) -> Unit,
        enableBackgroundLightEffect: Boolean,
        onEnableBackgroundLightEffectChange: () -> Unit,
        lightEffectAnimationSpeed: Float,
        onLightEffectAnimationSpeedChange: (Float) -> Unit,
        enableBackgroundLightEffectCustomColor: Boolean,
        onEnableBackgroundLightEffectCustomColorChange: () -> Unit,
        backgroundLightEffectCustomColor: Color,
        onBackgroundLightEffectCustomColorChange: (Color) -> Unit,
        animationSpeed: Float,
        onAnimationSpeedChange: (Float) -> Unit,
        launcherBackgroundUri: String?,
        onLauncherBackgroundUriChange: (String?) -> Unit,
        launcherBackgroundBlur: Float,
        onLauncherBackgroundBlurChange: (Float) -> Unit,
        launcherBackgroundBrightness: Float,
        onLauncherBackgroundBrightnessChange: (Float) -> Unit,
        enableParallax: Boolean,
        onEnableParallaxChange: (Boolean) -> Unit,
        parallaxMagnitude: Float,
        onParallaxMagnitudeChange: (Float) -> Unit,
        enableVersionCheck: Boolean,
        onEnableVersionCheckChange: () -> Unit,
        uiScale: Float,
        onUiScaleChange: (Float) -> Unit,
        isGlowEffectEnabled: Boolean,
        onIsGlowEffectEnabledChange: () -> Unit,
        onIsCardBlurEnabledChange: () -> Unit,
        onCardAlphaChange: (Float) -> Unit,
        isMusicPlayerEnabled: Boolean,
        onIsMusicPlayerEnabledChange: () -> Unit,
        musicPlayerViewModel: MusicPlayerViewModel
) {
    val cardLayoutConfig = LocalCardLayoutConfig.current
    val isCardBlurEnabled = cardLayoutConfig.isCardBlurEnabled
    val cardAlpha = cardLayoutConfig.cardAlpha
    val hazeState = cardLayoutConfig.hazeState
    val animatedSpeed by
            animateFloatAsState(
                    targetValue = animationSpeed,
                    label = "Animation Speed",
                    animationSpec = tween((1000 / animationSpeed).toInt())
            )
    val context = LocalContext.current

    var showColorPickerDialog by remember { mutableStateOf(false) }
    var tempLightColorScheme by remember(lightColorScheme) { mutableStateOf(lightColorScheme) }
    var tempDarkColorScheme by remember(darkColorScheme) { mutableStateOf(darkColorScheme) }

    var showLightEffectColorPickerDialog by remember { mutableStateOf(false) }
    var tempLightEffectColor by
            remember(backgroundLightEffectCustomColor) {
                mutableStateOf(backgroundLightEffectCustomColor)
            }

    var showBackgroundDialog by remember { mutableStateOf(false) }
    var showMusicPlayerDialog by remember { mutableStateOf(false) }

    var backgroundItems by remember { mutableStateOf(listOf<BackgroundItem>()) }
    var selectedBackground by remember { mutableStateOf<BackgroundItem?>(null) }
    var showAddBackgroundMenu by remember { mutableStateOf(false) }
    var randomBackground by remember { mutableStateOf(false) }
    var showDeleteBackgroundMenu by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<BackgroundItem?>(null) }

    var tempBlur by remember { mutableStateOf(0f) }
    var tempBrightness by remember { mutableStateOf(0f) }
    var tempEnableParallax by remember { mutableStateOf(enableParallax) }
    var tempParallaxMagnitude by remember { mutableStateOf(parallaxMagnitude) }

    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val gson = remember { Gson() }

    fun addBackground(uri: String, isVideo: Boolean) {
        val newItem = BackgroundItem(uri, isVideo)
        backgroundItems = backgroundItems + newItem
        selectedBackground = newItem
    }

    fun removeBackground(item: BackgroundItem) {
        backgroundItems = backgroundItems.filter { it.uri != item.uri }
        if (selectedBackground?.uri == item.uri) {
            selectedBackground = backgroundItems.firstOrNull()
        }
    }

    // Document Provider launchers for selecting images and videos
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            addBackground(it.toString(), false)
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            addBackground(it.toString(), true)
        }
    }

    LaunchedEffect(Unit) {
        val json = prefs.getString(KEY_BACKGROUND_ITEMS, null)
        if (json != null) {
            val type = object : TypeToken<List<BackgroundItem>>() {}.type
            backgroundItems = gson.fromJson(json, type)
        }
        randomBackground = prefs.getBoolean(KEY_RANDOM_BACKGROUND, false)
    }

    LaunchedEffect(backgroundItems, randomBackground) {
        val json = gson.toJson(backgroundItems)
        prefs.edit()
                .putString(KEY_BACKGROUND_ITEMS, json)
                .putBoolean(KEY_RANDOM_BACKGROUND, randomBackground)
                .apply()
    }

    LaunchedEffect(selectedBackground) {
        selectedBackground?.let {
            tempBlur = it.blur
            tempBrightness = it.brightness
        }
    }

    LaunchedEffect(showBackgroundDialog) {
        if (showBackgroundDialog) {
            selectedBackground = backgroundItems.find { it.uri == launcherBackgroundUri }
            tempEnableParallax = enableParallax
            tempParallaxMagnitude = parallaxMagnitude
        }
    }

    ShardDialog(
            visible = showBackgroundDialog,
            onDismissRequest = { showBackgroundDialog = false }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            LazyColumn(modifier = Modifier.weight(2f).padding(16.dp)) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    TitleAndSummary(title = "选择背景", summary = "选择自定义背景以使用")
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                            modifier =
                                    Modifier.border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outline,
                                                    RoundedCornerShape(16.dp)
                                            )
                                            .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(backgroundItems, key = { it.uri }) { item ->
                            Box {
                                var isPressed by remember { mutableStateOf(false) }
                                val scale by
                                        animateFloatAsState(
                                                targetValue = if (isPressed) 0.95f else 1f,
                                                label = ""
                                        )
                                Box(
                                        modifier =
                                                Modifier.scale(scale)
                                                        .size(160.dp, 90.dp)
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .background(
                                                                MaterialTheme.colorScheme
                                                                        .surfaceVariant
                                                        )
                                                        .pointerInput(Unit) {
                                                            detectTapGestures(
                                                                    onPress = {
                                                                        isPressed = true
                                                                        tryAwaitRelease()
                                                                        isPressed = false
                                                                    },
                                                                    onTap = {
                                                                        selectedBackground = item
                                                                    },
                                                                    onLongPress = {
                                                                        itemToDelete = item
                                                                        showDeleteBackgroundMenu =
                                                                                true
                                                                    }
                                                            )
                                                        }
                                                        .border(
                                                                width = 2.dp,
                                                                color =
                                                                        if (selectedBackground
                                                                                        ?.uri ==
                                                                                        item.uri
                                                                        )
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primary
                                                                        else Color.Transparent,
                                                                shape = RoundedCornerShape(16.dp)
                                                        )
                                ) {
                                    AsyncImage(
                                            model =
                                                    ImageRequest.Builder(LocalContext.current)
                                                            .data(item.uri)
                                                            .decoderFactory(
                                                                    VideoFrameDecoder.Factory()
                                                            )
                                                            .build(),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize()
                                    )
                                    if (item.isVideo) {
                                        Text(
                                            "视频",
                                            modifier =
                                                Modifier.align(Alignment.BottomStart)
                                                    .padding(4.dp)
                                                    .background(
                                                        Color.Black.copy(
                                                            alpha = 0.5f
                                                        ),
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(
                                                        horizontal = 4.dp,
                                                        vertical = 2.dp
                                                    ),
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                                ShardDropdownMenu(
                                        expanded =
                                                showDeleteBackgroundMenu &&
                                                        itemToDelete?.uri == item.uri,
                                        onDismissRequest = { showDeleteBackgroundMenu = false }
                                ) {
                                    DropdownMenuItem(
                                            text = { Text("删除") },
                                            onClick = {
                                                removeBackground(item)
                                                showDeleteBackgroundMenu = false
                                            }
                                    )
                                }
                            }
                        }
                        item {
                            Box(
                                    modifier =
                                            Modifier.size(160.dp, 90.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .border(
                                                            BorderStroke(
                                                                    1.dp,
                                                                    MaterialTheme.colorScheme
                                                                            .outline
                                                            ),
                                                            RoundedCornerShape(16.dp)
                                                    )
                                                    .clickable { showAddBackgroundMenu = true },
                                    contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Background"
                                )
                            }
                        }
                    }

                    ShardDropdownMenu(
                            expanded = showAddBackgroundMenu,
                            onDismissRequest = { showAddBackgroundMenu = false }
                    ) {
                        DropdownMenuItem(
                                text = { Text("添加图片") },
                                onClick = {
                                    imagePickerLauncher.launch(
                                        arrayOf("image/jpeg", "image/png", "image/gif", "image/webp")
                                    )
                                    showAddBackgroundMenu = false
                                }
                        )
                        DropdownMenuItem(
                                text = { Text("添加视频") },
                                onClick = {
                                    videoPickerLauncher.launch(
                                        arrayOf("video/mp4", "video/webm", "video/x-matroska")
                                    )
                                    showAddBackgroundMenu = false
                                }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("背景设置", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    SliderLayoutCard(
                            value = tempBlur,
                            onValueChange = { tempBlur = it },
                            valueRange = 0f..25f,
                            title = "背景模糊",
                            displayValue = tempBlur,
                            enabled = selectedBackground != null && !selectedBackground!!.isVideo,
                            isGlowEffectEnabled = isGlowEffectEnabled
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    SliderLayoutCard(
                            value = tempBrightness,
                            onValueChange = { tempBrightness = it },
                            valueRange = -100f..100f,
                            title = "背景明度",
                            displayValue = tempBrightness,
                            enabled = selectedBackground != null,
                            isGlowEffectEnabled = isGlowEffectEnabled
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("全局配置", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    SwitchLayoutCard(
                            checked = randomBackground,
                            onCheckedChange = { randomBackground = !randomBackground },
                            title = "启动时随机选择背景"
                    )
                    SwitchLayoutCard(
                            checked = tempEnableParallax,
                            onCheckedChange = { tempEnableParallax = !tempEnableParallax },
                            title = "启用背景视差效果"
                    )
                    if (tempEnableParallax) {
                        SliderLayoutCard(
                                value = tempParallaxMagnitude,
                                onValueChange = { tempParallaxMagnitude = it },
                                valueRange = 1f..40f,
                                title = "视差幅度",
                                displayValue = tempParallaxMagnitude,
                                isGlowEffectEnabled = isGlowEffectEnabled
                        )
                    }
                }
            }
            VerticalDivider(
                    modifier = Modifier.fillMaxHeight(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
            Column(modifier = Modifier.weight(1f).padding(16.dp)) {
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .aspectRatio(16f / 9f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .then(
                                                if (Build.VERSION.SDK_INT >=
                                                                Build.VERSION_CODES.S &&
                                                                selectedBackground?.isVideo == false
                                                ) {
                                                    Modifier.blur(radius = tempBlur.dp)
                                                } else {
                                                    Modifier
                                                }
                                        )
                                        .drawWithContent {
                                            drawContent()
                                            if (tempBrightness != 0f) {
                                                val color =
                                                        if (tempBrightness > 0) Color.White
                                                        else Color.Black
                                                drawRect(color, alpha = abs(tempBrightness) / 100f)
                                            }
                                        },
                        contentAlignment = Alignment.Center
                ) {
                    if (selectedBackground != null) {
                        val parallaxScale =
                                if (tempEnableParallax) 1f + (tempParallaxMagnitude - 1f) / 20f
                                else 1f
                        if (selectedBackground!!.isVideo) {
                            VideoPlayer(
                                    uri = selectedBackground!!.uri,
                                    modifier = Modifier.fillMaxSize().scale(parallaxScale)
                            )
                        } else {
                            AsyncImage(
                                    model =
                                            ImageRequest.Builder(LocalContext.current)
                                                    .data(selectedBackground!!.uri)
                                                    .build(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().scale(parallaxScale)
                            )
                        }
                    } else {
                        Text("效果预览")
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { showBackgroundDialog = false }) { Text("取消") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                            onClick = {
                                selectedBackground?.let { current ->
                                    val updatedItem =
                                            current.copy(
                                                    blur = tempBlur,
                                                    brightness = tempBrightness,
                                            )
                                    backgroundItems =
                                            backgroundItems.map {
                                                if (it.uri == current.uri) updatedItem else it
                                            }
                                    onLauncherBackgroundUriChange(updatedItem.uri)
                                    onLauncherBackgroundBlurChange(updatedItem.blur)
                                    onLauncherBackgroundBrightnessChange(updatedItem.brightness)
                                }
                                        ?: run { onLauncherBackgroundUriChange(null) }
                                onEnableParallaxChange(tempEnableParallax)
                                onParallaxMagnitudeChange(tempParallaxMagnitude)
                                showBackgroundDialog = false
                            }
                    ) { Text("确认") }
                }
            }
        }
    }

    if (showMusicPlayerDialog) {
        MusicPlayerDialog(
                onDismissRequest = { showMusicPlayerDialog = false },
                musicPlayerViewModel = musicPlayerViewModel
        )
    }

    if (showColorPickerDialog) {
        AlertDialog(
                onDismissRequest = { showColorPickerDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier.width(650.dp),
                title = { Text("自定义主题颜色") },
                text = {
                    Box(modifier = Modifier.height(500.dp)) {
                        ThemeColorEditor(
                                lightColorScheme = tempLightColorScheme,
                                darkColorScheme = tempDarkColorScheme,
                                onLightColorSchemeChange = { tempLightColorScheme = it },
                                onDarkColorSchemeChange = { tempDarkColorScheme = it }
                        )
                    }
                },
                confirmButton = {
                    Button(
                            onClick = {
                                onLightColorSchemeChange(tempLightColorScheme)
                                onDarkColorSchemeChange(tempDarkColorScheme)
                                onCustomPrimaryColorChange(tempLightColorScheme.primary)
                                onThemeColorChange(ThemeColor.Custom)
                                showColorPickerDialog = false
                            }
                    ) { Text("确定") }
                },
                dismissButton = {
                    Row {
                        TextButton(
                                onClick = {
                                    tempLightColorScheme = ColorPalettes.Green.lightColorScheme
                                    tempDarkColorScheme = ColorPalettes.Green.darkColorScheme
                                }
                        ) { Text("重置") }
                        TextButton(onClick = { showColorPickerDialog = false }) { Text("取消") }
                    }
                }
        )
    }

    val listState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        item {
            Text(
                text = "显示设置",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        .animatedAppearance(0, animationSpeed)
            )
        }
        item {
            SwitchLayoutCard(
                    modifier = Modifier.animatedAppearance(1, animationSpeed),
                    title = "深色模式",
                    summary = if (isDarkTheme) "已开启" else "已关闭",
                    checked = isDarkTheme,
                    onCheckedChange = { onThemeToggle() }
            )
        }
        item {
            SwitchLayoutCard(
                    modifier = Modifier.animatedAppearance(2, animationSpeed),
                    title = "获取Minecraft最新更新信息",
                    summary = "来源于news.bugjump.net",
                    checked = enableVersionCheck,
                    onCheckedChange = { onEnableVersionCheckChange() }
            )
        }
        item {
            SimpleListLayoutCard(
                    modifier = Modifier.animatedAppearance(3, animationSpeed),
                    title = "侧边栏位置",
                    items = SidebarPosition.entries,
                    selectedItem = sidebarPosition,
                    onValueChange = onPositionChange,
                    getItemText = {
                        when (it) {
                            SidebarPosition.Left -> "左侧"
                            SidebarPosition.Right -> "右侧"
                        }
                    }
            )
        }
        item {
            SimpleListLayoutCard(
                    modifier = Modifier.animatedAppearance(4, animationSpeed),
                    title = "主题颜色",
                    items = ThemeColor.entries.toList(),
                    selectedItem = themeColor,
                    onValueChange = { newColor ->
                        if (newColor == ThemeColor.Custom) {
                            tempLightColorScheme = lightColorScheme
                            tempDarkColorScheme = darkColorScheme
                            showColorPickerDialog = true
                        } else {
                            onThemeColorChange(newColor)
                        }
                    },
                    getItemText = { it.title }
            )
        }
        item {
            IconSwitchLayoutCard(
                    modifier = Modifier.animatedAppearance(6, animationSpeed),
                    checked = launcherBackgroundUri != null,
                    onCheckedChange = {
                        if (launcherBackgroundUri != null) {
                            onLauncherBackgroundUriChange(null)
                        } else {
                            showBackgroundDialog = true
                        }
                    },
                    onIconClick = { showBackgroundDialog = true },
                    icon = {
                        Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Background Settings",
                                tint =
                                        if (launcherBackgroundUri != null)
                                                MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    title = "自定义背景",
                    summary = if (launcherBackgroundUri != null) "已开启" else "已关闭"
            )
        }
        item {
            IconSwitchLayoutCard(
                    modifier = Modifier.animatedAppearance(7, animationSpeed),
                    checked = isMusicPlayerEnabled,
                    onCheckedChange = { onIsMusicPlayerEnabledChange() },
                    onIconClick = { showMusicPlayerDialog = true },
                    icon = {
                        Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Music Player Settings",
                                tint =
                                        if (isMusicPlayerEnabled) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    title = "启用音乐播放器",
                    summary = if (isMusicPlayerEnabled) "已开启" else "已关闭"
            )
        }
        item {
            SwitchLayoutCard(
                    modifier = Modifier.animatedAppearance(5, animationSpeed),
                    title = "UI发光效果",
                    summary = "为部分文字和图标添加发光效果",
                    checked = isGlowEffectEnabled,
                    onCheckedChange = { onIsGlowEffectEnabledChange() }
            )
        }
        item {
            SwitchLayoutCard(
                    modifier = Modifier.animatedAppearance(6, animationSpeed),
                    title = "卡片背景启用毛玻璃效果",
                    summary = "对卡片背景启用毛玻璃效果(Android 12+)",
                    checked = isCardBlurEnabled,
                    onCheckedChange = { onIsCardBlurEnabledChange() },
                    enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            )
        }
        item {
            SliderLayoutCard(
                    modifier = Modifier.animatedAppearance(7, animationSpeed),
                    value = cardAlpha,
                    onValueChange = onCardAlphaChange,
                    valueRange = 0f..1f,
                    steps = 19,
                    title = "卡片背景不透明度",
                    summary = "调整卡片背景的不透明度",
                    displayValue = cardAlpha,
                    isGlowEffectEnabled = isGlowEffectEnabled
            )
        }
        item {
            CollapsibleCard(
                    modifier = Modifier.animatedAppearance(8, animationSpeed),
                    title = "背景光效",
                    summary = if (enableBackgroundLightEffect) "已开启" else "已关闭",
                    animationSpeed = animationSpeed
            ) {
                SwitchLayoutCard(
                        checked = enableBackgroundLightEffect,
                        onCheckedChange = { onEnableBackgroundLightEffectChange() },
                        title = "启用背景光效"
                )
                Spacer(modifier = Modifier.height(16.dp))
                SliderLayoutCard(
                        value = lightEffectAnimationSpeed,
                        onValueChange = onLightEffectAnimationSpeedChange,
                        valueRange = 0.5f..2f,
                        steps = 14,
                        title = "光效运动速度",
                        summary = "控制背景光效的运动速度",
                        displayValue = lightEffectAnimationSpeed,
                        enabled = enableBackgroundLightEffect,
                        isGlowEffectEnabled = isGlowEffectEnabled
                )
                Spacer(modifier = Modifier.height(16.dp))
                SwitchLayoutCard(
                        checked = enableBackgroundLightEffectCustomColor,
                        onCheckedChange = { onEnableBackgroundLightEffectCustomColorChange() },
                        title = "自定义光效颜色",
                        enabled = enableBackgroundLightEffect
                )
                if (enableBackgroundLightEffectCustomColor) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box {
                        Row(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .clip(MaterialTheme.shapes.medium)
                                                .clickable {
                                                    tempLightEffectColor =
                                                            backgroundLightEffectCustomColor
                                                    showLightEffectColorPickerDialog = true
                                                }
                                                .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("编辑颜色")
                            Box(
                                    modifier =
                                            Modifier.size(36.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(backgroundLightEffectCustomColor)
                                                    .border(
                                                            1.dp,
                                                            MaterialTheme.colorScheme
                                                                    .outlineVariant,
                                                            RoundedCornerShape(16.dp)
                                                    )
                            )
                        }

                        PopupContainer(
                                visible = showLightEffectColorPickerDialog,
                                onDismissRequest = { showLightEffectColorPickerDialog = false },
                                alignment = Alignment.CenterEnd
                        ) {
                            Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "自定义光效颜色",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                HsvColorPicker(
                                        color = tempLightEffectColor,
                                        onColorSelected = { tempLightEffectColor = it }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                            onClick = { showLightEffectColorPickerDialog = false }
                                    ) { Text("取消") }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                            onClick = {
                                                onBackgroundLightEffectCustomColorChange(
                                                        tempLightEffectColor
                                                )
                                                showLightEffectColorPickerDialog = false
                                            }
                                    ) { Text("确定") }
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            SliderLayoutCard(
                    modifier = Modifier.animatedAppearance(9, animationSpeed),
                    value = animationSpeed,
                    onValueChange = onAnimationSpeedChange,
                    valueRange = 0.5f..2f,
                    steps = 14,
                    title = "动画速率",
                    summary = "控制 UI 动画的播放速度",
                    displayValue = animatedSpeed,
                    isGlowEffectEnabled = isGlowEffectEnabled
            )
        }
        item {
            SliderLayoutCard(
                    modifier = Modifier.animatedAppearance(10, animationSpeed),
                    value = uiScale,
                    onValueChange = onUiScaleChange,
                    valueRange = 0.7f..1.1f,
                    steps = 13,
                    title = "UI 缩放",
                    summary = "调整启动器整体界面的大小",
                    displayValue = uiScale,
                    isGlowEffectEnabled = isGlowEffectEnabled
            )
        }
        item { Spacer(modifier = Modifier.height(45.dp)) }
    }
    
    ScrollIndicator(
            listState = listState,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

