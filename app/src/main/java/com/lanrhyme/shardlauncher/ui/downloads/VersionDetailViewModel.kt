package com.lanrhyme.shardlauncher.ui.downloads

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lanrhyme.shardlauncher.api.ApiClient
import com.lanrhyme.shardlauncher.game.version.download.BaseMinecraftDownloader
import com.lanrhyme.shardlauncher.game.version.download.DownloadMode
import com.lanrhyme.shardlauncher.game.version.download.MinecraftDownloader
import com.lanrhyme.shardlauncher.model.FabricLoaderVersion
import com.lanrhyme.shardlauncher.model.LoaderVersion
import com.lanrhyme.shardlauncher.model.ForgeVersionToken
import com.lanrhyme.shardlauncher.model.QuiltVersion
import com.lanrhyme.shardlauncher.model.OptiFineVersionToken
import com.lanrhyme.shardlauncher.model.version.VersionManager
import com.lanrhyme.shardlauncher.coroutine.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ModLoader {
    None,
    Forge,
    Fabric,
    NeoForge,
    Quilt
}

class VersionDetailViewModel(application: Application, private val versionId: String) : AndroidViewModel(application) {

    private val _versionName = MutableStateFlow(versionId)
    val versionName = _versionName.asStateFlow()
    private var isVersionNameManuallyEdited = false

    // Mod Loader State
    private val _selectedModLoader = MutableStateFlow(ModLoader.None)
    val selectedModLoader = _selectedModLoader.asStateFlow()

    private val _isFabricApiSelected = MutableStateFlow(false)
    val isFabricApiSelected = _isFabricApiSelected.asStateFlow()
    private val _fabricApiVersions = MutableStateFlow<List<String>>(emptyList()) // Placeholder
    val fabricApiVersions = _fabricApiVersions.asStateFlow()
    private val _selectedFabricApiVersion = MutableStateFlow<String?>(null)
    val selectedFabricApiVersion = _selectedFabricApiVersion.asStateFlow()

    private val _fabricVersions = MutableStateFlow<List<FabricLoaderVersion>>(emptyList())
    val fabricVersions = _fabricVersions.asStateFlow()
    private val _selectedFabricVersion = MutableStateFlow<FabricLoaderVersion?>(null)
    val selectedFabricVersion = _selectedFabricVersion.asStateFlow()

    private val _forgeVersions = MutableStateFlow<List<LoaderVersion>>(emptyList())
    val forgeVersions = _forgeVersions.asStateFlow()
    private val _selectedForgeVersion = MutableStateFlow<LoaderVersion?>(null)
    val selectedForgeVersion = _selectedForgeVersion.asStateFlow()

    private val _neoForgeVersions = MutableStateFlow<List<LoaderVersion>>(emptyList())
    val neoForgeVersions = _neoForgeVersions.asStateFlow()
    private val _selectedNeoForgeVersion = MutableStateFlow<LoaderVersion?>(null)
    val selectedNeoForgeVersion = _selectedNeoForgeVersion.asStateFlow()

    private val _quiltVersions = MutableStateFlow<List<LoaderVersion>>(emptyList())
    val quiltVersions = _quiltVersions.asStateFlow()
    private val _selectedQuiltVersion = MutableStateFlow<LoaderVersion?>(null)
    val selectedQuiltVersion = _selectedQuiltVersion.asStateFlow()

    // Optifine State
    private val _isOptifineSelected = MutableStateFlow(false)
    val isOptifineSelected = _isOptifineSelected.asStateFlow()

    private val _optifineVersions = MutableStateFlow<List<LoaderVersion>>(emptyList())
    val optifineVersions = _optifineVersions.asStateFlow()
    private val _selectedOptifineVersion = MutableStateFlow<LoaderVersion?>(null)
    val selectedOptifineVersion = _selectedOptifineVersion.asStateFlow()

    private val _downloadTask = MutableStateFlow<Task?>(null)
    val downloadTask = _downloadTask.asStateFlow()
    
