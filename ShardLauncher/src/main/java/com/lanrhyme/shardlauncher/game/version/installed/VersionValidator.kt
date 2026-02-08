/*
 * Shard Launcher
 * 版本验证工具
 */

package com.lanrhyme.shardlauncher.game.version.installed

import com.google.gson.JsonParser
import com.lanrhyme.shardlauncher.utils.GSON
import com.lanrhyme.shardlauncher.utils.logging.Logger
import java.io.File

/**
 * 版本验证结果
 */
data class ValidationResult(
    val isValid: Boolean,
    val issues: List<VersionIssue> = emptyList()
) {
    val hasWarnings: Boolean
        get() = issues.any { it.severity == IssueSeverity.WARNING }

    val hasErrors: Boolean
        get() = issues.any { it.severity == IssueSeverity.ERROR }
}

/**
 * 版本问题
 */
data class VersionIssue(
    val severity: IssueSeverity,
    val message: String,
    val code: String
)

/**
 * 问题严重程度
 */
enum class IssueSeverity {
    WARNING,  // 警告，版本可能可以使用
    ERROR     // 错误，版本无法使用
}

/**
 * 版本验证工具
 */
object VersionValidator {
    // 最小有效的JAR文件大小（1KB）
    private const val MIN_JAR_SIZE = 1024L

