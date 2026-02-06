package com.lanrhyme.shardlauncher.utils.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat

/**
 * 权限管理工具类
 */
object PermissionHelper {
    
    /**
     * Android 11+ 完整文件访问权限
     */
    const val PERMISSION_MANAGE_EXTERNAL_STORAGE = Manifest.permission.MANAGE_EXTERNAL_STORAGE
    
    /**
     * 读取外部存储权限
     */
    const val PERMISSION_READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
    
    /**
     * 写入外部存储权限
     */
    const val PERMISSION_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
    
    /**
     * 检查是否有完整的文件访问权限（Android 11+）
     */
    fun hasManageExternalStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true // Android 11以下不需要此权限
        }
    }
    
    /**
     * 检查是否有读取外部存储权限
     */
    fun hasReadExternalStoragePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            PERMISSION_READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 检查是否有写入外部存储权限
     */
    fun hasWriteExternalStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 不需要写入权限，使用MediaStore API
            true
        } else {
            ContextCompat.checkSelfPermission(
                context,
                PERMISSION_WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 检查是否有所有必要的文件访问权限
     */
    fun hasAllFilePermissions(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            hasManageExternalStoragePermission(context)
        } else {
            hasReadExternalStoragePermission(context) && hasWriteExternalStoragePermission(context)
        }
    }
    
    /**
     * 获取需要请求的权限列表
     */
    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            arrayOf(PERMISSION_MANAGE_EXTERNAL_STORAGE)
        } else {
            arrayOf(
                PERMISSION_READ_EXTERNAL_STORAGE,
                PERMISSION_WRITE_EXTERNAL_STORAGE
            )
        }
    }
    
    /**
     * 检查是否应该显示权限说明对话框
     */
    fun shouldShowRequestPermissionRationale(
        activity: Activity,
        permission: String
    ): Boolean {
        return activity.shouldShowRequestPermissionRationale(permission)
    }
    
    /**
     * 检查外部存储是否可用
     */
    fun isExternalStorageAvailable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
    
    /**
     * 检查外部存储是否只读
     */
    fun isExternalStorageReadOnly(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED_READ_ONLY
    }
    
    /**
     * 获取外部存储目录
     */
    fun getExternalStorageDirectory(): java.io.File {
        return Environment.getExternalStorageDirectory()
    }
    
    /**
     * 检查应用是否可以访问指定路径
     */
    fun canAccessPath(context: Context, path: java.io.File): Boolean {
        return try {
            path.canRead() && path.canWrite()
        } catch (e: SecurityException) {
            false
        }
    }
}