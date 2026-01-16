package com.lanrhyme.shardlauncher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import coil.compose.rememberAsyncImagePainter
import com.lanrhyme.shardlauncher.common.SidebarPosition
import com.lanrhyme.shardlauncher.data.SettingsRepository
import com.lanrhyme.shardlauncher.game.account.AccountsManager
import com.lanrhyme.shardlauncher.service.MusicPlayerService
import com.lanrhyme.shardlauncher.ui.LocalSettingsProvider
import com.lanrhyme.shardlauncher.ui.SplashScreen
import com.lanrhyme.shardlauncher.ui.account.AccountScreen
import com.lanrhyme.shardlauncher.ui.account.AccountViewModel
import com.lanrhyme.shardlauncher.ui.components.BackgroundLightEffect
import com.lanrhyme.shardlauncher.ui.components.CardLayoutConfig
import com.lanrhyme.shardlauncher.ui.components.LocalCardLayoutConfig
import com.lanrhyme.shardlauncher.ui.components.glow
import com.lanrhyme.shardlauncher.ui.developeroptions.ComponentDemoScreen
import com.lanrhyme.shardlauncher.ui.developeroptions.LogViewerScreen
import com.lanrhyme.shardlauncher.ui.developeroptions.DeveloperOptionsScreen
import com.lanrhyme.shardlauncher.ui.downloads.DownloadScreen
import com.lanrhyme.shardlauncher.ui.downloads.VersionDetailScreen
import com.lanrhyme.shardlauncher.ui.home.HomeScreen
import com.lanrhyme.shardlauncher.ui.music.MusicPlayerViewModel
import com.lanrhyme.shardlauncher.ui.navigation.Screen
import com.lanrhyme.shardlauncher.ui.navigation.getRootRoute
import com.lanrhyme.shardlauncher.ui.navigation.navigationItems
import com.lanrhyme.shardlauncher.ui.notification.NotificationManager
import com.lanrhyme.shardlauncher.ui.notification.NotificationPanel
import com.lanrhyme.shardlauncher.ui.notification.NotificationPopupHost
import com.lanrhyme.shardlauncher.ui.notification.NotificationType
import com.lanrhyme.shardlauncher.ui.settings.SettingsScreen
import com.lanrhyme.shardlauncher.ui.theme.ColorPalettes
import com.lanrhyme.shardlauncher.ui.theme.ShardLauncherTheme
import com.lanrhyme.shardlauncher.ui.theme.ThemeColor
import com.lanrhyme.shardlauncher.ui.version.VersionScreen
import com.lanrhyme.shardlauncher.utils.rememberParallaxSensorHelper
import com.lanrhyme.shardlauncher.components.ComponentUnpacker
import com.lanrhyme.shardlauncher.game.resource.ResourceManager
import com.lanrhyme.shardlauncher.utils.logging.Logger
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlin.math.abs
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val tag = "MainActivity"
    private lateinit var settingsRepository: SettingsRepository
    private val accountViewModel: AccountViewModel by viewModels()
    private val musicPlayerViewModel: MusicPlayerViewModel by viewModels {
        MusicPlayerViewModel.Factory(application, settingsRepository)
    }
    private val newIntentState = mutableStateOf<Intent?>(null)

    /**
     * Check and install required resources automatically on startup
     */
    private suspend fun checkAndInstallResources(context: Context) {
        try {
            Logger.i(tag, "Checking required resources...")
            val checkResult = ResourceManager.checkResources(context)
            
            if (checkResult.missingResources.isNotEmpty()) {
                Logger.i(tag, "Missing resources detected: ${checkResult.missingResources}")
                
                // Install essential resources automatically
                ResourceManager.installEssentialResources(
                    context = context,
                    onProgress = { progress, message ->
                        Logger.d(tag, "Resource installation progress: $progress% - $message")
                    }
                ).onSuccess {
                    Logger.i(tag, "Essential resources installed successfully")
                }.onFailure { error ->
                    Logger.e(tag, "Failed to install essential resources", error)
                }
            } else {
                Logger.i(tag, "All required resources are available")
            }
        } catch (e: Exception) {
            Logger.e(tag, "Error during resource check", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsRepository = SettingsRepository(applicationContext)
        com.lanrhyme.shardlauncher.settings.AllSettings.initialize(settingsRepository)
        AccountsManager.initialize(applicationContext)
        com.lanrhyme.shardlauncher.game.path.GamePathManager.initialize(applicationContext)
        
        // Initialize renderers
        com.lanrhyme.shardlauncher.game.renderer.Renderers.init()
        
        com.lanrhyme.shardlauncher.game.version.installed.VersionsManager.refresh("MainActivity.onCreate")

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            val systemIsDark = isSystemInDarkTheme()
            var isDarkTheme by remember {
                mutableStateOf(settingsRepository.getIsDarkTheme(systemIsDark))
            }
            var sidebarPosition by remember {
                mutableStateOf(settingsRepository.getSidebarPosition())
            }
            var themeColor by remember { mutableStateOf(settingsRepository.getThemeColor()) }
            var customPrimaryColor by remember {
                mutableStateOf(Color(settingsRepository.getCustomPrimaryColor()))
            }
            var lightColorScheme by remember {
                mutableStateOf(
                        settingsRepository.getLightColorScheme()
                                ?: ColorPalettes.Green.lightColorScheme
                )
            }
            var darkColorScheme by remember {
                mutableStateOf(
                        settingsRepository.getDarkColorScheme()
                                ?: ColorPalettes.Green.darkColorScheme
                )
            }
            var animationSpeed by remember {
                mutableStateOf(settingsRepository.getAnimationSpeed())
            }
            var lightEffectAnimationSpeed by remember {
                mutableStateOf(settingsRepository.getLightEffectAnimationSpeed())
            }
            var enableBackgroundLightEffect by remember {
                mutableStateOf(settingsRepository.getEnableBackgroundLightEffect())
            }
            var enableBackgroundLightEffectCustomColor by remember {
                mutableStateOf(settingsRepository.getEnableBackgroundLightEffectCustomColor())
            }
            var backgroundLightEffectCustomColor by remember {
                mutableStateOf(Color(settingsRepository.getBackgroundLightEffectCustomColor()))
            }
            var launcherBackgroundUri by remember { mutableStateOf<String?>(null) }
            var launcherBackgroundBlur by remember { mutableStateOf(0f) }
            var launcherBackgroundBrightness by remember { mutableStateOf(0f) }
            var enableParallax by remember {
                mutableStateOf(settingsRepository.getEnableParallax())
            }
            var parallaxMagnitude by remember {
                mutableStateOf(settingsRepository.getParallaxMagnitude())
            }
            var enableVersionCheck by remember {
                mutableStateOf(settingsRepository.getEnableVersionCheck())
            }
            var uiScale by remember { mutableStateOf(settingsRepository.getUiScale()) }
            var isGlowEffectEnabled by remember {
                mutableStateOf(settingsRepository.getIsGlowEffectEnabled())
            }
            var isCardBlurEnabled by remember {
                mutableStateOf(settingsRepository.getIsCardBlurEnabled())
            }
            var cardAlpha by remember { mutableStateOf(settingsRepository.getCardAlpha()) }
            var isMusicPlayerEnabled by remember {
                mutableStateOf(settingsRepository.getIsMusicPlayerEnabled())
            }

            LaunchedEffect(Unit) {
                // Unpack essential components in background
                ComponentUnpacker.unpackAll(applicationContext)
                
                // Check and install required resources automatically
                checkAndInstallResources(applicationContext)

                val randomBackground = settingsRepository.getRandomBackground()
                if (randomBackground) {
                    val backgroundItems = settingsRepository.getBackgroundItems()
                    if (backgroundItems.isNotEmpty()) {
                        val randomItem = backgroundItems.random()
                        launcherBackgroundUri = randomItem.uri
                        launcherBackgroundBlur = randomItem.blur
                        launcherBackgroundBrightness = randomItem.brightness
                    } else {
                        launcherBackgroundUri = settingsRepository.getLauncherBackgroundUri()
                        launcherBackgroundBlur = settingsRepository.getLauncherBackgroundBlur()
                        launcherBackgroundBrightness =
                                settingsRepository.getLauncherBackgroundBrightness()
                        enableParallax = settingsRepository.getEnableParallax()
                        parallaxMagnitude = settingsRepository.getParallaxMagnitude()
                    }
                } else {
                    launcherBackgroundUri = settingsRepository.getLauncherBackgroundUri()
                    launcherBackgroundBlur = settingsRepository.getLauncherBackgroundBlur()
                    launcherBackgroundBrightness =
                            settingsRepository.getLauncherBackgroundBrightness()
                    enableParallax = settingsRepository.getEnableParallax()
                    parallaxMagnitude = settingsRepository.getParallaxMagnitude()
                }
            }

            val navController = rememberNavController()
            val newIntent by newIntentState

            LaunchedEffect(newIntent) {
                newIntent?.let { intent ->
                    // Wait for NavController to be properly initialized with a graph
                    try {
                        // Use a simple try-catch to handle deep link
                        navController.handleDeepLink(intent)
                    } catch (e: Exception) {
                        // NavController not ready yet or deep link handling failed, ignore
                        // This is expected during app startup
                    }
                    newIntentState.value = null
                }
            }

            var showSplash by remember { mutableStateOf(true) }

            val scaledDensity = Density(
                LocalDensity.current.density * uiScale,
                LocalDensity.current.fontScale * uiScale
            )

            CompositionLocalProvider(
                    LocalDensity provides scaledDensity,
                    LocalSettingsProvider provides
                            com.lanrhyme.shardlauncher.ui.SettingsProvider()
            ) {
                Crossfade(
                        targetState = showSplash,
                        label = "SplashCrossfade",
                        animationSpec = tween(durationMillis = 500)
                ) { show ->
                    if (show) {
                        ShardLauncherTheme(
                                darkTheme = isDarkTheme,
                                themeColor = themeColor,
                                lightColorScheme = lightColorScheme,
                                darkColorScheme = darkColorScheme
                        ) { SplashScreen(onAnimationFinished = { showSplash = false }) }
                    } else {
                        Crossfade(
                                targetState = isDarkTheme,
                                label = "ThemeCrossfade",
                                animationSpec = tween(durationMillis = 500)
                        ) { isDark ->
                            ShardLauncherTheme(
                                    darkTheme = isDark,
                                    themeColor = themeColor,
                                    lightColorScheme = lightColorScheme,
                                    darkColorScheme = darkColorScheme
                            ) {
                                MainScreen(
                                        navController = navController,
                                        isDarkTheme = isDark,
                                        onThemeToggle = {
                                            val newTheme = !isDarkTheme
                                            isDarkTheme = newTheme
                                            settingsRepository.setIsDarkTheme(newTheme)
                                        },
                                        sidebarPosition = sidebarPosition,
                                        onPositionChange = { newPosition ->
                                            sidebarPosition = newPosition
                                            settingsRepository.setSidebarPosition(newPosition)
                                        },
                                        themeColor = themeColor,
                                        onThemeColorChange = { newColor ->
                                            themeColor = newColor
                                            settingsRepository.setThemeColor(newColor)
                                        },
                                        customPrimaryColor = customPrimaryColor,
                                        onCustomPrimaryColorChange = { newColor ->
                                            customPrimaryColor = newColor
                                            settingsRepository.setCustomPrimaryColor(
                                                    newColor.toArgb()
                                            )
                                        },
                                        lightColorScheme = lightColorScheme,
                                        darkColorScheme = darkColorScheme,
                                        onLightColorSchemeChange = { newScheme ->
                                            lightColorScheme = newScheme
                                            settingsRepository.setLightColorScheme(newScheme)
                                        },
                                        onDarkColorSchemeChange = { newScheme ->
                                            darkColorScheme = newScheme
                                            settingsRepository.setDarkColorScheme(newScheme)
                                        },
                                        animationSpeed = animationSpeed,
                                        onAnimationSpeedChange = { newSpeed ->
                                            animationSpeed = newSpeed
                                            settingsRepository.setAnimationSpeed(newSpeed)
                                        },
                                        lightEffectAnimationSpeed = lightEffectAnimationSpeed,
                                        onLightEffectAnimationSpeedChange = { newSpeed ->
                                            lightEffectAnimationSpeed = newSpeed
                                            settingsRepository.setLightEffectAnimationSpeed(
                                                    newSpeed
                                            )
                                        },
                                        enableBackgroundLightEffect = enableBackgroundLightEffect,
                                        onEnableBackgroundLightEffectChange = {
                                            val newValue = !enableBackgroundLightEffect
                                            enableBackgroundLightEffect = newValue
                                            settingsRepository.setEnableBackgroundLightEffect(
                                                    newValue
                                            )
                                        },
                                        enableBackgroundLightEffectCustomColor =
                                                enableBackgroundLightEffectCustomColor,
                                        onEnableBackgroundLightEffectCustomColorChange = {
                                            val newValue = !enableBackgroundLightEffectCustomColor
                                            enableBackgroundLightEffectCustomColor = newValue
                                            settingsRepository
                                                    .setEnableBackgroundLightEffectCustomColor(
                                                            newValue
                                                    )
                                        },
                                        backgroundLightEffectCustomColor =
                                                backgroundLightEffectCustomColor,
                                        onBackgroundLightEffectCustomColorChange = { newColor ->
                                            backgroundLightEffectCustomColor = newColor
                                            settingsRepository.setBackgroundLightEffectCustomColor(
                                                    newColor.toArgb()
                                            )
                                        },
                                        launcherBackgroundUri = launcherBackgroundUri,
                                        onLauncherBackgroundUriChange = {
                                            launcherBackgroundUri = it
                                            settingsRepository.setLauncherBackgroundUri(it)
                                        },
                                        launcherBackgroundBlur = launcherBackgroundBlur,
                                        onLauncherBackgroundBlurChange = {
                                            launcherBackgroundBlur = it
                                            settingsRepository.setLauncherBackgroundBlur(it)
                                        },
                                        launcherBackgroundBrightness = launcherBackgroundBrightness,
                                        onLauncherBackgroundBrightnessChange = {
                                            launcherBackgroundBrightness = it
                                            settingsRepository.setLauncherBackgroundBrightness(it)
                                        },
                                        enableParallax = enableParallax,
                                        onEnableParallaxChange = {
                                            enableParallax = it
                                            settingsRepository.setEnableParallax(it)
                                        },
                                        parallaxMagnitude = parallaxMagnitude,
                                        onParallaxMagnitudeChange = {
                                            parallaxMagnitude = it
                                            settingsRepository.setParallaxMagnitude(it)
                                        },
                                        enableVersionCheck = enableVersionCheck,
                                        onEnableVersionCheckChange = {
                                            val newValue = !enableVersionCheck
                                            enableVersionCheck = newValue
                                            settingsRepository.setEnableVersionCheck(newValue)
                                        },
                                        uiScale = uiScale,
                                        onUiScaleChange = {
                                            uiScale = it
                                            settingsRepository.setUiScale(it)
                                        },
                                        isGlowEffectEnabled = isGlowEffectEnabled,
                                        onIsGlowEffectEnabledChange = {
                                            val newValue = !isGlowEffectEnabled
                                            isGlowEffectEnabled = newValue
                                            settingsRepository.setIsGlowEffectEnabled(newValue)
                                        },
                                        isCardBlurEnabled = isCardBlurEnabled,
                                        onIsCardBlurEnabledChange = {
                                            val newValue = !isCardBlurEnabled
                                            isCardBlurEnabled = newValue
                                            settingsRepository.setIsCardBlurEnabled(newValue)
                                        },
                                        cardAlpha = cardAlpha,
                                         onCardAlphaChange = {
                                             cardAlpha = it
                                             settingsRepository.setCardAlpha(it)
                                         },
                                         isMusicPlayerEnabled = isMusicPlayerEnabled,
                                        onIsMusicPlayerEnabledChange = {
                                            val newValue = !isMusicPlayerEnabled
                                            isMusicPlayerEnabled = newValue
                                            settingsRepository.setIsMusicPlayerEnabled(newValue)
                                        },
                                        accountViewModel = accountViewModel,
                                        musicPlayerViewModel = musicPlayerViewModel
                                )
                            }
                        }
                    }
                }
            }
            LaunchedEffect(isMusicPlayerEnabled) {
                if (isMusicPlayerEnabled && settingsRepository.getAutoPlayMusic()) {
                    val intent = Intent(this@MainActivity, MusicPlayerService::class.java)
                    startService(intent)
                } else {
                    val intent = Intent(this@MainActivity, MusicPlayerService::class.java)
                    stopService(intent)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        newIntentState.value = intent
    }
}

@Composable
fun MainScreen(
        navController: NavHostController,
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
        animationSpeed: Float,
        onAnimationSpeedChange: (Float) -> Unit,
        lightEffectAnimationSpeed: Float,
        onLightEffectAnimationSpeedChange: (Float) -> Unit,
        enableBackgroundLightEffect: Boolean,
        onEnableBackgroundLightEffectChange: () -> Unit,
        enableBackgroundLightEffectCustomColor: Boolean,
        onEnableBackgroundLightEffectCustomColorChange: () -> Unit,
        backgroundLightEffectCustomColor: Color,
        onBackgroundLightEffectCustomColorChange: (Color) -> Unit,
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
        isCardBlurEnabled: Boolean,
        onIsCardBlurEnabledChange: () -> Unit,
        cardAlpha: Float,
         onCardAlphaChange: (Float) -> Unit,
        isMusicPlayerEnabled: Boolean,
        onIsMusicPlayerEnabledChange: () -> Unit,
        accountViewModel: AccountViewModel,
        musicPlayerViewModel: MusicPlayerViewModel
) {
    var isSidebarExpanded by remember { mutableStateOf(false) }
    val animationDuration = (300 / animationSpeed).toInt()
    val hazeState = remember { HazeState() }
    val parallaxState by rememberParallaxSensorHelper(enableParallax, parallaxMagnitude)

    val sidebarWidth by
            animateDpAsState(
                    targetValue = if (isSidebarExpanded) 220.dp else 72.dp,
                    animationSpec = tween(durationMillis = animationDuration),
                    label = "SidebarWidth"
            )

    val contentBlurRadius by
            animateDpAsState(
                    targetValue = if (isSidebarExpanded) 8.dp else 0.dp,
                    animationSpec = tween(durationMillis = animationDuration),
                    label = "ContentBlur"
            )

    val context = LocalContext.current

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().hazeSource(state = hazeState)) {
                if (launcherBackgroundUri != null) {
                    val parallaxScale =
                            if (enableParallax) 1f + (parallaxMagnitude - 1f) / 20f else 1f
                    val isVideo = launcherBackgroundUri.endsWith(".mp4")
                    if (isVideo) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val exoPlayer =
                                    remember(launcherBackgroundUri) {
                                        ExoPlayer.Builder(context).build().apply {
                                            setMediaItem(
                                                    MediaItem.fromUri(
                                                            Uri.parse(launcherBackgroundUri)
                                                    )
                                            )
                                            videoScalingMode =
                                                    C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                                            repeatMode = Player.REPEAT_MODE_ALL
                                            playWhenReady = true
                                            prepare()
                                        }
                                    }

                            exoPlayer.volume = 0f

                            DisposableEffect(launcherBackgroundUri) {
                                onDispose { exoPlayer.release() }
                            }

                            AndroidView(
                                    factory = { ctx ->
                                        PlayerView(ctx).apply {
                                            player = exoPlayer
                                            useController = false
                                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                            layoutParams =
                                                    android.widget.FrameLayout.LayoutParams(
                                                            android.view.ViewGroup.LayoutParams
                                                                    .MATCH_PARENT,
                                                            android.view.ViewGroup.LayoutParams
                                                                    .MATCH_PARENT
                                                    )
                                        }
                                    },
                                    modifier =
                                            Modifier.fillMaxSize().graphicsLayer {
                                                scaleX = parallaxScale
                                                scaleY = parallaxScale
                                                translationX = parallaxState.x
                                                translationY = parallaxState.y
                                            }
                            )

                            val brightnessValue = launcherBackgroundBrightness
                            if (brightnessValue != 0f) {
                                val color = if (brightnessValue > 0) Color.White else Color.Black
                                Box(
                                        modifier =
                                                Modifier.fillMaxSize()
                                                        .background(
                                                                color.copy(
                                                                        alpha =
                                                                                abs(
                                                                                        brightnessValue
                                                                                ) / 100f
                                                                )
                                                        )
                                )
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                    painter =
                                            rememberAsyncImagePainter(Uri.parse(launcherBackgroundUri)),
                                    contentDescription = "Launcher Background",
                                    modifier =
                                            Modifier.fillMaxSize()
                                                    .blur(launcherBackgroundBlur.dp)
                                                    .graphicsLayer {
                                                        scaleX = parallaxScale
                                                        scaleY = parallaxScale
                                                        translationX = parallaxState.x
                                                        translationY = parallaxState.y
                                                    },
                                    contentScale = ContentScale.Crop
                            )
                            val brightnessValue = launcherBackgroundBrightness
                            if (brightnessValue != 0f) {
                                val color = if (brightnessValue > 0) Color.White else Color.Black
                                Box(
                                        modifier =
                                                Modifier.fillMaxSize()
                                                        .background(
                                                                color.copy(
                                                                        alpha =
                                                                                abs(
                                                                                        brightnessValue
                                                                                ) / 100f
                                                                )
                                                        )
                                )
                            }
                        }
                    }
                }
                if (enableBackgroundLightEffect) {
                    BackgroundLightEffect(
                            themeColor =
                                    if (enableBackgroundLightEffectCustomColor)
                                            backgroundLightEffectCustomColor
                                    else MaterialTheme.colorScheme.primary,
                            animationSpeed = lightEffectAnimationSpeed
                    )
                }
            }

            CompositionLocalProvider(
                    LocalCardLayoutConfig provides
                            CardLayoutConfig(
                                    isCardBlurEnabled = isCardBlurEnabled,
                                    cardAlpha = cardAlpha,
                                    hazeState = hazeState
                            )
            ) {
                MainContent(
                        modifier = Modifier.fillMaxSize(),
                        isSidebarExpanded = isSidebarExpanded,
                        contentBlurRadius = contentBlurRadius,
                        onSidebarExpandToggle = { isSidebarExpanded = !isSidebarExpanded },
                        navController = navController,
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = onThemeToggle,
                        sidebarPosition = sidebarPosition,
                        onPositionChange = onPositionChange,
                        themeColor = themeColor,
                        onThemeColorChange = onThemeColorChange,
                        customPrimaryColor = customPrimaryColor,
                        onCustomPrimaryColorChange = onCustomPrimaryColorChange,
                        lightColorScheme = lightColorScheme,
                        darkColorScheme = darkColorScheme,
                        onLightColorSchemeChange = onLightColorSchemeChange,
                        onDarkColorSchemeChange = onDarkColorSchemeChange,
                        animationSpeed = animationSpeed,
                        onAnimationSpeedChange = onAnimationSpeedChange,
                        lightEffectAnimationSpeed = lightEffectAnimationSpeed,
                        onLightEffectAnimationSpeedChange = onLightEffectAnimationSpeedChange,
                        enableBackgroundLightEffect = enableBackgroundLightEffect,
                        onEnableBackgroundLightEffectChange = onEnableBackgroundLightEffectChange,
                        enableBackgroundLightEffectCustomColor =
                                enableBackgroundLightEffectCustomColor,
                        onEnableBackgroundLightEffectCustomColorChange =
                                onEnableBackgroundLightEffectCustomColorChange,
                        backgroundLightEffectCustomColor = backgroundLightEffectCustomColor,
                        onBackgroundLightEffectCustomColorChange =
                                onBackgroundLightEffectCustomColorChange,
                        launcherBackgroundUri = launcherBackgroundUri,
                        onLauncherBackgroundUriChange = onLauncherBackgroundUriChange,
                        launcherBackgroundBlur = launcherBackgroundBlur,
                        onLauncherBackgroundBlurChange = onLauncherBackgroundBlurChange,
                        launcherBackgroundBrightness = launcherBackgroundBrightness,
                        onLauncherBackgroundBrightnessChange = onLauncherBackgroundBrightnessChange,
                        enableParallax = enableParallax,
                        onEnableParallaxChange = onEnableParallaxChange,
                        parallaxMagnitude = parallaxMagnitude,
                        onParallaxMagnitudeChange = onParallaxMagnitudeChange,
                        enableVersionCheck = enableVersionCheck,
                        onEnableVersionCheckChange = onEnableVersionCheckChange,
                        uiScale = uiScale,
                        onUiScaleChange = onUiScaleChange,
                        isGlowEffectEnabled = isGlowEffectEnabled,
                        onIsGlowEffectEnabledChange = onIsGlowEffectEnabledChange,
                         onIsCardBlurEnabledChange = onIsCardBlurEnabledChange,
                        onCardAlphaChange = onCardAlphaChange,
                        isMusicPlayerEnabled = isMusicPlayerEnabled,
                        onIsMusicPlayerEnabledChange = onIsMusicPlayerEnabledChange,
                        accountViewModel = accountViewModel,
                        musicPlayerViewModel = musicPlayerViewModel
                )

                val sidebarAlignment =
                        when (sidebarPosition) {
                            SidebarPosition.Left -> Alignment.CenterStart
                            SidebarPosition.Right -> Alignment.CenterEnd
                        }

                SideBar(
                        modifier = Modifier.align(sidebarAlignment).width(sidebarWidth),
                        isExpanded = isSidebarExpanded,
                        onToggleExpand = { isSidebarExpanded = !isSidebarExpanded },
                        navController = navController,
                        position = sidebarPosition,
                        isGlowEffectEnabled = isGlowEffectEnabled,
                        animationSpeed = animationSpeed
                )

                NotificationPanel(isVisible = isSidebarExpanded, sidebarPosition = sidebarPosition)

                NotificationPopupHost()
            }
        }
    }
}

