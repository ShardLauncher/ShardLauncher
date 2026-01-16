package com.lanrhyme.shardlauncher.game.download.game

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CleaningServices
import com.lanrhyme.shardlauncher.R
import com.lanrhyme.shardlauncher.coroutine.Task
import com.lanrhyme.shardlauncher.coroutine.TaskFlowExecutor
import com.lanrhyme.shardlauncher.coroutine.TitledTask
import com.lanrhyme.shardlauncher.coroutine.addTask
import com.lanrhyme.shardlauncher.coroutine.buildPhase
import com.lanrhyme.shardlauncher.game.addons.mirror.mapMirrorableUrls
import com.lanrhyme.shardlauncher.game.path.getGameHome
import com.lanrhyme.shardlauncher.game.version.download.BaseMinecraftDownloader
import com.lanrhyme.shardlauncher.game.version.download.GameLibDownloader
import com.lanrhyme.shardlauncher.game.version.download.MinecraftDownloader
import com.lanrhyme.shardlauncher.game.version.installed.VersionsManager
import com.lanrhyme.shardlauncher.game.versioninfo.models.GameManifest
import com.lanrhyme.shardlauncher.path.PathManager
import com.lanrhyme.shardlauncher.settings.AllSettings
import com.lanrhyme.shardlauncher.utils.GSON
import com.lanrhyme.shardlauncher.utils.logging.Logger
import com.lanrhyme.shardlauncher.utils.network.downloadFromMirrorListSuspend
import com.lanrhyme.shardlauncher.utils.network.fetchStringFromUrls
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.concurrent.atomic.AtomicLong

/**
 * 在安装游戏前发现存在冲突的已安装版本，抛出这个异常
 */
private class GameAlreadyInstalledException : RuntimeException()

/**
 * 游戏安装器
 * @param context 用于获取任务描述信息
 * @param info 安装游戏所需要的信息，包括 Minecraft id、自定义版本名称、Addon 列表
 * @param scope 在有生命周期管理的scope中执行安装任务
 */
