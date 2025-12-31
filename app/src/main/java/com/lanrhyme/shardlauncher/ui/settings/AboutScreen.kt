package com.lanrhyme.shardlauncher.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import com.lanrhyme.shardlauncher.ui.components.ScrollIndicator
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.HdrWeak
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.lanrhyme.shardlauncher.R
import com.lanrhyme.shardlauncher.ui.components.CombinedCard
import com.lanrhyme.shardlauncher.ui.components.LocalCardLayoutConfig
import com.lanrhyme.shardlauncher.ui.components.ScalingActionButton
import com.lanrhyme.shardlauncher.ui.components.TitleAndSummary
import com.lanrhyme.shardlauncher.ui.components.animatedAppearance

data class OssLibrary(val name: String, val author: String, val url: String, val license: String)

@Composable
fun AboutScreen(animationSpeed: Float) {
        val cardLayoutConfig = LocalCardLayoutConfig.current
        val isCardBlurEnabled = cardLayoutConfig.isCardBlurEnabled
        val cardAlpha = cardLayoutConfig.cardAlpha
        val hazeState = cardLayoutConfig.hazeState
        val context = LocalContext.current
        var showLicensesDialog by remember { mutableStateOf(false) }

        if (showLicensesDialog) {
                LicensesDialog { showLicensesDialog = false }
        }

        Row(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
                val listState = rememberLazyListState()
                Box(modifier = Modifier.weight(0.65f)) {
                        LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                        item {
                                CombinedCard(
                                        modifier = Modifier.animatedAppearance(1, animationSpeed),
                                        title = "关于",
                                        summary = "关于ShardLauncher"
                                ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                                Row(
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Image(
                                                                painter =
                                                                        painterResource(
                                                                                id =
                                                                                        R.drawable
                                                                                                .img_kotlin
                                                                        ),
                                                                contentDescription =
                                                                        "Launcher Title Image",
                                                                modifier = Modifier.size(200.dp)
                                                        )
                                                        Spacer(Modifier.width(16.dp))
                                                        Text(
                                                                "一款使用Kotlin和JetPack Compose开发的安卓Minecraft:Java Edition启动器"
                                                        )
                                                }
                                                Spacer(Modifier.padding(8.dp))
                                                Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(8.dp)
                                                ) {
                                                        ScalingActionButton(
                                                                onClick = { /*TODO*/},
                                                                modifier = Modifier.weight(1f),
                                                                icon =
                                                                        Icons.AutoMirrored.Filled
                                                                                .Article,
                                                                text = "文档"
                                                        )
                                                        ScalingActionButton(
                                                                onClick = {
                                                                        val intent =
                                                                                Intent(
                                                                                        Intent.ACTION_VIEW,
                                                                                        Uri.parse(
                                                                                                "https://github.com/LanRhyme/ShardLauncher"
                                                                                        )
                                                                                )
                                                                        context.startActivity(
                                                                                intent
                                                                        )
                                                                },
                                                                modifier = Modifier.weight(1f),
                                                                icon = Icons.Default.Code,
                                                                text = "Github"
                                                        )
                                                }
                                        }
                                }
                        }
                        item {
                                CreditsCard(
                                        modifier = Modifier.animatedAppearance(2, animationSpeed),
                                        title = "贡献者",
                                        summary = "感谢各位对本项目的贡献",
                                        items =
                                                listOf(
                                                        CreditItem(
                                                                image = R.drawable.img_lanrhyme,
                                                                title = "LanRhyme",
                                                                summary = "项目发起者，主要开发者",
                                                                actions =
                                                                        listOf(
                                                                                CreditAction(
                                                                                        icon =
                                                                                                Icons.Default
                                                                                                        .Code,
                                                                                        text =
                                                                                                "Github",
                                                                                        url =
                                                                                                "https://github.com/LanRhyme"
                                                                                ),
                                                                                CreditAction(
                                                                                        icon =
                                                                                                Icons.Filled
                                                                                                        .HdrWeak,
                                                                                        text =
                                                                                                "个人站点",
                                                                                        url =
                                                                                                "https://lanrhyme.netlify.app/"
                                                                                )
                                                                        )
                                                        )
                                                )
                                )
                        }
                        item {
                                CreditsCard(
                                        modifier = Modifier.animatedAppearance(3, animationSpeed),
                                        title = "鸣谢",
                                        summary = "对本项目有帮助的开源项目",
                                        items =
                                                listOf(
                                                        CreditItem(
                                                                image =
                                                                        R.drawable
                                                                                .img_zalithlauncher,
                                                                title = "ZalithLauncher2",
                                                                summary =
                                                                        "参考和引用了ZalithLauncher2的部分代码",
                                                                actions =
                                                                        listOf(
                                                                                CreditAction(
                                                                                        icon =
                                                                                                Icons.Default
                                                                                                        .Code,
                                                                                        text =
                                                                                                "Github",
                                                                                        url =
                                                                                                "https://github.com/ZalithLauncher/ZalithLauncher2"
                                                                                )
                                                                        )
                                                        )
                                                )
                                )
                        }
                        item {
                                CreditsCard(
                                        modifier = Modifier.animatedAppearance(4, animationSpeed),
                                        title = "第三方API",
                                        summary = "本启动器所使用的第三方API",
                                        items =
                                                listOf(
                                                        CreditItem(
                                                                title = "BMCLAPI",
                                                                summary = "提供Minecraft版本和资源下载服务",
                                                                actions =
                                                                        listOf(
                                                                                CreditAction(
                                                                                        icon =
                                                                                                Icons.Filled
                                                                                                        .Link,
                                                                                        url =
                                                                                                "https://bmclapidoc.bangbang93.com/"
                                                                                )
                                                                        )
                                                        ),
                                                        CreditItem(
                                                                title = "新闻主页",
                                                                summary = "启动器主页中的Minecraft更新卡片",
                                                                actions =
                                                                        listOf(
                                                                                CreditAction(
                                                                                        icon =
                                                                                                Icons.Filled
                                                                                                        .Link,
                                                                                        url =
                                                                                                "https://news.bugjump.net/static/"
                                                                                )
                                                                        )
                                                        ),
                                                        CreditItem(
                                                                title = "星之阁API",
                                                                summary = "提供皮肤模型图的获取服务",
                                                                actions =
                                                                        listOf(
                                                                                CreditAction(
                                                                                        icon =
                                                                                                Icons.Filled
                                                                                                        .Link,
                                                                                        url =
                                                                                                "https://api.xingzhige.com/doc/35"
                                                                                )
                                                                        )
                                                        ),
                                                        CreditItem(
                                                                title = "minecraft-headshot-api",
                                                                summary = "提供头像图像的获取服务",
                                                                actions =
                                                                        listOf(
                                                                                CreditAction(
                                                                                        icon =
                                                                                                Icons.Default
                                                                                                        .Code,
                                                                                        text =
                                                                                                "Github",
                                                                                        url =
                                                                                                "https://github.com/RMS-Server/minecraft-headshot-api"
                                                                                )
                                                                        )
                                                        ),
                                                        CreditItem(
                                                                title = "crafatar.com",
                                                                summary = "用于皮肤和头像的获取服务",
                                                                actions =
                                                                        listOf(
                                                                                CreditAction(
                                                                                        icon =
                                                                                                Icons.Filled
                                                                                                        .Link,
                                                                                        url =
                                                                                                "https://crafatar.com/"
                                                                                )
                                                                        )
                                                        )
                                                )
                                )
                        }
                        item {
                                CombinedCard(
                                        modifier = Modifier.animatedAppearance(5, animationSpeed),
                                        title = "开源许可"
                                ) {
                                        Column(
                                                modifier = Modifier.padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                                Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically,
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(16.dp)
                                                ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                        text = "本应用使用了许多优秀的开源库来构建",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .bodySmall
                                                                )
                                                        }
                                                        Row(
                                                                horizontalArrangement =
                                                                        Arrangement.spacedBy(8.dp)
                                                        ) {
                                                                ScalingActionButton(
                                                                        onClick = {
                                                                                showLicensesDialog =
                                                                                        true
                                                                        },
                                                                        icon =
                                                                                Icons.AutoMirrored
                                                                                        .Filled
                                                                                        .Article,
                                                                        text = "查看许可",
                                                                        contentPadding =
                                                                                PaddingValues(
                                                                                        horizontal =
                                                                                                12.dp,
                                                                                        vertical =
                                                                                                8.dp
                                                                                )
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }
                        item { Spacer(modifier = Modifier.height(45.dp)) }
                        }
                        ScrollIndicator(
                                listState = listState,
                                modifier = Modifier.align(Alignment.CenterEnd)
                        )
                }

                Column(modifier = Modifier.weight(0.35f)) {
                        CombinedCard(
                                modifier = Modifier.animatedAppearance(1, animationSpeed),
                                title = "版本信息"
                        ) {
                                Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                Text(
                                                        buildAnnotatedString {
                                                                withStyle(
                                                                        style =
                                                                                SpanStyle(
                                                                                        fontWeight =
                                                                                                FontWeight
                                                                                                        .Bold
                                                                                )
                                                                ) { append("版本: ") }
                                                                append(
                                                                        stringResource(
                                                                                id =
                                                                                        R.string
                                                                                                .version_name
                                                                        )
                                                                )
                                                        }
                                                )
                                                Spacer(Modifier.weight(1f))
                                                Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = MaterialTheme.colorScheme.primary
                                                ) {
                                                        Text(
                                                                text =
                                                                        stringResource(
                                                                                id =
                                                                                        R.string
                                                                                                .git_hash
                                                                        ),
                                                                modifier =
                                                                        Modifier.padding(
                                                                                horizontal = 8.dp,
                                                                                vertical = 4.dp
                                                                        ),
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onPrimary,
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodySmall,
                                                        )
                                                }
                                        }
                                        Text(
                                                buildAnnotatedString {
                                                        withStyle(
                                                                style =
                                                                        SpanStyle(
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Bold
                                                                        )
                                                        ) { append("分支: ") }
                                                        append(
                                                                stringResource(
                                                                        id = R.string.git_branch
                                                                )
                                                        )
                                                }
                                        )
                                        Text(
                                                buildAnnotatedString {
                                                        withStyle(
                                                                style =
                                                                        SpanStyle(
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Bold
                                                                        )
                                                        ) { append("版本状态: ") }
                                                        append(
                                                                stringResource(
                                                                        id = R.string.build_status
                                                                )
                                                        )
                                                }
                                        )
                                        Text(
                                                buildAnnotatedString {
                                                        withStyle(
                                                                style =
                                                                        SpanStyle(
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Bold
                                                                        )
                                                        ) { append("上次更新时间: ") }
                                                        append(
                                                                stringResource(
                                                                        id =
                                                                                R.string
                                                                                        .last_update_time
                                                                )
                                                        )
                                                }
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        ScalingActionButton(
                                                onClick = { /*TODO*/},
                                                modifier = Modifier.fillMaxWidth(),
                                                icon = Icons.Filled.SystemUpdate,
                                                text = "检查更新"
                                        )
                                }
                        }
                }
        }
}

