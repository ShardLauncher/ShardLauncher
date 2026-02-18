package com.lanrhyme.shardlauncher.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.lanrhyme.shardlauncher.game.launch.GameLaunchManager
import com.lanrhyme.shardlauncher.game.version.installed.VersionsManager
import com.lanrhyme.shardlauncher.model.LatestVersionsResponse
import com.lanrhyme.shardlauncher.model.VersionInfo
import com.lanrhyme.shardlauncher.ui.account.AccountViewModel
import com.lanrhyme.shardlauncher.ui.components.basic.ButtonSize
import com.lanrhyme.shardlauncher.ui.components.basic.ButtonType
import com.lanrhyme.shardlauncher.ui.components.basic.CardStyle
import com.lanrhyme.shardlauncher.ui.components.basic.ShardButton
import com.lanrhyme.shardlauncher.ui.components.basic.ShardCard
import com.lanrhyme.shardlauncher.ui.components.basic.ShardTag
import com.lanrhyme.shardlauncher.ui.components.basic.animatedAppearance
import com.lanrhyme.shardlauncher.ui.components.layout.PageLazyColumn
import com.lanrhyme.shardlauncher.ui.components.layout.PageStateContainer
import com.lanrhyme.shardlauncher.ui.components.layout.PageTwoColumnLayout
import com.lanrhyme.shardlauncher.ui.navigation.Screen
import com.lanrhyme.shardlauncher.ui.xaml.XamlRenderer
import com.lanrhyme.shardlauncher.ui.xaml.parseXaml
import com.lanrhyme.shardlauncher.ui.xaml.model.XamlNode
import com.lanrhyme.shardlauncher.utils.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

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

    val animatedSpeed by animateFloatAsState(
        targetValue = animationSpeed,
        animationSpec = tween((1000 / animationSpeed).toInt())
    )

    // Version check polling
    if (enableVersionCheck) {
        LaunchedEffect(Unit) {
            Logger.log(context, "HomeScreen", "Version check enabled. Starting polling.")
            var backoffDelay = 60 * 1000L
            val maxBackoffDelay = 60 * 60 * 1000L
            val normalPollInterval = 60 * 60 * 1000L

            while (true) {
                var nextDelay = normalPollInterval
                try {
                    Logger.log(context, "HomeScreen", "Fetching latest versions...")
                    errorMessage = null
                    val response = ApiClient.versionApiService.getLatestVersions()
                    latestVersions = response
                    Logger.log(context, "HomeScreen", "Successfully fetched latest versions: $response")
                    backoffDelay = 60 * 1000L
                } catch (e: Exception) {
                    e.printStackTrace()
                    val errorText = "获取版本信息失败: ${e.message}"
                    errorMessage = errorText
                    Logger.log(context, "HomeScreen", errorText)
                    nextDelay = backoffDelay
                    backoffDelay = (backoffDelay * 2).coerceAtMost(maxBackoffDelay)
                    Logger.log(context, "HomeScreen", "Request failed. Retrying in ${nextDelay / 1000} seconds.")
                }
                delay(nextDelay)
            }
        }
    }

    PageTwoColumnLayout(
        leftWeight = 0.72f,
        rightWeight = 0.28f,
        leftContent = {
            LeftPanel(
                nodes = nodes,
                enableVersionCheck = enableVersionCheck,
                errorMessage = errorMessage,
                latestVersions = latestVersions,
                animatedSpeed = animatedSpeed
            )
        },
        rightContent = {
            RightPanel(
                selectedAccount = selectedAccount,
                selectedVersionForLaunch = selectedVersionForLaunch,
                installedVersions = installedVersions,
                onAccountClick = { navController.navigate(Screen.Account.route) },
                onVersionSelected = { version ->
                    selectedVersionForLaunch = version
                    VersionsManager.saveCurrentVersion(version.getVersionName())
                },
                onLaunchGame = {
                    selectedVersionForLaunch?.let { version ->
                        navController.navigate("game/${version.getVersionName()}")
                    }
                },
                animatedSpeed = animatedSpeed
            )
        }
    )
}