@Composable
fun MainContent(
        modifier: Modifier = Modifier,
        isSidebarExpanded: Boolean,
        contentBlurRadius: androidx.compose.ui.unit.Dp,
        onSidebarExpandToggle: () -> Unit,
        navController: NavHostController,
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
        animationSpeed: Float,
        onAnimationSpeedChange: (Float) -> Unit,
        lightEffectAnimationSpeed: Float,
        onLightEffectAnimationSpeedChange: (Float) -> Unit,
        enableBackgroundLightEffect: Boolean,
        onEnableBackgroundLightEffectChange: () -> Unit,
        enableBackgroundLightEffectCustomColor: Boolean,
        onEnableBackgroundLightEffectCustomColorChange: () -> Unit,
        backgroundLightEffectCustomColor: Color,
        onBackgroundLightEffectCustomColorChange: (Color) -> Unit,
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
        accountViewModel: AccountViewModel,
        musicPlayerViewModel: MusicPlayerViewModel
) {
    val cardLayoutConfig = LocalCardLayoutConfig.current
    val isCardBlurEnabled = cardLayoutConfig.isCardBlurEnabled
    val cardAlpha = cardLayoutConfig.cardAlpha
    val hazeState = cardLayoutConfig.hazeState
    val collapsedSidebarWidth = 72.dp
    val animationDuration = (300 / animationSpeed).toInt()

    val paddingStart by
            animateDpAsState(
                    targetValue =
                            if (sidebarPosition == SidebarPosition.Left) collapsedSidebarWidth
                            else 0.dp,
                    animationSpec = tween(durationMillis = animationDuration),
                    label = "ContentPaddingStart"
            )
    val paddingEnd by
            animateDpAsState(
                    targetValue =
                            if (sidebarPosition == SidebarPosition.Right) collapsedSidebarWidth
                            else 0.dp,
                    animationSpec = tween(durationMillis = animationDuration),
                    label = "ContentPaddingEnd"
            )
    val contentPadding = PaddingValues(start = paddingStart, end = paddingEnd)

    Box(modifier = modifier.blur(radius = contentBlurRadius)) {
        Box(modifier = Modifier.padding(contentPadding)) {
            val navAnimationDuration = (500 / animationSpeed).toInt()
            NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    enterTransition = {
                        val initialRoute = initialState.destination.route
                        val targetRoute = targetState.destination.route
                        val initialIndex =
                                navigationItems.indexOfFirst {
                                    it.route == getRootRoute(initialRoute)
                                }
                        val targetIndex =
                                navigationItems.indexOfFirst {
                                    it.route == getRootRoute(targetRoute)
                                }

                        if (targetRoute == Screen.Home.route) {
                            slideInVertically(animationSpec = tween(navAnimationDuration)) { it } +
                                    fadeIn(animationSpec = tween(navAnimationDuration))
                        } else if (initialIndex == -1 || targetIndex == -1) {
                            slideInVertically(animationSpec = tween(navAnimationDuration)) { it } +
                                    fadeIn(animationSpec = tween(navAnimationDuration))
                        } else if (targetIndex > initialIndex) {
                            slideInVertically(animationSpec = tween(navAnimationDuration)) { it } +
                                    fadeIn(animationSpec = tween(navAnimationDuration))
                        } else {
                            slideInVertically(animationSpec = tween(navAnimationDuration)) { -it } +
                                    fadeIn(animationSpec = tween(navAnimationDuration))
                        }
                    },
                    exitTransition = {
                        val initialRoute = initialState.destination.route
                        val targetRoute = targetState.destination.route
                        val initialIndex =
                                navigationItems.indexOfFirst {
                                    it.route == getRootRoute(initialRoute)
                                }
                        val targetIndex =
                                navigationItems.indexOfFirst {
                                    it.route == getRootRoute(targetRoute)
                                }

                        if (targetRoute == Screen.Home.route) {
                            slideOutVertically(animationSpec = tween(navAnimationDuration)) {
                                -it
                            } + fadeOut(animationSpec = tween(navAnimationDuration))
                        } else if (initialIndex == -1 || targetIndex == -1) {
                            slideOutVertically(animationSpec = tween(navAnimationDuration)) {
                                -it
                            } + fadeOut(animationSpec = tween(navAnimationDuration))
                        } else if (targetIndex > initialIndex) {
                            slideOutVertically(animationSpec = tween(navAnimationDuration)) {
                                -it
                            } + fadeOut(animationSpec = tween(navAnimationDuration))
                        } else {
                            slideOutVertically(animationSpec = tween(navAnimationDuration)) { it } +
                                    fadeOut(animationSpec = tween(navAnimationDuration))
                        }
                    },
                    popEnterTransition = {
                        val initialRoute = initialState.destination.route
                        val targetRoute = targetState.destination.route
                        val initialIndex =
                                navigationItems.indexOfFirst {
                                    it.route == getRootRoute(initialRoute)
                                }
                        val targetIndex =
                                navigationItems.indexOfFirst {
                                    it.route == getRootRoute(targetRoute)
                                }

                        if (targetRoute == Screen.Home.route) {
                            slideInVertically(animationSpec = tween(navAnimationDuration)) { -it } +
                                    fadeIn(animationSpec = tween(navAnimationDuration))
                        } else if (initialIndex == -1 || targetIndex == -1) {
                            slideInVertically(animationSpec = tween(navAnimationDuration)) { -it } +
                                    fadeIn(animationSpec = tween(navAnimationDuration))
                        } else if (targetIndex > initialIndex) {
                            slideInVertically(animationSpec = tween(navAnimationDuration)) { -it } +
                                    fadeIn(animationSpec = tween(navAnimationDuration))
                        } else {
                            slideInVertically(animationSpec = tween(navAnimationDuration)) { it } +
                                    fadeIn(animationSpec = tween(navAnimationDuration))
                        }
                    },
                    popExitTransition = {
                        val initialRoute = initialState.destination.route
                        val targetRoute = targetState.destination.route
                        val initialIndex =
                                navigationItems.indexOfFirst {
                                    it.route == getRootRoute(initialRoute)
                                }
                        val targetIndex =
                                navigationItems.indexOfFirst {
                                    it.route == getRootRoute(targetRoute)
                                }

                        if (targetRoute == Screen.Home.route) {
                            slideOutVertically(animationSpec = tween(navAnimationDuration)) { it } +
                                    fadeOut(animationSpec = tween(navAnimationDuration))
                        } else if (initialIndex == -1 || targetIndex == -1) {
                            slideOutVertically(animationSpec = tween(navAnimationDuration)) { it } +
                                    fadeOut(animationSpec = tween(navAnimationDuration))
                        } else if (targetIndex > initialIndex) {
                            slideOutVertically(animationSpec = tween(navAnimationDuration)) { it } +
                                    fadeOut(animationSpec = tween(navAnimationDuration))
                        } else {
                            slideOutVertically(animationSpec = tween(navAnimationDuration)) {
                                -it
                            } + fadeOut(animationSpec = tween(navAnimationDuration))
                        }
                    }
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                            navController,
                            enableVersionCheck = enableVersionCheck,
                            animationSpeed = animationSpeed,
                            accountViewModel = accountViewModel
                    )
                }
                composable(Screen.Version.route) { VersionScreen(navController, animationSpeed) }
                composable(Screen.Download.route) {
                    DownloadScreen(navController = navController)
                }
                composable(
                        "version_detail/{versionId}",
                        arguments = listOf(navArgument("versionId") { type = NavType.StringType })
                ) { VersionDetailScreen(navController, it.arguments?.getString("versionId")) }
                composable(Screen.Online.route) { OnlineScreen() }
                composable(
                        route = Screen.Account.route + "?code={code}",
                        arguments =
                                listOf(
                                        navArgument("code") {
                                            type = NavType.StringType
                                            nullable = true
                                        }
                                ),
                        deepLinks =
                                listOf(
                                        navDeepLink {
                                            uriPattern =
                                                    "shardlauncher://auth/microsoft?code={code}"
                                        }
                                )
                ) { backStackEntry ->
                    AccountScreen(
                            navController = navController,
                            accountViewModel = accountViewModel
                    )
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                            navController = navController,
                            isDarkTheme = isDarkTheme,
                            onThemeToggle = onThemeToggle,
                            sidebarPosition = sidebarPosition,
                            onPositionChange = onPositionChange,
                            themeColor = themeColor,
                            onThemeColorChange = onThemeColorChange,
                            customPrimaryColor = customPrimaryColor,
                            onCustomPrimaryColorChange = onCustomPrimaryColorChange,
                            lightColorScheme = lightColorScheme,
                            darkColorScheme = darkColorScheme,
                            onLightColorSchemeChange = onLightColorSchemeChange,
                            onDarkColorSchemeChange = onDarkColorSchemeChange,
                            enableBackgroundLightEffect = enableBackgroundLightEffect,
                            onEnableBackgroundLightEffectChange =
                                     onEnableBackgroundLightEffectChange,
                            enableBackgroundLightEffectCustomColor =
                                    enableBackgroundLightEffectCustomColor,
                            onEnableBackgroundLightEffectCustomColorChange =
                                    onEnableBackgroundLightEffectCustomColorChange,
                            backgroundLightEffectCustomColor = backgroundLightEffectCustomColor,
                            onBackgroundLightEffectCustomColorChange =
                                    onBackgroundLightEffectCustomColorChange,
                            animationSpeed = animationSpeed,
                            onAnimationSpeedChange = onAnimationSpeedChange,
                            lightEffectAnimationSpeed = lightEffectAnimationSpeed,
                            onLightEffectAnimationSpeedChange = onLightEffectAnimationSpeedChange,
                            launcherBackgroundUri = launcherBackgroundUri,
                            onLauncherBackgroundUriChange = onLauncherBackgroundUriChange,
                            launcherBackgroundBlur = launcherBackgroundBlur,
                            onLauncherBackgroundBlurChange = onLauncherBackgroundBlurChange,
                            launcherBackgroundBrightness = launcherBackgroundBrightness,
                            onLauncherBackgroundBrightnessChange =
                                    onLauncherBackgroundBrightnessChange,
                            enableParallax = enableParallax,
                            onEnableParallaxChange = onEnableParallaxChange,
                            parallaxMagnitude = parallaxMagnitude,
                            onParallaxMagnitudeChange = onParallaxMagnitudeChange,
                            enableVersionCheck = enableVersionCheck,
                            onEnableVersionCheckChange = onEnableVersionCheckChange,
                            uiScale = uiScale,
                            onUiScaleChange = onUiScaleChange,
                            isGlowEffectEnabled = isGlowEffectEnabled,
                            onIsGlowEffectEnabledChange = onIsGlowEffectEnabledChange,
                            onIsCardBlurEnabledChange = onIsCardBlurEnabledChange,
                            onCardAlphaChange = onCardAlphaChange,
                            isMusicPlayerEnabled = isMusicPlayerEnabled,
                            onIsMusicPlayerEnabledChange = onIsMusicPlayerEnabledChange,
                            musicPlayerViewModel = musicPlayerViewModel
                    )
                }
                composable(Screen.DeveloperOptions.route) {
                    DeveloperOptionsScreen(navController = navController)
                }
                composable("log_viewer") { LogViewerScreen(navController = navController) }
                composable("component_demo") { ComponentDemoScreen() }
            }
        }

        if (isSidebarExpanded) {
            Box(
                    modifier =
                            Modifier.fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .clickable { onSidebarExpandToggle() }
            )
        }
    }
}