@Composable
fun LicensesDialog(onDismiss: () -> Unit) {
        val context = LocalContext.current
        val libraries = remember {
                listOf(
                        OssLibrary(
                                "Jetpack Compose",
                                "Google",
                                "https://developer.android.com/jetpack/compose",
                                "Apache License 2.0"
                        ),
                        OssLibrary(
                                "AndroidX",
                                "Google",
                                "https://source.android.com/",
                                "Apache License 2.0"
                        ),
                        OssLibrary(
                                "Coil",
                                "Coil Contributors",
                                "https://coil-kt.github.io/coil/",
                                "Apache License 2.0"
                        ),
                        OssLibrary(
                                "Retrofit",
                                "Square, Inc.",
                                "https://square.github.io/retrofit/",
                                "Apache License 2.0"
                        ),
                        OssLibrary(
                                "Gson",
                                "Google",
                                "https://github.com/google/gson",
                                "Apache License 2.0"
                        ),
                        OssLibrary(
                                "PCL2-NewsHomepage",
                                "Light-Beacon",
                                "https://github.com/Light-Beacon/PCL2-NewsHomepage",
                                "MIT License"
                        ),
                        OssLibrary(
                                "ZalithLauncher2",
                                "ZalithLauncher",
                                "https://github.com/ZalithLauncher/ZalithLauncher2",
                                "GNU GPL v3.0"
                        ),
                )
        }

        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Open Source Libraries and Licenses") },
                text = {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(libraries) { lib ->
                                        Column(
                                                modifier =
                                                        Modifier.fillMaxWidth().clickable {
                                                                val intent =
                                                                        Intent(
                                                                                Intent.ACTION_VIEW,
                                                                                Uri.parse(lib.url)
                                                                        )
                                                                context.startActivity(intent)
                                                        }
                                        ) {
                                                Text(lib.name, fontWeight = FontWeight.Bold)
                                                Text(
                                                        "by ${lib.author}",
                                                        style = MaterialTheme.typography.bodySmall
                                                )
                                                Text(
                                                        lib.license,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant
                                                )
                                        }
                                }
                        }
                },
                confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
        )
}