@Composable
private fun LeftPanel(
    nodes: List<XamlNode>,
    enableVersionCheck: Boolean,
    errorMessage: String?,
    latestVersions: LatestVersionsResponse?,
    animatedSpeed: Float
) {
    PageLazyColumn(
        contentPadding = PaddingValues(16.dp),
        showScrollIndicator = false
    ) {
        // Welcome Card
        item {
            WelcomeCard(nodes = nodes, animatedSpeed = animatedSpeed)
        }

        // Version Check Content
        if (enableVersionCheck) {
            item {
                VersionCheckContent(
                    errorMessage = errorMessage,
                    latestVersions = latestVersions,
                    animatedSpeed = animatedSpeed
                )
            }
        }
    }
}

@Composable
private fun WelcomeCard(
    nodes: List<XamlNode>,
    animatedSpeed: Float
) {
    ShardCard(
        modifier = Modifier.animatedAppearance(1, animatedSpeed),
        style = CardStyle.GLASS
    ) {
        Text(
            text = "欢迎回来！",
            style = MaterialTheme.typography.titleMedium
        )
        XamlRenderer(nodes = nodes, modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
private fun VersionCheckContent(
    errorMessage: String?,
    latestVersions: LatestVersionsResponse?,
    animatedSpeed: Float
) {
    when {
        errorMessage != null -> {
            ShardCard(
                modifier = Modifier.animatedAppearance(3, animatedSpeed),
                style = CardStyle.GLASS
            ) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        latestVersions != null -> {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ShardCard(
                    modifier = Modifier.animatedAppearance(2, animatedSpeed),
                    style = CardStyle.GLASS
                ) {
                    Text(
                        text = "Minecraft 最新动态",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    latestVersions.release.let { release ->
                        VersionInfoCard(
                            versionInfo = release,
                            modifier = Modifier.animatedAppearance(3, animatedSpeed)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    latestVersions.snapshot?.let { snapshot ->
                        VersionInfoCard(
                            versionInfo = snapshot,
                            modifier = Modifier.animatedAppearance(4, animatedSpeed)
                        )
                    }
                }
            }
        }
        else -> {
            ShardCard(
                modifier = Modifier.animatedAppearance(3, animatedSpeed),
                style = CardStyle.GLASS
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun RightPanel(
    selectedAccount: Account?,
    selectedVersionForLaunch: com.lanrhyme.shardlauncher.game.version.installed.Version?,
    installedVersions: List<com.lanrhyme.shardlauncher.game.version.installed.Version>,
    onAccountClick: () -> Unit,
    onVersionSelected: (com.lanrhyme.shardlauncher.game.version.installed.Version) -> Unit,
    onLaunchGame: () -> Unit,
    animatedSpeed: Float
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Account Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { onAccountClick() }
        ) {
            HomeAccountCard(
                account = selectedAccount ?: Account(
                    uniqueUUID = "",
                    username = "选择账户档案",
                    accountType = null
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Launch Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.animatedAppearance(6, animatedSpeed)
        ) {
            // Version Selector
            VersionSelector(
                selectedVersion = selectedVersionForLaunch,
                versions = installedVersions,
                onVersionSelected = onVersionSelected,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Launch Button
            ShardButton(
                onClick = onLaunchGame,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                type = ButtonType.GRADIENT,
                size = ButtonSize.LARGE,
                enabled = selectedVersionForLaunch != null && selectedAccount != null
            ) {
                Text(
                    text = if (selectedVersionForLaunch != null && selectedAccount != null) "启动游戏" else "未准备就绪",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun VersionInfoCard(versionInfo: VersionInfo, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    ShardCard(
        modifier = modifier.fillMaxWidth().alpha(0.5f),
        shape = RoundedCornerShape(24.dp),
        style = CardStyle.GLASS,
        border = true
    ) {
        AsyncImage(
            model = versionInfo.versionImageLink,
            contentDescription = versionInfo.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
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
                VersionActionButton(
                    icon = Icons.AutoMirrored.Filled.Article,
                    text = "官方日志",
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(versionInfo.officialLink))
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                VersionActionButton(
                    icon = Icons.Default.Book,
                    text = "Wiki",
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(versionInfo.wikiLink))
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                VersionActionButton(
                    icon = Icons.Default.Download,
                    text = "服务端",
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(versionInfo.serverJar))
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun VersionActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ShardButton(
        onClick = onClick,
        modifier = modifier,
        type = ButtonType.GRADIENT,
        size = ButtonSize.SMALL,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
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
            FileOutputStream(externalFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
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
        val brush = Brush.verticalGradient(
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