    // GameInstaller 实例
    private var installer: com.lanrhyme.shardlauncher.game.download.game.GameInstaller? = null

    init {
        loadAllLoaderVersions()
    }

    private fun loadAllLoaderVersions() {
        viewModelScope.launch {
            try {
                val fabricVersions = ApiClient.fabricApiService.getLoaderVersions(versionId)
                _fabricVersions.value = fabricVersions
                _selectedFabricVersion.value = fabricVersions.firstOrNull { it.stable == true }

                val forgeVersionTokens = ApiClient.forgeApiService.getForgeVersions(versionId)
                val forgeVersions = forgeVersionTokens.map { it.toLoaderVersion() }
                _forgeVersions.value = forgeVersions
                _selectedForgeVersion.value = forgeVersions.firstOrNull { it.isRecommended }

                val neoForgeVersionStrings = ApiClient.neoForgeApiService.getNeoForgeVersions(versionId)
                val neoForgeVersions = neoForgeVersionStrings.map { LoaderVersion(version = it) }
                _neoForgeVersions.value = neoForgeVersions
                _selectedNeoForgeVersion.value = neoForgeVersions.firstOrNull()

                val quiltVersions = ApiClient.quiltApiService.getQuiltVersions(versionId)
                val mappedQuiltVersions = quiltVersions.map { it.toLoaderVersion() }
                _quiltVersions.value = mappedQuiltVersions
                _selectedQuiltVersion.value = mappedQuiltVersions.firstOrNull { it.status == "Stable" }

                val optiFineVersionTokens = ApiClient.optiFineApiService.getOptiFineVersions()
                val mappedOptiFineVersions = optiFineVersionTokens
                    .filter { it.mcVersion == versionId }
                    .map { it.toLoaderVersion() }
                _optifineVersions.value = mappedOptiFineVersions
                _selectedOptifineVersion.value = mappedOptiFineVersions.firstOrNull()

            } catch (e: Exception) { 
                // 记录错误，但继续执行，让UI可以显示其他可用的Mod Loader
                e.printStackTrace()
            }

            // Placeholders
            _fabricApiVersions.value = listOf("0.100.0+1.21", "0.99.3+1.21", "0.96.4+1.20.6")
            _selectedFabricApiVersion.value = _fabricApiVersions.value.firstOrNull()
        }
    }

    fun setVersionName(name: String) {
        isVersionNameManuallyEdited = true
        _versionName.value = name
    }

    fun selectModLoader(loader: ModLoader) {
        _selectedModLoader.value = if (_selectedModLoader.value == loader) ModLoader.None else loader
        if (_selectedModLoader.value != ModLoader.Fabric) {
            _isFabricApiSelected.value = false
        }
        updateVersionNameSuffix()
    }

    fun toggleFabricApi(selected: Boolean) {
        _isFabricApiSelected.value = selected
    }

    fun selectFabricVersion(version: FabricLoaderVersion) {
        _selectedFabricVersion.value = version
        updateVersionNameSuffix()
    }

    fun selectFabricApiVersion(version: String) {
        _selectedFabricApiVersion.value = version
    }

    fun selectForgeVersion(version: LoaderVersion) {
        _selectedForgeVersion.value = version
        updateVersionNameSuffix()
    }

    fun selectNeoForgeVersion(version: LoaderVersion) {
        _selectedNeoForgeVersion.value = version
        updateVersionNameSuffix()
    }

    fun selectQuiltVersion(version: LoaderVersion) {
        _selectedQuiltVersion.value = version
        updateVersionNameSuffix()
    }

    fun toggleOptifine(selected: Boolean) {
        _isOptifineSelected.value = selected
        updateVersionNameSuffix()
    }

    fun selectOptifineVersion(version: LoaderVersion) {
        _selectedOptifineVersion.value = version
        updateVersionNameSuffix()
    }