data class CreditAction(val icon: ImageVector, val text: String? = null, val url: String)

data class CreditItem(
        @DrawableRes val image: Int? = null,
        val title: String,
        val summary: String,
        val actions: List<CreditAction>
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CreditsCard(
        modifier: Modifier = Modifier,
        title: String,
        summary: String? = null,
        items: List<CreditItem>
) {
        val context = LocalContext.current
        CombinedCard(modifier = modifier, title = title, summary = summary) {
                Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                        items.forEach { item ->
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                        item.image?.let {
                                                Image(
                                                        painter = painterResource(id = it),
                                                        contentDescription = "${item.title} avatar",
                                                        contentScale = ContentScale.Crop,
                                                        modifier =
                                                                Modifier.size(48.dp)
                                                                        .clip(CircleShape)
                                                )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                                TitleAndSummary(item.title, item.summary)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                item.actions.forEach { action ->
                                                        ScalingActionButton(
                                                                onClick = {
                                                                        val intent =
                                                                                Intent(
                                                                                        Intent.ACTION_VIEW,
                                                                                        Uri.parse(
                                                                                                action.url
                                                                                        )
                                                                                )
                                                                        context.startActivity(
                                                                                intent
                                                                        )
                                                                },
                                                                icon = action.icon,
                                                                text = action.text,
                                                                contentPadding =
                                                                        PaddingValues(
                                                                                horizontal = 12.dp,
                                                                                vertical = 8.dp
                                                                        )
                                                        )
                                                }
                                        }
                                }
                        }
                }
        }
}