    /**
     * 从JSON文件解析VersionInfo
     */
    private fun parseJsonToVersionInfo(jsonFile: File): VersionInfo? {
        return try {
            val jsonText = jsonFile.readText()
            val jsonElement = JsonParser.parseString(jsonText)
            
            // 提取基本信息
            val id = jsonElement.asJsonObject.get("id")?.asString ?: return null
            val mcVersion = jsonElement.asJsonObject.get("inheritsFrom")?.asString 
                ?: jsonElement.asJsonObject.get("id")?.asString 
                ?: ""
            
            // 尝试提取loader信息（如果有）
            val loaderInfo = try {
                val loaderTypeStr = jsonElement.asJsonObject.get("loaderType")?.asString
                val loaderVersion = jsonElement.asJsonObject.get("loaderVersion")?.asString ?: ""
                
                if (loaderTypeStr != null) {
                    val loaderType = when (loaderTypeStr.lowercase()) {
                        "fabric" -> com.lanrhyme.shardlauncher.game.addons.modloader.ModLoader.FABRIC
                        "forge" -> com.lanrhyme.shardlauncher.game.addons.modloader.ModLoader.FORGE
                        "neoforge" -> com.lanrhyme.shardlauncher.game.addons.modloader.ModLoader.NEOFORGE
                        "quilt" -> com.lanrhyme.shardlauncher.game.addons.modloader.ModLoader.QUILT
                        else -> com.lanrhyme.shardlauncher.game.addons.modloader.ModLoader.FORGE
                    }
                    VersionInfo.LoaderInfo(loaderType, loaderVersion)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
            
            VersionInfo(
                minecraftVersion = mcVersion,
                quickPlay = VersionInfo.QuickPlay(false, false, false),
                loaderInfo = loaderInfo
            )
        } catch (e: Exception) {
            Logger.e("VersionValidator", "Failed to parse VersionInfo from JSON", e)
            null
        }
    }

    /**
     * 验证版本完整性
     * @param versionPath 版本文件夹路径
     * @return 验证结果
     */
    fun validateVersion(versionPath: File): ValidationResult {
        val issues = mutableListOf<VersionIssue>()
        val versionName = versionPath.name

        // 1. 检查版本文件夹是否存在
        if (!versionPath.exists()) {
            issues.add(VersionIssue(
                IssueSeverity.ERROR,
                "版本文件夹不存在: ${versionPath.absolutePath}",
                "FOLDER_NOT_EXISTS"
            ))
            return ValidationResult(false, issues)
        }

        if (!versionPath.isDirectory) {
            issues.add(VersionIssue(
                IssueSeverity.ERROR,
                "版本路径不是文件夹: ${versionPath.absolutePath}",
                "NOT_A_FOLDER"
            ))
            return ValidationResult(false, issues)
        }

        // 2. 检查JSON文件
        val jsonFile = File(versionPath, "$versionName.json")
        if (!jsonFile.exists()) {
            issues.add(VersionIssue(
                IssueSeverity.ERROR,
                "版本JSON文件不存在: $versionName.json",
                "JSON_NOT_EXISTS"
            ))
            return ValidationResult(false, issues)
        }

        if (!jsonFile.isFile) {
            issues.add(VersionIssue(
                IssueSeverity.ERROR,
                "版本JSON路径不是文件: ${jsonFile.absolutePath}",
                "JSON_NOT_FILE"
            ))
            return ValidationResult(false, issues)
        }

        // 3. 验证JSON格式
        val jsonValidation = validateJsonFile(jsonFile)
        issues.addAll(jsonValidation.issues)

        // 4. 检查JAR文件
        val jarFile = File(versionPath, "$versionName.jar")
        if (!jarFile.exists()) {
            issues.add(VersionIssue(
                IssueSeverity.ERROR,
                "版本JAR文件不存在: $versionName.jar",
                "JAR_NOT_EXISTS"
            ))
        } else {
            if (!jarFile.isFile) {
                issues.add(VersionIssue(
                    IssueSeverity.ERROR,
                    "版本JAR路径不是文件: ${jarFile.absolutePath}",
                    "JAR_NOT_FILE"
                ))
            } else {
                // 检查JAR文件大小
                if (jarFile.length() < MIN_JAR_SIZE) {
                    issues.add(VersionIssue(
                        IssueSeverity.ERROR,
                        "版本JAR文件大小异常: ${jarFile.length()} bytes (最小: $MIN_JAR_SIZE bytes)",
                        "JAR_SIZE_INVALID"
                    ))
                }
            }
        }

        // 5. 检查libraries文件夹（可选）
        val librariesDir = File(versionPath, "libraries")
        if (librariesDir.exists()) {
            if (!librariesDir.isDirectory) {
                issues.add(VersionIssue(
                    IssueSeverity.WARNING,
                    "libraries路径不是文件夹: ${librariesDir.absolutePath}",
                    "LIBRARIES_NOT_DIR"
                ))
            }
        }

        // 6. 检查natives文件夹（可选）
        val nativesDir = File(versionPath, "natives")
        if (nativesDir.exists()) {
            if (!nativesDir.isDirectory) {
                issues.add(VersionIssue(
                    IssueSeverity.WARNING,
                    "natives路径不是文件夹: ${nativesDir.absolutePath}",
                    "NATIVES_NOT_DIR"
                ))
            }
        }

        // 7. 尝试解析VersionInfo
        try {
            val versionInfo = parseJsonToVersionInfo(jsonFile)
            if (versionInfo == null) {
                issues.add(VersionIssue(
                    IssueSeverity.WARNING,
                    "无法解析版本信息",
                    "VERSION_INFO_PARSE_FAILED"
                ))
            } else {
                // 验证Minecraft版本号
                if (versionInfo.minecraftVersion.isBlank()) {
                    issues.add(VersionIssue(
                        IssueSeverity.WARNING,
                        "Minecraft版本号为空",
                        "MC_VERSION_EMPTY"
                    ))
                }
            }
        } catch (e: Exception) {
            issues.add(VersionIssue(
                IssueSeverity.WARNING,
                "解析版本信息时出错: ${e.message}",
                "VERSION_INFO_PARSE_ERROR"
            ))
        }

        val isValid = !issues.any { it.severity == IssueSeverity.ERROR }

        if (isValid) {
            Logger.d("VersionValidator", "版本验证通过: $versionName")
        } else {
            Logger.w("VersionValidator", "版本验证失败: $versionName, 问题: ${issues.joinToString("; ") { it.message }}")
        }

        return ValidationResult(isValid, issues)
    }

    /**
     * 验证JSON文件格式
     */
    private fun validateJsonFile(jsonFile: File): ValidationResult {
        val issues = mutableListOf<VersionIssue>()

        try {
            val jsonText = jsonFile.readText()
            if (jsonText.isBlank()) {
                issues.add(VersionIssue(
                    IssueSeverity.ERROR,
                    "JSON文件为空",
                    "JSON_EMPTY"
                ))
                return ValidationResult(false, issues)
            }

            // 尝试解析JSON
            val jsonElement = JsonParser.parseString(jsonText)

            // 验证必需字段
            if (!jsonElement.isJsonObject) {
                issues.add(VersionIssue(
                    IssueSeverity.ERROR,
                    "JSON格式错误: 不是JSON对象",
                    "JSON_NOT_OBJECT"
                ))
                return ValidationResult(false, issues)
            }

            val jsonObject = jsonElement.asJsonObject

            // 检查必需字段
            val requiredFields = listOf("id", "type", "time")
            for (field in requiredFields) {
                if (!jsonObject.has(field)) {
                    issues.add(VersionIssue(
                        IssueSeverity.WARNING,
                        "JSON缺少必需字段: $field",
                        "MISSING_FIELD_$field"
                    ))
                }
            }

            // 检查id字段是否与文件夹名匹配
            if (jsonObject.has("id")) {
                val jsonId = jsonObject.get("id").asString
                val folderName = jsonFile.nameWithoutExtension
                if (jsonId != folderName) {
                    issues.add(VersionIssue(
                        IssueSeverity.WARNING,
                        "JSON中的id($jsonId)与文件夹名($folderName)不匹配",
                        "ID_MISMATCH"
                    ))
                }
            }

        } catch (e: Exception) {
            issues.add(VersionIssue(
                IssueSeverity.ERROR,
                "JSON解析失败: ${e.message}",
                "JSON_PARSE_ERROR"
            ))
        }

        val isValid = !issues.any { it.severity == IssueSeverity.ERROR }
        return ValidationResult(isValid, issues)
    }

    /**
     * 快速验证（仅检查必要文件）
     * @param versionPath 版本文件夹路径
     * @return 是否有效
     */
    fun quickValidate(versionPath: File): Boolean {
        val versionName = versionPath.name
        val jsonFile = File(versionPath, "$versionName.json")
        val jarFile = File(versionPath, "$versionName.jar")

        return jsonFile.exists() && jsonFile.isFile &&
               jarFile.exists() && jarFile.isFile &&
               jarFile.length() >= MIN_JAR_SIZE
    }

    /**
     * 获取版本状态描述
     * @param validation 验证结果
     * @return 状态描述
     */
    fun getStatusDescription(validation: ValidationResult): String {
        return when {
            !validation.isValid -> "无效"
            validation.hasErrors -> "有错误"
            validation.hasWarnings -> "有警告"
            else -> "正常"
        }
    }
}