    private fun updateVersionNameSuffix() {
        if (isVersionNameManuallyEdited) return

        _versionName.value = buildString {
            append(versionId)

            when (selectedModLoader.value) {
                ModLoader.Fabric -> {
                    append("-Fabric")
                    selectedFabricVersion.value?.let { append("-${it.version}") }
                }
                ModLoader.Forge -> {
                    append("-Forge")
                    selectedForgeVersion.value?.let { append("-${it.version}") }
                }
                ModLoader.NeoForge -> {
                    append("-NeoForge")
                    selectedNeoForgeVersion.value?.let { append("-${it.version}") }
                }
                ModLoader.Quilt -> {
                    append("-Quilt")
                    selectedQuiltVersion.value?.let { append("-${it.version}") }
                }
                ModLoader.None -> { /* Do nothing */ }
            }

            if (isOptifineSelected.value) {
                append("-Optifine")
                selectedOptifineVersion.value?.let { append("-${it.version}") }
            }
        }
    }

    private fun ForgeVersionToken.toLoaderVersion(): LoaderVersion {
        val isRecommended = version.endsWith("-recommended")
        val status = when {
            isRecommended -> "Recommended"
            else -> null
        }
        return LoaderVersion(
            version = version,
            releaseTime = modified,
            isRecommended = isRecommended,
            status = status
        )
    }

    private fun QuiltVersion.toLoaderVersion(): LoaderVersion {
        val status = if (stable) "Stable" else "Beta"
        return LoaderVersion(
            version = version,
            status = status
        )
    }

    private fun OptiFineVersionToken.toLoaderVersion(): LoaderVersion {
        return LoaderVersion(
            version = "${type}_${patch}"
        )
    }

