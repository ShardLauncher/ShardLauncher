package com.lanrhyme.shardlauncher.ui.components.business

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lanrhyme.shardlauncher.R
import com.lanrhyme.shardlauncher.model.BmclapiManifest
import com.lanrhyme.shardlauncher.ui.components.basic.ShardGlassCard
import com.lanrhyme.shardlauncher.ui.components.basic.ShardTag

/**
 * 版本列表项组件
 * 用于显示 Minecraft 版本信息
 *
 * @param version 版本信息
 * @param modifier 修饰符
 * @param onClick 点击回调
 */
@Composable
fun VersionListItem(
    version: BmclapiManifest.Version,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ShardGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_minecraft),
                contentDescription = "MinecraftIcon",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${version.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                val (versionTypeString, tagColor) = when (version.type) {
                    "release" -> "正式版" to MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    "snapshot" -> "快照版" to MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    "old_alpha", "old_beta" -> "远古版" to MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                    else -> version.type to MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShardTag(
                        text = versionTypeString,
                        containerColor = tagColor
                    )
                    Text(
                        text = version.releaseTime.substringBefore('T'),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * 版本类型标签
 *
 * @param type 版本类型
 * @param modifier 修饰符
 */
@Composable
fun VersionTypeTag(
    type: String,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (type) {
        "release" -> "正式版" to MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        "snapshot" -> "快照版" to MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
        "old_alpha", "old_beta" -> "远古版" to MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
        else -> type to MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }

    ShardTag(
        text = text,
        containerColor = color,
        modifier = modifier
    )
}
