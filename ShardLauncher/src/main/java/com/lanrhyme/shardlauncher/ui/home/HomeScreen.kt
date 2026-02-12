package com.lanrhyme.shardlauncher.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Download
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.lanrhyme.shardlauncher.api.ApiClient
import com.lanrhyme.shardlauncher.data.SettingsRepository
import com.lanrhyme.shardlauncher.game.account.Account
import com.lanrhyme.shardlauncher.model.LatestVersionsResponse
import com.lanrhyme.shardlauncher.model.VersionInfo
import com.lanrhyme.shardlauncher.ui.account.AccountViewModel
import com.lanrhyme.shardlauncher.ui.components.basic.*
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
import com.lanrhyme.shardlauncher.ui.xaml.XamlRenderer
import com.lanrhyme.shardlauncher.ui.xaml.parseXaml
import com.lanrhyme.shardlauncher.ui.navigation.Screen
import com.lanrhyme.shardlauncher.utils.Logger
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import com.lanrhyme.shardlauncher.game.launch.GameLaunchManager
import com.lanrhyme.shardlauncher.game.version.installed.VersionsManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
        navController: NavController,
        enableVersionCheck: Boolean,
        animationSpeed: Float,
        accountViewModel: AccountViewModel = viewModel()
) {
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }
    val xamlContent = remember { loadXaml(context, "home.xaml") }
    val nodes = parseXaml(xamlContent)
    var latestVersions by remember { mutableStateOf<LatestVersionsResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val selectedAccount by accountViewModel.selectedAccount.collectAsState()
    val cardLayoutConfig = LocalCardLayoutConfig.current
    val coroutineScope = rememberCoroutineScope()
    val currentGamePath = com.lanrhyme.shardlauncher.game.path.GamePathManager.currentPath

    // Get installed versions and current version
    val installedVersions = VersionsManager.versions
    val currentVersion by VersionsManager.currentVersion.collectAsState()
    var selectedVersionForLaunch by remember { mutableStateOf(currentVersion) }
    
    // Update selected version when current version changes
    LaunchedEffect(currentVersion) {
        if (selectedVersionForLaunch == null) {
            selectedVersionForLaunch = currentVersion
        }
    }
    
    // Refresh versions when game path changes
    LaunchedEffect(currentGamePath) {
        VersionsManager.refresh("HomeScreen_GamePathChanged")
    }

    val animatedSpeed by
            animateFloatAsState(
                    targetValue = animationSpeed,
                    animationSpec = tween((1000 / animationSpeed).toInt())
            )

    if (enableVersionCheck) {
        LaunchedEffect(Unit) {
            Logger.log(context, "HomeScreen", "Version check enabled. Starting polling.")
            var backoffDelay = 60 * 1000L // 1 minute initial backoff
            val maxBackoffDelay = 60 * 60 * 1000L // 1 hour
            val normalPollInterval = 60 * 60 * 1000L // 1 hour

            while (true) {
                var nextDelay = normalPollInterval
                try {
                    Logger.log(context, "HomeScreen", "Fetching latest versions...")
                    errorMessage = null // Clear previous error
                    val response = ApiClient.versionApiService.getLatestVersions()
                    latestVersions = response
                    Logger.log(
                            context,
                            "HomeScreen",
                            "Successfully fetched latest versions: $response"
                    )
                    // On success, reset backoff delay
                    backoffDelay = 60 * 1000L
                } catch (e: Exception) {
                    e.printStackTrace()
                    val errorText = "获取版本信息失败: ${e.message}"
                    errorMessage = errorText
                    Logger.log(context, "HomeScreen", errorText)

                    // On failure, use the current backoff delay for the next attempt
                    nextDelay = backoffDelay
                    // Increase backoff for the *next* failure
                    backoffDelay = (backoffDelay * 2).coerceAtMost(maxBackoffDelay)
                    Logger.log(
                            context,
                            "HomeScreen",
                            "Request failed. Retrying in ${nextDelay / 1000} seconds."
                    )
                }
                delay(nextDelay)
            }
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(0.72f)) {
            LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(24.dp)
            ) {
                item {
                    ShardSectionHeader(
                        title = "欢迎使用 Shard Launcher",
                        modifier = Modifier.animatedAppearance(0, animatedSpeed)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ShardGlassCard(
                            modifier = Modifier.animatedAppearance(1, animatedSpeed),
                    ) {
                        XamlRenderer(nodes = nodes, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
                if (enableVersionCheck) {
                    item {
                        ShardSectionHeader(
                            title = "Minecraft 更新动态",
                            modifier = Modifier.animatedAppearance(2, animatedSpeed)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        when {
                            errorMessage != null -> {
                                ShardGlassCard(modifier = Modifier.animatedAppearance(3, animatedSpeed)) {
                                    Text(text = errorMessage!!, modifier = Modifier.padding(16.dp))
                                }
                            }
                            latestVersions != null -> {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    latestVersions!!.release.let { release ->
                                        VersionInfoCard(
                                            versionInfo = release,
                                            modifier = Modifier.animatedAppearance(3, animatedSpeed)
                                        )
                                    }
                                    latestVersions!!.snapshot?.let { snapshot ->
                                        VersionInfoCard(
                                            versionInfo = snapshot,
                                            modifier = Modifier.animatedAppearance(4, animatedSpeed)
                                        )
                                    }
                                }
                            }
                            else -> {
                                ShardGlassCard(modifier = Modifier.animatedAppearance(3, animatedSpeed)) {
                                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        VerticalDivider()

        Box(modifier = Modifier.weight(0.28f).fillMaxHeight()) {
            Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier =
                                Modifier.clickable { navController.navigate(Screen.Account.route) }
                ) {
                    HomeAccountCard(
                            account = selectedAccount
                                            ?: Account(
                                                    uniqueUUID = "",
                                                    username = "选择账户档案",
                                                    accountType = null
                                            ),
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.animatedAppearance(6, animatedSpeed)
                ) {
                    // Version Selector
                    VersionSelector(
                        selectedVersion = selectedVersionForLaunch,
                        versions = installedVersions,
                        onVersionSelected = { version ->
                            selectedVersionForLaunch = version
                            VersionsManager.saveCurrentVersion(version.getVersionName())
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))

                    // Launch Button - Height increased for prominence
                    ScalingActionButton(
                        onClick = { 
                            selectedVersionForLaunch?.let { version ->
                                selectedAccount?.let { account ->
                                    coroutineScope.launch {
                                        try {
                                            GameLaunchManager.launchGame(
                                                activity = context as android.app.Activity,
                                                version = version,
                                                account = account,
                                                onExit = { code, isSignal ->
                                                    Logger.log(context, "HomeScreen", "Game exited with code: $code")
                                                }
                                            )
                                        } catch (e: Exception) {
                                            Logger.log(context, "HomeScreen", "Launch failed: ${e.message}")
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        text = if (selectedVersionForLaunch != null && selectedAccount != null) "启动游戏" else "未准备就绪",
                        enabled = selectedVersionForLaunch != null && selectedAccount != null
                    )
                }
            }
        }
    }
}

@Composable
fun VersionInfoCard(versionInfo: VersionInfo, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    ShardGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column {
            AsyncImage(
                    model = versionInfo.versionImageLink,
                    contentDescription = versionInfo.title,
                    modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                ) {
                    Column(Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = versionInfo.title, 
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        versionInfo.intro?.let { intro ->
                            Text(
                                text = intro,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    ShardTag(
                        text = versionInfo.versionType,
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ScalingActionButton(
                            onClick = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(versionInfo.officialLink)))
                            },
                            icon = Icons.AutoMirrored.Filled.Article,
                            text = "官方日志",
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                    )
                    ScalingActionButton(
                            onClick = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(versionInfo.wikiLink)))
                            },
                            icon = Icons.Default.Book,
                            text = "Wiki",
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                    )
                    ScalingActionButton(
                            onClick = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(versionInfo.serverJar)))
                            },
                            icon = Icons.Default.Download,
                            text = "服务端",
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

fun loadXaml(context: Context, fileName: String): String {
    val homesDir = File(context.getExternalFilesDir(null), ".shardlauncher/homes")
    if (!homesDir.exists()) {
        homesDir.mkdirs()
    }
    val externalFile = File(homesDir, fileName)

    if (externalFile.exists()) {
        return try {
            FileInputStream(externalFile).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }

    return try {
        context.assets.open(fileName).use { inputStream ->
            FileOutputStream(externalFile).use { outputStream -> inputStream.copyTo(outputStream) }
            // Now read the copied file
            FileInputStream(externalFile).bufferedReader().use { it.readText() }
        }
    } catch (e: IOException) {
        e.printStackTrace()
        ""
    }
}

@Composable
fun VerticalDivider() {
    val dividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    Canvas(modifier = Modifier.fillMaxHeight().width(1.dp)) {
        val brush =
                Brush.verticalGradient(
                        colors = listOf(Color.Transparent, dividerColor, Color.Transparent),
                        startY = 0f,
                        endY = size.height
                )
        drawLine(
                brush = brush,
                start = Offset(x = 0f, y = 0f),
                end = Offset(x = 0f, y = size.height),
                strokeWidth = 1.dp.toPx()
        )
    }
}