    fun download() {
        viewModelScope.launch {
            try {
                com.lanrhyme.shardlauncher.utils.logging.Logger.lInfo("Starting download for version: $versionId")
                
                val manifest = VersionManager.getVersionManifest()
                val version = manifest.versions.find { it.id == versionId }
                if (version != null) {
                    com.lanrhyme.shardlauncher.utils.logging.Logger.lInfo("Found version manifest: ${version.id}")
                    
                    // 根据选择的 Mod Loader 创建相应的版本信息
                    val fabricVersion = selectedFabricVersion.value?.let {
                        com.lanrhyme.shardlauncher.utils.logging.Logger.lInfo("Selected Fabric version: ${it.version}")
                        com.lanrhyme.shardlauncher.game.download.game.FabricVersion(
                            version = it.version,
                            loaderName = "Fabric"
                        )
                    }
                    
                    val forgeVersion = selectedForgeVersion.value?.let {
                        com.lanrhyme.shardlauncher.utils.logging.Logger.lInfo("Selected Forge version: ${it.version}")
                        com.lanrhyme.shardlauncher.game.download.game.ForgeVersion(
                            version = it.version,
                            loaderName = "Forge"
                        )
                    }
                    
                    val neoForgeVersion = selectedNeoForgeVersion.value?.let {
                        com.lanrhyme.shardlauncher.utils.logging.Logger.lInfo("Selected NeoForge version: ${it.version}")
                        com.lanrhyme.shardlauncher.game.download.game.NeoForgeVersion(
                            version = it.version,
                            loaderName = "NeoForge"
                        )
                    }
                    
                    val quiltVersion = selectedQuiltVersion.value?.let {
                        com.lanrhyme.shardlauncher.utils.logging.Logger.lInfo("Selected Quilt version: ${it.version}")
                        com.lanrhyme.shardlauncher.game.download.game.QuiltVersion(
                            version = it.version,
                            loaderName = "Quilt"
                        )
                    }
                    
                    val downloadInfo = com.lanrhyme.shardlauncher.game.download.game.GameDownloadInfo(
                        gameVersion = version.id,
                        customVersionName = _versionName.value,
                        fabric = fabricVersion,
                        forge = forgeVersion,
                        neoForge = neoForgeVersion,
                        quilt = quiltVersion
                    )
                    
                    com.lanrhyme.shardlauncher.utils.logging.Logger.lInfo("Creating game installer for: ${downloadInfo.customVersionName}")
                    
                    // 创建游戏安装器
                    installer = com.lanrhyme.shardlauncher.game.download.game.GameInstaller(
                        context = getApplication(),
                        info = downloadInfo,
                        scope = viewModelScope
                    )
                    
                    // 创建一个虚拟的Task来跟踪下载状态
                    val downloadTask = com.lanrhyme.shardlauncher.coroutine.Task.runTask(
                        id = "game_download_${versionId}",
                        task = { task ->
                            task.taskState = com.lanrhyme.shardlauncher.coroutine.TaskState.RUNNING
                            // 这里不执行实际任务，实际任务由GameInstaller处理
                            // 我们只是用这个Task来跟踪UI状态
                        },
                        onError = { error ->
                            com.lanrhyme.shardlauncher.utils.logging.Logger.lError("Download task error: ${error.message}", error)
                            _downloadTask.value = null
                        },
                        onFinally = {
                            // 延迟清理任务状态，让用户看到完成状态
                            viewModelScope.launch {
                                kotlinx.coroutines.delay(500)
                                _downloadTask.value = null
                            }
                        }
                    )
                    
                    // 设置下载任务状态
                    _downloadTask.value = downloadTask
                    
                    // 执行安装
                    installer?.installGame(
                        isRunning = { 
                            com.lanrhyme.shardlauncher.utils.logging.Logger.lWarning("Installation already in progress")
                            // 已在安装中，取消虚拟任务
                            downloadTask.taskState = com.lanrhyme.shardlauncher.coroutine.TaskState.COMPLETED
                        },
                        onInstalled = { installedVersion ->
                            com.lanrhyme.shardlauncher.utils.logging.Logger.lInfo("Game installed successfully: $installedVersion")
                            downloadTask.taskState = com.lanrhyme.shardlauncher.coroutine.TaskState.COMPLETED
                            // 刷新版本列表，让新安装的版本被检测到
                            com.lanrhyme.shardlauncher.game.version.installed.VersionsManager.refresh(
                                tag = "VersionDetailViewModel.download",
                                trySetVersion = installedVersion
                            )
                        },
                        onError = { error ->
                            com.lanrhyme.shardlauncher.utils.logging.Logger.lError("Game installation failed: ${error.message}", error)
                            downloadTask.taskState = com.lanrhyme.shardlauncher.coroutine.TaskState.COMPLETED
                        },
                        onGameAlreadyInstalled = {
                            com.lanrhyme.shardlauncher.utils.logging.Logger.lWarning("Game already installed: ${_versionName.value}")
                            downloadTask.taskState = com.lanrhyme.shardlauncher.coroutine.TaskState.COMPLETED
                        }
                    )
                } else {
                    com.lanrhyme.shardlauncher.utils.logging.Logger.lError("Version not found: $versionId")
                    // Handle version not found
                }
            } catch (e: Exception) {
                com.lanrhyme.shardlauncher.utils.logging.Logger.lError("Download initialization failed: ${e.message}", e)
                // Handle error
                _downloadTask.value = null
            }
        }
    }
    
    /**
     * 获取游戏安装器的任务流
     */
    fun getTasksFlow(): kotlinx.coroutines.flow.StateFlow<List<com.lanrhyme.shardlauncher.coroutine.TitledTask>> {
        return installer?.tasksFlow ?: kotlinx.coroutines.flow.MutableStateFlow(emptyList())
    }
    
    /**
     * 取消安装
     */
    fun cancelInstall() {
        com.lanrhyme.shardlauncher.utils.logging.Logger.lInfo("Cancelling installation for version: $versionId")
        installer?.cancelInstall()
        _downloadTask.value = null
        com.lanrhyme.shardlauncher.utils.logging.Logger.lInfo("Installation cancelled")
    }

    /**
     * 完成下载
     */
    fun completeDownload() {
        _downloadTask.value = null
    }
}