class GameInstaller(
    private val context: Context,
    private val info: GameDownloadInfo,
    private val scope: CoroutineScope
) {
    private val taskExecutor = TaskFlowExecutor(scope)
    val tasksFlow: StateFlow<List<TitledTask>> = taskExecutor.tasksFlow

    /**
     * 基础下载器
     */
    private val downloader = BaseMinecraftDownloader(verifyIntegrity = true)

    /**
     * 目标游戏客户端目录（缓存）
     * versions/<client-name>/...
     */
    private var targetClientDir: File? = null

    /**
     * 目标游戏目录
     */
    private val targetGameFolder: File = File(getGameHome())

    /**
     * 安装 Minecraft 游戏
     * @param isRunning 正在运行中，阻止此次安装时
     * @param onInstalled 游戏已完成安装
     * @param onError 游戏安装失败
     * @param onGameAlreadyInstalled 在安装游戏前发现存在冲突的已安装版本
     */
    fun installGame(
        isRunning: () -> Unit = {},
        onInstalled: (version: String) -> Unit,
        onError: (th: Throwable) -> Unit,
        onGameAlreadyInstalled: () -> Unit
    ) {
        Logger.lInfo("Starting game installation: ${info.customVersionName} (Minecraft ${info.gameVersion})")
        Logger.lInfo("Mod loaders: Fabric=${info.fabric != null}, Forge=${info.forge != null}, NeoForge=${info.neoForge != null}, Quilt=${info.quilt != null}")
        
        if (taskExecutor.isRunning()) {
            //正在安装中，阻止这次安装请求
            Logger.lWarning("Game installation is already in progress, ignoring new request")
            isRunning()
            return
        }

        taskExecutor.executePhasesAsync(
            onStart = {
                Logger.lInfo("Game installation started: ${info.customVersionName}")
                val tasks = getTaskPhase()
                taskExecutor.addPhases(tasks)
            },
            onComplete = {
                Logger.lInfo("Game installation completed successfully: ${info.customVersionName}")
                onInstalled(info.customVersionName)
            },
            onError = { th ->
                Logger.lError("Game installation failed: ${info.customVersionName}", th)
                if (th is GameAlreadyInstalledException) {
                    onGameAlreadyInstalled()
                } else {
                    onError(th)
                }
            }
        )
    }

    /**
     * 安装过程中所需的所有文件路径配置
     */
    private class InstallationPathConfig(
        val targetClientDir: File,
        val tempGameDir: File,
        val tempMinecraftDir: File,
        val tempGameVersionsDir: File,
        val tempClientDir: File,
        val fabricDir: File?
    )

    /**
     * 构建安装过程中使用的所有路径配置
     */
    private fun createPathConfig(checkTargetVersion: Boolean): InstallationPathConfig {
        //目标版本目录
        val targetClientDir1 = VersionsManager.getVersionPath(info.customVersionName)
        targetClientDir = targetClientDir1
        val targetVersionJson = File(targetClientDir1, "${info.customVersionName}.json")

        //目标版本已经安装的情况
        if (checkTargetVersion && targetVersionJson.exists()) {
            Logger.lDebug("The game has already been installed!")
            throw GameAlreadyInstalledException()
        }

        val tempGameDir = PathManager.DIR_CACHE_GAME_DOWNLOADER
        val tempMinecraftDir = File(tempGameDir, ".minecraft")
        val tempGameVersionsDir = File(tempMinecraftDir, "versions")
        val tempClientDir = File(tempGameVersionsDir, info.gameVersion)

        //ModLoader临时目录
        val fabricDir = info.fabric?.let { File(tempGameVersionsDir, "fabric-loader-${it.version}-${info.gameVersion}") }

        return InstallationPathConfig(
            targetClientDir = targetClientDir1,
            tempGameDir = tempGameDir,
            tempMinecraftDir = tempMinecraftDir,
            tempGameVersionsDir = tempGameVersionsDir,
            tempClientDir = tempClientDir,
            fabricDir = fabricDir
        )
    }

    /**
     * 获取安装 Minecraft 游戏的任务流阶段
     */
    private fun getTaskPhase(): List<TaskFlowExecutor.TaskPhase> {
        val pathConfig = createPathConfig(checkTargetVersion = true)

        return listOf(
            buildPhase {
                //开始之前，应该先清理一次临时游戏目录，否则可能会影响安装结果
                addTask(
                    id = "Download.Game.ClearTemp",
                    title = context.getString(R.string.download_install_clear_temp),
                    icon = Icons.Outlined.CleaningServices,
                    task = Task.runTask(id = "Download.Game.ClearTemp", task = { task ->
                        clearTempGameDir()
                        //清理完成缓存目录后，创建新的缓存目录
                        pathConfig.tempClientDir.createDirAndLog()
                        pathConfig.fabricDir?.createDirAndLog()
                    })
                )

                //下载安装原版
                addTask(
                    title = context.getString(R.string.download_game_install_vanilla, info.gameVersion),
                    task = createMinecraftDownloadTask(info.gameVersion, pathConfig.tempGameVersionsDir)
                )

                //下载加载器/模组
                addLoaderTasks(
                    tempGameDir = pathConfig.tempGameDir,
                    tempMinecraftDir = pathConfig.tempMinecraftDir,
                    fabricDir = pathConfig.fabricDir
                )

                //最终游戏安装任务
                addTask(
                    title = context.getString(R.string.download_game_install_game_files_progress),
                    icon = Icons.Outlined.Build,
                    //如果有非原版以外的任务，则需要进行处理安装（合并版本Json、迁移文件等）
                    task = if (pathConfig.fabricDir != null) {
                        createGameInstalledTask(
                            tempMinecraftDir = pathConfig.tempMinecraftDir,
                            targetMinecraftDir = targetGameFolder,
                            targetClientDir = pathConfig.targetClientDir,
                            tempClientDir = pathConfig.tempClientDir,
                            fabricFolder = pathConfig.fabricDir
                        )
                    } else {
                        //仅仅下载了原版，只复制版本client文件
                        createVanillaFilesCopyTask(
                            tempMinecraftDir = pathConfig.tempMinecraftDir
                        )
                    }
                )
            }
        )
    }

    private fun MutableList<TitledTask>.addLoaderTasks(
        tempGameDir: File,
        tempMinecraftDir: File,
        fabricDir: File?
    ) {
        // Fabric 安装
        info.fabric?.let {
            createFabricLikeTask(
                loaderName = "Fabric",
                loaderVersion = it,
                tempMinecraftDir = tempMinecraftDir,
                tempFolderName = fabricDir!!.name
            )
        }
        
        // Quilt 安装
        info.quilt?.let {
            createFabricLikeTask(
                loaderName = "Quilt",
                loaderVersion = it,
                tempMinecraftDir = tempMinecraftDir,
                tempFolderName = fabricDir!!.name
            )
        }
        
        // Forge 安装
        info.forge?.let {
            createForgeLikeTask(
                loaderName = "Forge",
                loaderVersion = it,
                tempGameDir = tempGameDir,
                tempMinecraftDir = tempMinecraftDir,
                tempFolderName = "forge-${it.version}-${info.gameVersion}"
            )
        }
        
        // NeoForge 安装
        info.neoForge?.let {
            createForgeLikeTask(
                loaderName = "NeoForge",
                loaderVersion = it,
                tempGameDir = tempGameDir,
                tempMinecraftDir = tempMinecraftDir,
                tempFolderName = "neoforge-${it.version}-${info.gameVersion}"
            )
        }
    }

    fun cancelInstall() {
        Logger.lInfo("Cancelling game installation: ${info.customVersionName}")
        taskExecutor.cancel()
        clearTargetClient()
        Logger.lInfo("Game installation cancelled")
    }

    /**
     * 清除临时游戏目录
     */
    private fun clearTempGameDir() {
        PathManager.DIR_CACHE_GAME_DOWNLOADER.takeIf { it.exists() }?.let { folder ->
            Logger.lInfo("Clearing temporary game directory: ${folder.absolutePath}")
            FileUtils.deleteQuietly(folder)
            Logger.lInfo("Temporary game directory cleared successfully")
        }
    }

    /**
     * 安装失败、取消安装时，都应该清除目标客户端版本文件夹
     */
    private fun clearTargetClient() {
        val dirToDelete = targetClientDir //临时变量
        targetClientDir = null

        CoroutineScope(Dispatchers.IO).launch {
            dirToDelete?.let {
                Logger.lInfo("Deleting target client directory: ${it.absolutePath}")
                //直接清除上一次安装的目标目录
                FileUtils.deleteQuietly(it)
                Logger.lInfo("Successfully deleted version directory: ${it.name} at path: ${it.absolutePath}")
            }
        }
    }

    /**
     * 获取下载原版 Task
     */
    private fun createMinecraftDownloadTask(
        tempClientName: String,
        tempVersionsDir: File
    ): Task {
        Logger.lInfo("Creating Minecraft download task for version: ${info.gameVersion}")
        val mcDownloader = MinecraftDownloader(
            context = context,
            version = info.gameVersion,
            customName = info.customVersionName,
            verifyIntegrity = true
        )

        return mcDownloader.getDownloadTask(tempClientName, tempVersionsDir)
    }

    private fun MutableList<TitledTask>.createFabricLikeTask(
        loaderName: String,
        loaderVersion: ModLoaderVersion,
        tempMinecraftDir: File,
        tempFolderName: String
    ) {
        Logger.lInfo("Creating $loaderName download task for version: ${loaderVersion.version}")
        val tempVersionJson = File(tempMinecraftDir, "versions/$tempFolderName/$tempFolderName.json")
        val tempVersionJar = File(tempMinecraftDir, "versions/$tempFolderName/$tempFolderName.jar")

        // 下载 Mod Loader Json
        addTask(
            title = context.getString(
                R.string.download_game_install_fabric,
                loaderVersion.version
            ),
            task = Task.runTask(id = "Download.$loaderName.Json", task = { task ->
                // 构造 Mod Loader 版本 URL
                val baseUrl = when (loaderName) {
                    "Fabric" -> "https://meta.fabricmc.net/v2/versions/loader"
                    "Quilt" -> "https://meta.quiltmc.org/v3/versions/loader"
                    else -> throw IllegalArgumentException("Unsupported loader: $loaderName")
                }
                
                val loaderUrl = "$baseUrl/${info.gameVersion}/${loaderVersion.version}/profile/json"

                Logger.lInfo("Downloading $loaderName profile from: $loaderUrl")
                
                // 下载 Mod Loader 配置文件
                val downloadSource = AllSettings.fetchModLoaderSource.state
                val loaderJson = fetchStringFromUrls(loaderUrl.mapMirrorableUrls(downloadSource))

                // 保存 Mod Loader 配置文件
                tempVersionJson.parentFile.mkdirs()
                tempVersionJson.writeText(loaderJson)

                Logger.lInfo("Successfully downloaded $loaderName profile to: ${tempVersionJson.absolutePath}")
            })
        )
        
        // 下载 Mod Loader 库文件
        addTask(
            title = context.getString(R.string.download_game_install_game_files_progress),
            task = Task.runTask(id = "Download.$loaderName.Libraries", task = { task ->
                Logger.lInfo("Starting $loaderName libraries download")
                
                // 从 Mod Loader 配置文件中提取并下载所需的库
                val loaderJson = tempVersionJson.readText()
                val gameManifest = GSON.fromJson(loaderJson, GameManifest::class.java)

                // 创建库下载器
                val libDownloader = GameLibDownloader(
                    downloader = this@GameInstaller.downloader,
                    gameManifest = gameManifest,
                    maxDownloadThreads = 32
                )
                
                // 下载所有库文件
                libDownloader.download(task)
                
                Logger.lInfo("Successfully downloaded all $loaderName libraries")
            })
        )
    }
    
    private fun MutableList<TitledTask>.createForgeLikeTask(
        loaderName: String,
        loaderVersion: ModLoaderVersion,
        tempGameDir: File,
        tempMinecraftDir: File,
        tempFolderName: String
    ) {
        Logger.lInfo("Creating $loaderName download task for version: ${loaderVersion.version}")
        val tempVersionJson = File(tempMinecraftDir, "versions/$tempFolderName/$tempFolderName.json")
        val tempInstallerJar = File(tempGameDir, "$tempFolderName-installer.jar")

        // 下载安装器
        addTask(
            title = context.getString(R.string.download_game_install_game_files_progress),
            task = Task.runTask(id = "Download.$loaderName.Installer", task = { task ->
                // 获取下载 URL
                val downloadUrl = when (loaderName) {
                    "Forge" -> {
                        val forgeVersion = loaderVersion as ForgeVersion
                        forgeVersion.installerPath ?: getForgeDownloadUrl(info.gameVersion, loaderVersion.version)
                    }
                    "NeoForge" -> {
                        val neoForgeVersion = loaderVersion as NeoForgeVersion
                        neoForgeVersion.installerPath ?: getNeoForgeDownloadUrl(info.gameVersion, loaderVersion.version)
                    }
                    else -> throw IllegalArgumentException("Unsupported loader: $loaderName")
                }
                
                Logger.lInfo("Downloading $loaderName installer from: $downloadUrl")
                
                // 下载安装器
                val downloadSource = AllSettings.fetchModLoaderSource.state
                downloadFromMirrorListSuspend(
                    urls = downloadUrl.mapMirrorableUrls(downloadSource),
                    targetFile = tempInstallerJar
                )
                
                Logger.lInfo("Successfully downloaded $loaderName installer to: ${tempInstallerJar.absolutePath}")
            })
        )
        
        // 安装 Mod Loader（这里简化处理，实际应该运行安装器）
        addTask(
            title = context.getString(R.string.download_game_install_game_files_progress),
            task = Task.runTask(id = "Install.$loaderName", task = { task ->
                Logger.lInfo("Starting $loaderName installation")
                
                // TODO: 运行 Forge/NeoForge 安装器
                // 由于 Forge/NeoForge 安装器需要运行 Java 程序，这里先简化处理
                // 实际应该：
                // 1. 解压安装器 JAR
                // 2. 运行安装器提取文件
                // 3. 获取版本 JSON 和库文件列表
                
                // 临时方案：直接下载版本 JSON（如果可用）
                val versionJsonUrl = when (loaderName) {
                    "Forge" -> getForgeVersionJsonUrl(info.gameVersion, loaderVersion.version)
                    "NeoForge" -> getNeoForgeVersionJsonUrl(info.gameVersion, loaderVersion.version)
                    else -> null
                }
                
                versionJsonUrl?.let { url ->
                    try {
                        Logger.lInfo("Downloading $loaderName version JSON from: $url")
                        val downloadSource = AllSettings.fetchModLoaderSource.state
                        val versionJson = fetchStringFromUrls(url.mapMirrorableUrls(downloadSource))
                        tempVersionJson.parentFile.mkdirs()
                        tempVersionJson.writeText(versionJson)
                        
                        Logger.lInfo("Starting $loaderName libraries download")
                        
                        // 下载库文件
                        val gameManifest = GSON.fromJson(versionJson, GameManifest::class.java)
                        val libDownloader = GameLibDownloader(
                            downloader = this@GameInstaller.downloader,
                            gameManifest = gameManifest,
                            maxDownloadThreads = 32
                        )
                        libDownloader.download(task)
                        
                        Logger.lInfo("Successfully downloaded all $loaderName libraries")
                    } catch (e: Exception) {
                        Logger.lError("Failed to install $loaderName: ${e.message}", e)
                        throw e
                    }
                }
                
                Logger.lInfo("$loaderName installation completed successfully")
            })
        )
    }

    /**
     * 游戏带附加内容安装完成，合并版本Json、迁移游戏文件
     */
    private fun createGameInstalledTask(
        tempMinecraftDir: File,
        targetMinecraftDir: File,
        targetClientDir: File,
        tempClientDir: File,
        fabricFolder: File? = null
    ) = Task.runTask(
        id = GAME_JSON_MERGER_ID,
        dispatcher = Dispatchers.IO,
        task = { task ->
            //合并版本 Json
            task.updateProgress(0.1f)
            mergeGameJson(
                info = info,
                outputFolder = targetClientDir,
                clientFolder = tempClientDir,
                fabricFolder = fabricFolder
            )

            //迁移游戏文件
            val sourceLibraries = File(tempMinecraftDir, "libraries")
            val targetLibraries = File(targetMinecraftDir, "libraries")
            if (sourceLibraries.exists()) {
                FileUtils.copyDirectory(sourceLibraries, targetLibraries)
            }

            //复制客户端文件
            copyVanillaFiles(
                sourceGameFolder = tempMinecraftDir,
                sourceVersion = info.gameVersion,
                destinationGameFolder = targetGameFolder,
                targetVersion = info.customVersionName
            )

            //清除临时游戏目录
            task.updateProgress(-1f, R.string.download_install_clear_temp)
            clearTempGameDir()
        }
    )

    /**
     * 仅原本客户端文件复制任务 json、jar
     */
    private fun createVanillaFilesCopyTask(
        tempMinecraftDir: File
    ): Task {
        return Task.runTask(
            id = "VanillaFilesCopy",
            task = { task ->
                //复制客户端文件
                copyVanillaFiles(
                    sourceGameFolder = tempMinecraftDir,
                    sourceVersion = info.gameVersion,
                    destinationGameFolder = targetGameFolder,
                    targetVersion = info.customVersionName
                )

                //清除临时游戏目录
                task.updateProgress(-1f, R.string.download_install_clear_temp)
                clearTempGameDir()
            }
        )
    }

    private fun File.createDirAndLog(): File {
        this.mkdirs()
        Logger.lDebug("Created directory: $this")
        return this
    }

    companion object {
        private const val GAME_JSON_MERGER_ID = "GameJsonMerger"
        
        // Forge 下载 URL 模板
        private const val FORGE_MAVEN_URL = "https://files.minecraftforge.net/maven/net/minecraftforge/forge"
        
        // NeoForge 下载 URL 模板
        private const val NEOFORGE_MAVEN_URL = "https://maven.neoforged.net/releases/net/neoforged"
        
        /**
         * 获取 Forge 安装器下载 URL
         */
        fun getForgeDownloadUrl(mcVersion: String, forgeVersion: String): String {
            // Forge 版本格式：1.19.3-41.2.8
            // URL 格式：https://files.minecraftforge.net/maven/net/minecraftforge/forge/1.19.3-41.2.8/forge-1.19.3-41.2.8-installer.jar
            return "$FORGE_MAVEN_URL/$mcVersion-$forgeVersion/forge-$mcVersion-$forgeVersion-installer.jar"
        }
        
        /**
         * 获取 Forge 版本 JSON URL
         * 使用 BMCLAPI 提供的 Forge 版本 JSON
         */
        fun getForgeVersionJsonUrl(mcVersion: String, forgeVersion: String): String? {
            // Forge 版本格式：1.19.3-41.2.8
            // BMCLAPI 提供版本 JSON：https://bmclapi2.bangbang93.com/maven/net/minecraftforge/forge/{version}/forge-{version}.json
            val fullVersion = "$mcVersion-$forgeVersion"
            return "$FORGE_MAVEN_URL/$fullVersion/forge-$fullVersion.json"
        }
        
        /**
         * 获取 NeoForge 安装器下载 URL
         */
        fun getNeoForgeDownloadUrl(mcVersion: String, neoForgeVersion: String): String {
            // NeoForge 版本格式：1.20.1-47.1.76
            // URL 格式：https://maven.neoforged.net/releases/net/neoforged/neoforge/1.20.1-47.1.76/neoforge-1.20.1-47.1.76-installer.jar
            return "$NEOFORGE_MAVEN_URL/neoforge/$mcVersion-$neoForgeVersion/neoforge-$mcVersion-$neoForgeVersion-installer.jar"
        }
        
        /**
         * 获取 NeoForge 版本 JSON URL
         * 使用 BMCLAPI 提供的 NeoForge 版本 JSON
         */
        fun getNeoForgeVersionJsonUrl(mcVersion: String, neoForgeVersion: String): String? {
            // NeoForge 版本格式：1.20.1-47.1.76
            // BMCLAPI 提供版本 JSON：https://bmclapi2.bangbang93.com/maven/net/neoforged/neoforge/{version}/neoforge-{version}.json
            val fullVersion = "$mcVersion-$neoForgeVersion"
            return "$NEOFORGE_MAVEN_URL/neoforge/$fullVersion/neoforge-$fullVersion.json"
        }
    }
}

