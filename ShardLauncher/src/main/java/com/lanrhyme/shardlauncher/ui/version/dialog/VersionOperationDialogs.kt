package com.lanrhyme.shardlauncher.ui.version.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.lanrhyme.shardlauncher.ui.components.basic.ShardAlertDialog
import com.lanrhyme.shardlauncher.ui.components.basic.ShardEditDialog
import com.lanrhyme.shardlauncher.ui.components.basic.ShardInputField
import com.lanrhyme.shardlauncher.ui.components.basic.ShardTaskDialog

@Composable
fun RenameVersionDialog(
    version: Version,
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(version.getVersionName()) }
    var errorMessage by remember { mutableStateOf("") }

    val isError = name.isEmpty() || run {
        val validationError = validateVersionName(name, version.getVersionName())
        if (validationError != null) {
            errorMessage = validationError
            true
        } else {
            false
        }
    }

    ShardEditDialog(
        title = "重命名版本",
        value = name,
        onValueChange = { name = it },
        label = "版本名称",
        isError = isError,
        supportingText = {
            when {
                name.isEmpty() -> Text(text = "版本名称不能为空")
                isError -> Text(text = errorMessage)
            }
        },
        singleLine = true,
        onDismissRequest = onDismissRequest,
        onConfirm = {
            if (!isError) {
                onConfirm(name)
            }
        }
    )
}

@Composable
fun CopyVersionDialog(
    version: Version,
    onDismissRequest: () -> Unit,
    onConfirm: (String, Boolean) -> Unit
) {
    var copyAll by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val isError = name.isEmpty() || run {
        val validationError = validateVersionName(name, version.getVersionName())
        if (validationError != null) {
            errorMessage = validationError
            true
        } else {
            false
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("复制版本") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("创建版本的副本")
                
                ShardInputField(
                    value = name,
                    onValueChange = { name = it },
                    label = "新版本名称",
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (name.isEmpty()) {
                    Text(
                        text = "版本名称不能为空",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (isError) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = copyAll,
                        onCheckedChange = { copyAll = it }
                    )
                    Text(
                        text = "复制所有文件（包括存档、模组等）",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (!isError) {
                        onConfirm(name, copyAll)
                    }
                },
                enabled = !isError
            ) {
                Text("复制")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("取消")
            }
        }
    )
}

@Composable
fun DeleteVersionDialog(
    version: Version,
    message: String? = null,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    if (message != null) {
        ShardAlertDialog(
            visible = true,
            title = "删除版本",
            text = message,
            onDismiss = onDismissRequest,
            onConfirm = onConfirm
        )
    } else {
        ShardAlertDialog(
            visible = true,
            title = "删除版本",
            text = buildString {
                appendLine("确定要删除版本 ${version.getVersionName()} 吗？")
                appendLine("此操作将删除版本文件夹及其所有内容")
                appendLine("包括存档、模组、资源包等所有数据")
                append("此操作不可撤销！")
            },
            onDismiss = onDismissRequest,
            onConfirm = onConfirm,
            onCancel = onDismissRequest
        )
    }
}

sealed interface VersionOperationState {
    data object None: VersionOperationState
    data class Rename(val version: Version): VersionOperationState
    data class Copy(val version: Version): VersionOperationState
    data class Delete(val version: Version, val text: String? = null): VersionOperationState
    data class InvalidDelete(val version: Version): VersionOperationState
    data class RunTask(val title: String, val task: suspend () -> Unit): VersionOperationState
}

@Composable
fun VersionsOperation(
    versionsOperation: VersionOperationState,
    updateVersionsOperation: (VersionOperationState) -> Unit,
    onError: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    
    when (versionsOperation) {
        is VersionOperationState.None -> {}
        is VersionOperationState.Rename -> {
            RenameVersionDialog(
                version = versionsOperation.version,
                onDismissRequest = { updateVersionsOperation(VersionOperationState.None) },
                onConfirm = { newName ->
                    updateVersionsOperation(
                        VersionOperationState.RunTask(
                            title = "重命名版本",
                            task = {
                                VersionsManager.renameVersion(versionsOperation.version, newName)
                            }
                        )
                    )
                }
            )
        }
        is VersionOperationState.Copy -> {
            CopyVersionDialog(
                version = versionsOperation.version,
                onDismissRequest = { updateVersionsOperation(VersionOperationState.None) },
                onConfirm = { name, copyAll ->
                    updateVersionsOperation(
                        VersionOperationState.RunTask(
                            title = "复制版本",
                            task = {
                                VersionsManager.copyVersion(versionsOperation.version, name, copyAll)
                            }
                        )
                    )
                }
            )
        }
        is VersionOperationState.Delete -> {
            DeleteVersionDialog(
                version = versionsOperation.version,
                message = versionsOperation.text,
                onDismissRequest = { updateVersionsOperation(VersionOperationState.None) },
                onConfirm = {
                    updateVersionsOperation(
                        VersionOperationState.RunTask(
                            title = "删除版本",
                            task = {
                                VersionsManager.deleteVersion(versionsOperation.version)
                            }
                        )
                    )
                }
            )
        }
        is VersionOperationState.InvalidDelete -> {
             ShardAlertDialog(
                visible = true,
                title = "无法删除",
                text = "版本 ${versionsOperation.version.getVersionName()} 正在运行或状态异常，无法删除。",
                onConfirm = { updateVersionsOperation(VersionOperationState.None) },
                onDismiss = { updateVersionsOperation(VersionOperationState.None) }
            )
        }
        is VersionOperationState.RunTask -> {
            ShardTaskDialog(
                title = versionsOperation.title,
                task = versionsOperation.task,
                onDismiss = { updateVersionsOperation(VersionOperationState.None) },
                onError = { e ->
                    onError(e.message ?: "处理失败")
                }
            )
        }
    }
}

private fun validateVersionName(name: String, originalName: String? = null): String? {
    return when {
        name.isEmpty() -> "版本名称不能为空"
        name.contains(Regex("""[<>:"/\\|?*]""")) -> "版本名称包含非法字符"
        VersionsManager.isVersionExists(name, true) -> "版本已存在"
        originalName != null && name == originalName -> "新名称不能与原名称相同"
        else -> null
    }
}
