package com.lanrhyme.shardlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lanrhyme.shardlauncher.model.FabricLoaderVersion
import com.lanrhyme.shardlauncher.model.LoaderVersion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> LoaderVersionDropdown(
    versions: List<T>,
    selectedVersion: T?,
    onVersionSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // 如果选中版本为null且列表不为空，自动选择第一个
    val actualSelectedVersion = selectedVersion ?: versions.firstOrNull()
    
    val selectedVersionText = (when (actualSelectedVersion) {
        is FabricLoaderVersion -> actualSelectedVersion.version
        is LoaderVersion -> actualSelectedVersion.version
        is String -> actualSelectedVersion
        else -> null
    }) ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { 
            if (versions.isNotEmpty()) {
                expanded = !expanded 
            }
        }
    ) {
        CustomTextField(
            value = selectedVersionText,
            onValueChange = {},
            readOnly = true,
            label = "版本",
            trailingIcon = {
                if (versions.isNotEmpty()) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        
        // 只在有版本数据时显示下拉菜单
        if (versions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                versions.forEach { version ->
                    DropdownMenuItem(
                        text = { VersionDropdownItem(version = version) },
                        onClick = {
                            onVersionSelected(version)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> VersionDropdownItem(version: T) {
    when (version) {
        is FabricLoaderVersion -> {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(version.version, style = MaterialTheme.typography.bodyMedium)
                val status = if (version.stable == true) "Stable" else "Beta"
                val color = if (version.stable == true) Color(0xFF4CAF50) else Color(0xFFFFA000)
                Text(
                    text = status,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(color)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        is LoaderVersion -> {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(version.version, style = MaterialTheme.typography.bodyMedium)
                        version.status?.let {
                            val color = if (version.isRecommended) Color(0xFF4CAF50) else Color(0xFFFFA000)
                            Text(
                                text = it,
                                color = Color.White,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(color)
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    version.releaseTime?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        is String -> {
            Text(version, style = MaterialTheme.typography.bodyMedium)
        }
    }
}