@Composable
fun SideBar(
        modifier: Modifier = Modifier,
        isExpanded: Boolean,
        onToggleExpand: () -> Unit,
        navController: NavController,
        position: SidebarPosition,
        isGlowEffectEnabled: Boolean,
        animationSpeed: Float
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val rootRoute = getRootRoute(currentRoute)

    val cardShape =
            when (position) {
                SidebarPosition.Left -> RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
                SidebarPosition.Right -> RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)
            }

    Box(
            modifier =
                    modifier.fillMaxHeight()
                            .clip(cardShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        SideBarContent(
                isExpanded,
                onToggleExpand,
                navController,
                rootRoute,
                isGlowEffectEnabled,
                animationSpeed
        )
    }
}

@Composable
private fun SideBarContent(
        isExpanded: Boolean,
        onToggleExpand: () -> Unit,
        navController: NavController,
        currentRoute: String?,
        isGlowEffectEnabled: Boolean,
        animationSpeed: Float
) {
    val notifications by NotificationManager.notifications.collectAsState()
    val hasPersistentNotifications = notifications.any { it.type != NotificationType.Temporary }

    LazyColumn(
            modifier = Modifier.fillMaxSize().padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            ExpandButton(
                    isExpanded = isExpanded,
                    onClick = onToggleExpand,
                    showBadge = hasPersistentNotifications,
                    animationSpeed = animationSpeed
            )
        }
        items(navigationItems) { screen ->
            val isSelected = currentRoute == screen.route
            SideBarButton(
                    screen = screen,
                    isExpanded = isExpanded,
                    isSelected = isSelected,
                    onClick = {
                        if (currentRoute != screen.route) {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    isGlowEffectEnabled = isGlowEffectEnabled,
                    animationSpeed = animationSpeed
            )
        }
    }
}

@Composable
fun ExpandButton(
        isExpanded: Boolean,
        onClick: () -> Unit,
        icon: ImageVector? = null,
        showBadge: Boolean = false,
        animationSpeed: Float
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animationDuration = (150 / animationSpeed).toInt()
    val scale by
            animateFloatAsState(
                    targetValue = if (isPressed) 0.95f else 1f,
                    animationSpec = tween(durationMillis = animationDuration),
                    label = "ExpandButtonScale"
            )
    val shape = RoundedCornerShape(22.dp)

    val buttonModifier =
            if (isExpanded) {
                Modifier.fillMaxWidth().height(56.dp)
            } else {
                Modifier.size(56.dp)
            }

    Box(
            modifier =
                    buttonModifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(shape)
                            .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = onClick
                            ),
            contentAlignment = Alignment.Center
    ) {
        Crossfade(
                targetState = isExpanded,
                label = "ToggleIcon",
                animationSpec = tween(durationMillis = animationDuration)
        ) {
            Icon(
                    imageVector = icon
                                    ?: if (it) Icons.AutoMirrored.Filled.ArrowBack
                                    else Icons.Filled.Menu,
                    contentDescription = if (it) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
            )
        }
        if (showBadge) {
            Box(
                    Modifier.align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun OnlineScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("联机", style = MaterialTheme.typography.headlineLarge)
    }
}

@Composable
fun SideBarButton(
        screen: Screen,
        isExpanded: Boolean,
        isSelected: Boolean,
        onClick: () -> Unit,
        isGlowEffectEnabled: Boolean,
        animationSpeed: Float
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animationDuration = (150 / animationSpeed).toInt()
    val scale by
            animateFloatAsState(
                    targetValue = if (isPressed) 0.95f else 1f,
                    animationSpec = tween(durationMillis = animationDuration),
                    label = "SidebarButtonScale"
            )

    val backgroundColor =
            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else Color.Transparent
    val contentColor =
            if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    val shape = RoundedCornerShape(22.dp)

    val buttonModifier =
            if (isExpanded) {
                Modifier.fillMaxWidth().height(56.dp)
            } else {
                Modifier.size(56.dp)
            }

    Box(
            modifier =
                    Modifier.then(buttonModifier)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(shape)
                            .background(backgroundColor, shape)
                            .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = onClick
                            ),
            contentAlignment = Alignment.Center
    ) {
        Row(
                modifier = Modifier.padding(horizontal = if (isExpanded) 14.dp else 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (isExpanded) Arrangement.Start else Arrangement.Center
        ) {
            Icon(
                    imageVector = screen.icon,
                    contentDescription = screen.label,
                    tint = contentColor,
                    modifier =
                            Modifier.size(28.dp)
                                    .glow(
                                            color = MaterialTheme.colorScheme.primary,
                                            enabled = isGlowEffectEnabled && isSelected,
                                            cornerRadius = 22.dp,
                                            blurRadius = 12.dp
                                    )
            )
            if (isExpanded) {
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                        text = screen.label,
                        color = contentColor,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1
                )
            }
        }
    }
}
