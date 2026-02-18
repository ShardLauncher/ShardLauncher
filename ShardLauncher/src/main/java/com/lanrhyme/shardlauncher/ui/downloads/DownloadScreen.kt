package com.lanrhyme.shardlauncher.ui.downloads

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lanrhyme.shardlauncher.ui.components.basic.*
import com.lanrhyme.shardlauncher.ui.components.layout.PageContent
import com.lanrhyme.shardlauncher.ui.components.layout.PageLazyColumn

@Composable
fun DownloadScreen(navController: NavController) {
    var selectedPage by remember { mutableStateOf(DownloadPage.Game) }
    val animatedSpeed = 1.0f

    Column(modifier = Modifier.fillMaxSize()) {
        SegmentedNavigationBar(
            title = "下载中心",
            selectedPage = selectedPage,
            onPageSelected = { selectedPage = it },
            pages = DownloadPage.entries,
            getTitle = { it.title },
            modifier = Modifier.animatedAppearance(0, animatedSpeed)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .animatedAppearance(1, animatedSpeed)
        ) {
            when (selectedPage) {
                DownloadPage.Game -> GameDownloadContent(navController)
                DownloadPage.Mod -> PlaceholderContent(
                    icon = Icons.Default.Cloud,
                    title = "模组下载功能开发中",
                    subtitle = "即将支持从 CurseForge 和 Modrinth 下载",
                    primaryColor = true
                )
                DownloadPage.Modpack -> PlaceholderContent(
                    icon = Icons.Default.Cloud,
                    title = "整合包功能敬请期待",
                    subtitle = "一键下载并自动均衡各版本配置",
                    primaryColor = false
                )
            }
        }
    }
}

@Composable
private fun PlaceholderContent(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    primaryColor: Boolean
) {
    PageContent(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ShardGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(32.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = if (primaryColor) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

enum class DownloadPage(val title: String) {
    Game("游戏"),
    Mod("模组"),
    Modpack("整合包")
}