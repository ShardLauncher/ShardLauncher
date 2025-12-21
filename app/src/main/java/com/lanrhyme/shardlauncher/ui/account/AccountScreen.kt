package com.lanrhyme.shardlauncher.ui.account

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.lanrhyme.shardlauncher.R
import com.lanrhyme.shardlauncher.game.account.Account
import com.lanrhyme.shardlauncher.ui.components.FluidFab
import com.lanrhyme.shardlauncher.ui.components.FluidFabDirection
import com.lanrhyme.shardlauncher.ui.components.FluidFabItem
import com.lanrhyme.shardlauncher.ui.components.LocalCardLayoutConfig
import com.lanrhyme.shardlauncher.ui.components.PopupContainer
import com.lanrhyme.shardlauncher.ui.theme.ShardLauncherTheme
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.launch

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AccountScreen(navController: NavController, accountViewModel: AccountViewModel = viewModel()) {
    val cardLayoutConfig = LocalCardLayoutConfig.current
    val isCardBlurEnabled = cardLayoutConfig.isCardBlurEnabled
    val cardAlpha = cardLayoutConfig.cardAlpha
    val hazeState = cardLayoutConfig.hazeState
    val accounts by accountViewModel.accounts.collectAsState()
    val selectedAccount by accountViewModel.selectedAccount.collectAsState()
    var showOfflineAccountDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<Account?>(null) }
    val microsoftLoginState by accountViewModel.microsoftLoginState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text("账户档案", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.weight(1f))
            }

            Row(modifier = Modifier.fillMaxSize()) {
                // Left side: Large card for the 3D model placeholder
                val avatarCardShape = RoundedCornerShape(22.dp)
                Card(
                    modifier =
                    Modifier.fillMaxHeight()
                        .weight(0.3f)
                        .padding(16.dp)
                        .then(
                            if (isCardBlurEnabled &&
                                Build.VERSION
                                    .SDK_INT >=
                                Build.VERSION_CODES
                                    .S
                            ) {
                                Modifier.clip(
                                    avatarCardShape
                                )
                                    .hazeEffect(
                                        state =
                                        hazeState
                                    )
                            } else Modifier
                        ),
                    shape = avatarCardShape,
                    colors =
                    CardDefaults.cardColors(
                        containerColor =
                        MaterialTheme.colorScheme.surface
                            .copy(alpha = cardAlpha)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        selectedAccount?.let { account ->
                            val localSkinFile =
                                java.io.File(
                                    LocalContext.current
                                        .filesDir,
                                    "skins/${account.profileId}.png"
                                )
                            val imageUrl =
                                if (localSkinFile.exists()) {
                                    localSkinFile
                                } else {
                                    "https://api.xingzhige.com/API/get_Minecraft_skins/?name=${account.username}&type=身体&overlay=true"
                                }

                            val imageRequest =
                                ImageRequest.Builder(
                                    LocalContext.current
                                )
                                    .data(imageUrl)
                                    .crossfade(true)
                                    .diskCachePolicy(
                                        CachePolicy.ENABLED
                                    )
                                    .memoryCachePolicy(
                                        CachePolicy.ENABLED
                                    )
                                    .build()
                            SubcomposeAsyncImage(
                                model = imageRequest,
                                contentDescription =
                                "${account.username}'s skin",
                                modifier =
                                Modifier.fillMaxSize(0.8f),
                                loading = {
                                    Box(
                                        modifier =
                                        Modifier.fillMaxSize(),
                                        contentAlignment =
                                        Alignment
                                            .Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier =
                                            Modifier.size(
                                                32.dp
                                            )
                                        )
                                    }
                                }
                            )
                        }
                            ?: run {
                                Text(
                                    text = "未选择账户",
                                    textAlign = TextAlign.Center
                                )
                            }
                    }
                }

                // Right side: Horizontally scrollable grid of account cards
                LazyHorizontalGrid(
                    rows = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxHeight().weight(0.7f),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(accounts) { account ->
                        AccountCard(
                            account = account,
                            isSelected = selectedAccount == account,
                            onClick = {
                                accountViewModel.selectAccount(
                                    account
                                )
                            },
                            onDelete = {
                                accountViewModel.deleteAccount(it)
                            },
                            onEdit = { editingAccount = it },
                            cardAlpha = cardAlpha
                        )
                    }
                }
            }
        }

        FluidFab(
            modifier =
            Modifier.align(Alignment.BottomEnd).offset(x = 170.dp, y = 150.dp),
            direction = FluidFabDirection.TOP_LEFT,
            items =
            listOf(
                FluidFabItem(
                    label = "离线账户",
                    icon = Icons.Default.Person,
                    onClick = { showOfflineAccountDialog = true }
                ),
                FluidFabItem(
                    label = "微软账户",
                    icon = Icons.Default.Cloud,
                    onClick = { accountViewModel.startMicrosoftLogin() }
                )
            ),
            sectorSize = 70f
        )
    }

    if (showOfflineAccountDialog) {
        OfflineAccountInputDialog(
            onDismiss = { showOfflineAccountDialog = false },
            onAddOfflineAccount = {
                accountViewModel.addOfflineAccount(it)
                showOfflineAccountDialog = false
            }
        )
    }

    when (val state = microsoftLoginState) {
        is MicrosoftLoginState.InProgress -> {
            val deviceCodeResponse by accountViewModel.deviceCodeData.collectAsState()
            PopupContainer(
                visible = true,
                onDismissRequest = if (deviceCodeResponse != null) {
                    { accountViewModel.cancelMicrosoftLogin() }
                } else {
                    {}
                }
            ) {
                if (deviceCodeResponse != null) {
                    val clipboardManager = LocalClipboard.current
                    LaunchedEffect(deviceCodeResponse) {
                        deviceCodeResponse?.let {
                            clipboardManager.setClipEntry(
                                ClipEntry(
                                    ClipData.newPlainText(
                                        "Microsoft Device Code",
                                        it.userCode
                                    )
                                )
                            )
                            android.widget.Toast.makeText(
                                context,
                                "代码已复制到剪贴板",
                                android.widget.Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Microsoft 登录", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("请访问以下链接并输入代码：")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            deviceCodeResponse!!
                                .verificationUri,
                            style =
                            MaterialTheme.typography
                                .bodyLarge,
                            color =
                            MaterialTheme.colorScheme
                                .primary,
                            modifier =
                            Modifier.clickable {
                                val intent =
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(
                                            deviceCodeResponse!!
                                                .verificationUri
                                        )
                                    )
                                context.startActivity(
                                    intent
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "代码 (长按复制)：",
                            style =
                            MaterialTheme.typography
                                .titleMedium
                        )
                        Text(
                            text =
                            deviceCodeResponse!!
                                .userCode,
                            style =
                            MaterialTheme.typography
                                .displayMedium,
                            modifier =
                            Modifier.combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    scope
                                        .launch {
                                            clipboardManager
                                                .setClipEntry(
                                                    ClipEntry(
                                                        ClipData.newPlainText(
                                                            "Microsoft Device Code",
                                                            deviceCodeResponse!!
                                                                .userCode
                                                        )
                                                    )
                                                )
                                        }
                                    android.widget
                                        .Toast
                                        .makeText(
                                            context,
                                            "代码已复制到剪贴板",
                                            android.widget
                                                .Toast
                                                .LENGTH_SHORT
                                        )
                                        .show()
                                }
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(
                            modifier =
                            Modifier.align(
                                Alignment
                                    .CenterHorizontally
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(
                            onClick = {
                                accountViewModel
                                    .cancelMicrosoftLogin()
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) { Text("取消") }
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("登录中", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) { CircularProgressIndicator() }
                    }
                }
            }
        }
        is MicrosoftLoginState.Error -> {
            PopupContainer(visible = true, onDismissRequest = { accountViewModel.resetMicrosoftLoginState() }) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("登录失败", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(state.message)
                    Spacer(Modifier.height(16.dp))
                    TextButton(
                        onClick = {
                            accountViewModel.resetMicrosoftLoginState()
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) { Text("确定") }
                }
            }
        }
        is MicrosoftLoginState.Success -> {
            LaunchedEffect(state) {
                // Should automatically show in list
                accountViewModel.resetMicrosoftLoginState()
            }
        }
        else -> {}
    }

    editingAccount?.let {
        EditAccountDialog(
            account = it,
            onDismiss = { editingAccount = null },
            onConfirm = { newUsername ->
                accountViewModel.updateOfflineAccount(it, newUsername)
                editingAccount = null
            }
        )
    }
}

@Composable
fun OfflineAccountInputDialog(onDismiss: () -> Unit, onAddOfflineAccount: (String) -> Unit) {
    var username by remember { mutableStateOf("") }
    PopupContainer(visible = true, onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text("添加离线账户", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("用户名") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.align(Alignment.End)) {
                TextButton(onClick = onDismiss) { Text("取消") }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        onAddOfflineAccount(username)
                        onDismiss()
                    }
                ) { Text("添加") }
            }
        }
    }
}

@Composable
fun EditAccountDialog(account: Account, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var username by remember(account.username) { mutableStateOf(account.username) }
    PopupContainer(visible = true, onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text("编辑账户", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("用户名") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.align(Alignment.End)) {
                TextButton(onClick = onDismiss) { Text("取消") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { onConfirm(username) }) { Text("保存") }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 1280, heightDp = 720)
@Composable
fun AccountScreenPreview() {
    ShardLauncherTheme { AccountScreen(navController = rememberNavController()) }
}
