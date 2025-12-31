package com.lanrhyme.shardlauncher.ui.downloads

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.lanrhyme.shardlauncher.ui.components.SegmentedNavigationBar

@Composable
fun DownloadScreen(navController: NavController) {
    var selectedPage by remember { mutableStateOf(DownloadPage.Game) }

    Column(modifier = Modifier.fillMaxSize()) {
        SegmentedNavigationBar(
                title = "下载",
                selectedPage = selectedPage,
                onPageSelected = { selectedPage = it },
                pages = DownloadPage.entries,
                getTitle = { it.title }
        )

        when (selectedPage) {
            DownloadPage.Game -> GameDownloadContent(navController)
            DownloadPage.Mod -> ModDownloadContent()
            DownloadPage.Modpack -> ModpackDownloadContent()
        }
    }
}

@Composable
fun ModDownloadContent() {
    // Placeholder
}

@Composable
fun ModpackDownloadContent() {
    // Placeholder
}

enum class DownloadPage(val title: String) {
    Game("游戏"),
    Mod("模组"),
    Modpack("整合包")
}
