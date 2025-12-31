package com.lanrhyme.shardlauncher.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lanrhyme.shardlauncher.ui.components.LocalCardLayoutConfig
import com.lanrhyme.shardlauncher.ui.components.SimpleListLayout
import com.lanrhyme.shardlauncher.ui.navigation.Screen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Alignment
import com.lanrhyme.shardlauncher.ui.components.ScrollIndicator

@Composable
internal fun OtherSettingsContent(navController: NavController) {
    val cardLayoutConfig = LocalCardLayoutConfig.current
    val isCardBlurEnabled = cardLayoutConfig.isCardBlurEnabled
    val cardAlpha = cardLayoutConfig.cardAlpha
    val hazeState = cardLayoutConfig.hazeState
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
                    text = "高级",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        item {
            SimpleListLayout(
                    title = "开发者选项",
                    items = listOf(Unit),
                    selectedItem = Unit,
                    onValueChange = { navController.navigate(Screen.DeveloperOptions.route) },
                    getItemText = { "点击进入开发者选项" }
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
