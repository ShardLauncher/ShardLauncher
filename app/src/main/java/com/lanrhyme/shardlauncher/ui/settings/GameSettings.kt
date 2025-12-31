package com.lanrhyme.shardlauncher.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lanrhyme.shardlauncher.ui.components.LocalCardLayoutConfig
import com.lanrhyme.shardlauncher.ui.components.SwitchLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Alignment
import com.lanrhyme.shardlauncher.ui.components.animatedAppearance
import com.lanrhyme.shardlauncher.ui.components.ScrollIndicator

@Composable
fun GameSettingsContent(
        useBmclapi: Boolean,
        onUseBmclapiChange: (Boolean) -> Unit,
        animationSpeed: Float
) {
        val cardLayoutConfig = LocalCardLayoutConfig.current
        val isCardBlurEnabled = cardLayoutConfig.isCardBlurEnabled
        val cardAlpha = cardLayoutConfig.cardAlpha
        val hazeState = cardLayoutConfig.hazeState
        val listState = rememberLazyListState()

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                    item {
                            SwitchLayout(
                                    modifier = Modifier.animatedAppearance(1, animationSpeed),
                                    title = "优先使用 BMCLAPI",
                                    summary = "开启后将优先从 BMCLAPI 下载游戏版本，可以加快下载速度",
                                    checked = useBmclapi,
                                    onCheckedChange = { onUseBmclapiChange(!useBmclapi) }
                            )
                    }
            }
            ScrollIndicator(
                    listState = listState,
                    modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
}
