package com.lanrhyme.shardlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lanrhyme.shardlauncher.model.FabricLoaderVersion
import com.lanrhyme.shardlauncher.model.LoaderVersion
import com.lanrhyme.shardlauncher.model.ModrinthVersion
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.unit.Constraints

@Composable
fun DisableIntrinsicMeasurements(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier,
        measurePolicy = object : MeasurePolicy {
            override fun MeasureScope.measure(
                measurables: List<Measurable>,
                constraints: Constraints
            ): MeasureResult {
                val placeables = measurables.map { it.measure(constraints) }
                val maxWidth = placeables.maxOfOrNull { it.width } ?: 0
                val maxHeight = placeables.maxOfOrNull { it.height } ?: 0
                return layout(maxWidth, maxHeight) {
                    placeables.forEach { it.place(0, 0) }
                }
            }

            override fun IntrinsicMeasureScope.minIntrinsicWidth(
                measurables: List<IntrinsicMeasurable>,
                height: Int
            ): Int = 0

            override fun IntrinsicMeasureScope.minIntrinsicHeight(
                measurables: List<IntrinsicMeasurable>,
                width: Int
            ): Int = 0

            override fun IntrinsicMeasureScope.maxIntrinsicWidth(
                measurables: List<IntrinsicMeasurable>,
                height: Int
            ): Int = 0

            override fun IntrinsicMeasureScope.maxIntrinsicHeight(
                measurables: List<IntrinsicMeasurable>,
                width: Int
            ): Int = 0
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> LoaderVersionDropdown(
    versions: List<T>,
    selectedVersion: T?,
    onVersionSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var page by remember { mutableIntStateOf(0) }
    val pageSize = 10

    LaunchedEffect(versions) {
        page = 0
    }

    val totalPages = (versions.size + pageSize - 1) / pageSize
    // 确保 page 在有效范围内 (防止 versions 变空或变短导致越界)
    val safePage = page.coerceIn(0, maxOf(0, totalPages - 1))
    val displayVersions = versions.drop(safePage * pageSize).take(pageSize)

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
            ShardDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize()
            ) {
                // 使用 LazyColumn 实现虚拟化列表，解决大量数据时的卡顿问题
                // 限制高度以确保在下拉菜单中正确显示
                // 使用 DisableIntrinsicMeasurements 包裹 LazyColumn 以避免在 DropdownMenu 中崩溃
                DisableIntrinsicMeasurements {
                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 300.dp)
                            .fillMaxWidth() // 确保 LazyColumn 填满宽度
                    ) {
                        items(versions) { version ->
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
    }
}

@Composable
private fun <T> VersionDropdownItem(version: T) {
    when (version) {
        is ModrinthVersion -> {
             Column {
                Text(version.versionNumber, style = MaterialTheme.typography.bodyMedium)
                Text(version.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
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