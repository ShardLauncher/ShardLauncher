package com.lanrhyme.shardlauncher.ui.components.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lanrhyme.shardlauncher.ui.components.basic.ScrollIndicator
import com.lanrhyme.shardlauncher.ui.components.basic.TitledDivider

/**
 * 统一的页面内容容器
 * 所有页面内容区域都使用此组件作为根容器
 *
 * @param modifier 修饰符
 * @param contentPadding 内容内边距，默认为 16.dp
 * @param verticalArrangement 垂直排列方式，默认为 spacedBy(12.dp)
 * @param horizontalAlignment 水平对齐方式
 * @param content 页面内容
 */
@Composable
fun PageContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment
    ) {
        content()
    }
}

/**
 * 统一的 LazyColumn 页面容器
 * 用于需要滚动的长列表页面
 *
 * @param modifier 修饰符
 * @param state LazyListState
 * @param contentPadding 内容内边距，默认为 16.dp
 * @param verticalArrangement 垂直排列方式，默认为 spacedBy(12.dp)
 * @param showScrollIndicator 是否显示滚动指示器，默认为 true
 * @param content 列表内容
 */
@Composable
fun PageLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
    showScrollIndicator: Boolean = true,
    content: LazyListScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state,
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement
        ) {
            content()
        }

        if (showScrollIndicator) {
            ScrollIndicator(
                listState = state,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

/**
 * 页面分组容器
 * 用于将相关的内容组织在一起
 *
 * @param title 分组标题（可选）
 * @param modifier 修饰符
 * @param verticalArrangement 垂直排列方式
 * @param content 分组内容
 */
@Composable
fun PageSection(
    title: String? = null,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = verticalArrangement
    ) {
        title?.let {
            TitledDivider(title = it)
        }
        content()
    }
}

/**
 * 双栏布局页面容器
 * 用于需要左右分栏的页面（如版本管理）
 *
 * @param modifier 修饰符
 * @param leftWeight 左栏权重，默认为 0.3f
 * @param rightWeight 右栏权重，默认为 0.7f
 * @param leftContent 左栏内容
 * @param rightContent 右栏内容
 */
@Composable
fun PageTwoColumnLayout(
    modifier: Modifier = Modifier,
    leftWeight: Float = 0.3f,
    rightWeight: Float = 0.7f,
    leftContent: @Composable () -> Unit,
    rightContent: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.weight(leftWeight)) {
            leftContent()
        }
        Box(modifier = Modifier.weight(rightWeight)) {
            rightContent()
        }
    }
}

/**
 * 页面加载状态容器
 * 用于显示加载中、空状态或错误状态
 *
 * @param isLoading 是否正在加载
 * @param isEmpty 是否为空
 * @param errorMessage 错误信息（null 表示无错误）
 * @param modifier 修饰符
 * @param loadingContent 加载状态内容
 * @param emptyContent 空状态内容
 * @param errorContent 错误状态内容
 * @param content 正常内容
 */
@Composable
fun PageStateContainer(
    isLoading: Boolean,
    isEmpty: Boolean,
    errorMessage: String? = null,
    modifier: Modifier = Modifier,
    loadingContent: @Composable () -> Unit = { DefaultLoadingContent() },
    emptyContent: @Composable () -> Unit = { DefaultEmptyContent() },
    errorContent: @Composable (String) -> Unit = { DefaultErrorContent(it) },
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> loadingContent()
            errorMessage != null -> errorContent(errorMessage)
            isEmpty -> emptyContent()
            else -> content()
        }
    }
}

@Composable
private fun DefaultLoadingContent() {
    androidx.compose.material3.CircularProgressIndicator(strokeWidth = 3.dp)
}

@Composable
private fun DefaultEmptyContent() {
    androidx.compose.material3.Text(
        text = "暂无内容",
        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun DefaultErrorContent(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        androidx.compose.material3.Text(
            text = "出错了",
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            color = androidx.compose.material3.MaterialTheme.colorScheme.error
        )
        androidx.compose.material3.Text(
            text = message,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
