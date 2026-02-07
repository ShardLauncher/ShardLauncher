/*
 * Shard Launcher
 * 版本导入导出工具
 */

package com.lanrhyme.shardlauncher.game.version.installed

import com.lanrhyme.shardlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * 获取版本类型
 */
fun Version.getVersionType(): VersionType {
    return if (getVersionInfo()?.loaderInfo != null) {
        VersionType.MODLOADERS
    } else {
        VersionType.VANILLA
    }
}

/**
 * 导出结果
 */
data class ExportResult(
    val success: Boolean,
    val message: String,
    val outputFile: File? = null
)

/**
 * 导入结果
 */
data class ImportResult(
    val success: Boolean,
    val message: String,
    val importedVersionName: String? = null
)

/**
 * 版本导入导出工具
 */
object VersionImporterExporter {
    // 版本导出元数据文件名
    private const val METADATA_FILE = "shard_version_metadata.json"

    /**
     * 导出版本为ZIP文件
     * @param version 要导出的版本
     * @param outputFile 输出文件
     * @param includeLibraries 是否包含libraries（默认false，节省空间）
     * @param includeNatives 是否包含natives（默认false，节省空间）
     * @return 导出结果
     */
    suspend fun exportVersion(
        version: Version,
        outputFile: File,
        includeLibraries: Boolean = false,
        includeNatives: Boolean = false
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val versionPath = version.getVersionPath()
            val versionName = version.getVersionName()

            // 验证版本
            if (!versionPath.exists()) {
                return@withContext ExportResult(
                    false,
                    "版本文件夹不存在: ${versionPath.absolutePath}"
                )
            }

            // 确保输出目录存在
            outputFile.parentFile?.mkdirs()

            // 创建ZIP输出流
            ZipOutputStream(BufferedOutputStream(FileOutputStream(outputFile))).use { zos ->
                // 添加版本元数据
                val metadata = """
                    {
                        "versionName": "$versionName",
                        "minecraftVersion": "${version.getVersionInfo()?.minecraftVersion ?: ""}",
                        "versionType": "${version.getVersionType()}",
                        "exportTime": ${System.currentTimeMillis()},
                        "includeLibraries": $includeLibraries,
                        "includeNatives": $includeNatives
                    }
                """.trimIndent()

                zos.putNextEntry(ZipEntry(METADATA_FILE))
                zos.write(metadata.toByteArray())
                zos.closeEntry()

                // 递归添加版本文件夹中的文件
                addDirectoryToZip(zos, versionPath, "", includeLibraries, includeNatives)
            }

            val fileSize = outputFile.length() / (1024 * 1024) // MB
            Logger.i("VersionExporter", "Version exported successfully: $versionName (${fileSize}MB) -> ${outputFile.absolutePath}")

            ExportResult(
                true,
                "导出成功: $versionName (${fileSize}MB)",
                outputFile
            )
        } catch (e: Exception) {
            Logger.e("VersionExporter", "Failed to export version: ${version.getVersionName()}", e)
            ExportResult(
                false,
                "导出失败: ${e.message}"
            )
        }
    }

    /**
     * 从ZIP文件导入版本
     * @param zipFile ZIP文件
     * @param targetVersionsPath 目标versions目录
     * @param newVersionName 新版本名称（可选，如果为空则使用ZIP中的名称）
     * @return 导入结果
     */
    suspend fun importVersion(
        zipFile: File,
        targetVersionsPath: File,
        newVersionName: String? = null
    ): ImportResult = withContext(Dispatchers.IO) {
        try {
            // 验证ZIP文件
            if (!zipFile.exists() || !zipFile.isFile) {
                return@withContext ImportResult(
                    false,
                    "ZIP文件不存在或不是文件: ${zipFile.absolutePath}"
                )
            }

            // 确保目标目录存在
            if (!targetVersionsPath.exists()) {
                targetVersionsPath.mkdirs()
            }

            var metadata: VersionMetadata? = null
            var extractedVersionName: String? = null

            // 解压ZIP文件
            ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
                var entry: ZipEntry?
                while (zis.nextEntry.also { entry = it } != null) {
                    val entryName = entry!!.name

                    // 解析元数据
                    if (entryName == METADATA_FILE) {
                        val metadataContent = zis.readBytes().toString(Charsets.UTF_8)
                        metadata = parseMetadata(metadataContent)
                        extractedVersionName = metadata?.versionName
                    } else {
                        // 解压文件
                        val outputFile = File(targetVersionsPath, entryName)
                        
                        // 创建父目录
                        outputFile.parentFile?.mkdirs()

                        // 解压文件
                        if (!entry.isDirectory) {
                            BufferedOutputStream(FileOutputStream(outputFile)).use { bos ->
                                zis.copyTo(bos)
                            }
                        }
                    }

                    zis.closeEntry()
                }
            }

            // 确定最终版本名称
            val finalVersionName = newVersionName ?: (extractedVersionName ?: extractVersionNameFromZip(zipFile.name))
            
            // 检查版本是否已存在
            val targetVersionPath = File(targetVersionsPath, finalVersionName)
            if (targetVersionPath.exists()) {
                return@withContext ImportResult(
                    false,
                    "版本已存在: $finalVersionName"
                )
            }

            // 重命名版本文件夹（如果需要）
            if (extractedVersionName != null && extractedVersionName != finalVersionName) {
                val oldPath = File(targetVersionsPath, extractedVersionName)
                if (oldPath.exists() && oldPath != targetVersionPath) {
                    oldPath.renameTo(targetVersionPath)
                }
            }

            // 重命名JSON和JAR文件（如果需要）
            if (extractedVersionName != null && extractedVersionName != finalVersionName) {
                val oldJson = File(targetVersionPath, "$extractedVersionName.json")
                val newJson = File(targetVersionPath, "$finalVersionName.json")
                if (oldJson.exists()) {
                    oldJson.renameTo(newJson)
                }

                val oldJar = File(targetVersionPath, "$extractedVersionName.jar")
                val newJar = File(targetVersionPath, "$finalVersionName.jar")
                if (oldJar.exists()) {
                    oldJar.renameTo(newJar)
                }
            }

            // 验证导入的版本
            val validation = VersionValidator.validateVersion(targetVersionPath)
            if (!validation.isValid) {
                Logger.w("VersionImporter", "Imported version validation failed: ${validation.issues.joinToString("; ") { it.message }}")
            }

            Logger.i("VersionImporter", "Version imported successfully: $finalVersionName from ${zipFile.name}")

            ImportResult(
                true,
                "导入成功: $finalVersionName${if (metadata != null) " (MC ${metadata.minecraftVersion})" else ""}",
                finalVersionName
            )
        } catch (e: Exception) {
            Logger.e("VersionImporter", "Failed to import version from: ${zipFile.name}", e)
            ImportResult(
                false,
                "导入失败: ${e.message}"
            )
        }
    }

    /**
     * 递归添加目录到ZIP
     */
    private fun addDirectoryToZip(
        zos: ZipOutputStream,
        dir: File,
        basePath: String,
        includeLibraries: Boolean,
        includeNatives: Boolean
    ) {
        dir.listFiles()?.forEach { file ->
            val entryName = if (basePath.isEmpty()) file.name else "$basePath/${file.name}"

            // 跳过某些目录
            if (!includeLibraries && file.name == "libraries") return
            if (!includeNatives && file.name == "natives") return

            if (file.isDirectory) {
                // 递归处理子目录
                zos.putNextEntry(ZipEntry("$entryName/"))
                zos.closeEntry()
                addDirectoryToZip(zos, file, entryName, includeLibraries, includeNatives)
            } else {
                // 添加文件
                zos.putNextEntry(ZipEntry(entryName))
                BufferedInputStream(FileInputStream(file)).use { bis ->
                    bis.copyTo(zos)
                }
                zos.closeEntry()
            }
        }
    }

    /**
     * 从ZIP文件名提取版本名
     */
    private fun extractVersionNameFromZip(zipFileName: String): String {
        return zipFileName
            .removeSuffix(".zip")
            .removeSuffix(".slpack")
            .replace("[^a-zA-Z0-9_\\-.]".toRegex(), "_")
    }

    /**
     * 解析版本元数据
     */
    private fun parseMetadata(metadataJson: String): VersionMetadata? {
        return try {
            com.lanrhyme.shardlauncher.utils.GSON.fromJson(metadataJson, VersionMetadata::class.java)
        } catch (e: Exception) {
            Logger.w("VersionImporter", "Failed to parse metadata: ${e.message}")
            null
        }
    }

    /**
     * 版本元数据
     */
    data class VersionMetadata(
        val versionName: String,
        val minecraftVersion: String,
        val versionType: String,
        val exportTime: Long,
        val includeLibraries: Boolean,
        val includeNatives: Boolean
    )
}
