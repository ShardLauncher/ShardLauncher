package com.lanrhyme.shardlauncher.ui.components.business

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lanrhyme.shardlauncher.model.BmclapiManifest

/**
 * 版本列表项组件
 *
 * @param version 版本信息对象
 * @param onClick 点击时的回调
 */
@Composable
fun VersionItem(
    version: BmclapiManifest.Version,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = version.id, style = MaterialTheme.typography.titleMedium)
            Text(text = "Type: ${version.type}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Released: ${version.releaseTime}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