/**
 * 合并游戏版本Json
 */
private fun mergeGameJson(
    info: GameDownloadInfo,
    outputFolder: File,
    clientFolder: File,
    fabricFolder: File?
) {
    //合并版本 Json
    val vanillaJson = File(clientFolder, "${info.gameVersion}.json")
    val fabricJson = fabricFolder?.let { File(it, "${it.name}.json") }
    val outputJson = File(outputFolder, "${info.customVersionName}.json")
    
    // 如果有 Fabric，使用 Fabric 配置，否则使用原版配置
    val finalJson = fabricJson?.takeIf { it.exists() } ?: vanillaJson
    
    // 复制并保存最终配置
    finalJson.copyTo(outputJson, overwrite = true)
    Logger.lInfo("Merged game JSON files")
}

/**
 * 复制原版游戏文件
 */
private fun copyVanillaFiles(
    sourceGameFolder: File,
    sourceVersion: String,
    destinationGameFolder: File,
    targetVersion: String
) {
    // 复制客户端文件
    val sourceClientFolder = File(sourceGameFolder, "versions/$sourceVersion")
    val destClientFolder = File(destinationGameFolder, "versions/$targetVersion")
    
    // 创建目标目录
    destClientFolder.mkdirs()
    
    // 复制JSON文件
    val sourceJson = File(sourceClientFolder, "$sourceVersion.json")
    val destJson = File(destClientFolder, "$targetVersion.json")
    sourceJson.copyTo(destJson, overwrite = true)
    
    // 复制JAR文件
    val sourceJar = File(sourceClientFolder, "$sourceVersion.jar")
    val destJar = File(destClientFolder, "$targetVersion.jar")
    sourceJar.copyTo(destJar, overwrite = true)
    
    Logger.lInfo("Copied vanilla files from $sourceClientFolder to $destClientFolder")
}