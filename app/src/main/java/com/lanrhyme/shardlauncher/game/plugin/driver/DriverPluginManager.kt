/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.plugin.driver

import android.content.Context
import android.content.pm.ApplicationInfo
import com.lanrhyme.shardlauncher.settings.AllSettings

/**
 * Driver plugin manager for handling GPU drivers
 */
object DriverPluginManager {
    private val driverList: MutableList<Driver> = mutableListOf()

    @JvmStatic
    fun getDriverList(): List<Driver> = driverList.toList()

    private lateinit var currentDriver: Driver

    @JvmStatic
    fun setDriverById(driverId: String) {
        currentDriver = driverList.find { it.id == driverId } ?: driverList[0]
    }

    @JvmStatic
    fun getDriver(): Driver = currentDriver

    /**
     * Initialize drivers
     */
    fun initDriver(context: Context, reset: Boolean = false) {
        if (reset) driverList.clear()
        
        val applicationInfo = context.applicationInfo
        driverList.add(
            Driver(
                id = AllSettings.vulkanDriver.defaultValue,
                name = "Turnip",
                path = applicationInfo.nativeLibraryDir
            )
        )
        
        setDriverById(AllSettings.vulkanDriver.getValue())
    }

    /**
     * Parse driver plugin from APK
     */
    fun parsePlugin(
        context: Context,
        info: ApplicationInfo,
        loaded: (Driver) -> Unit = {}
    ) {
        if (info.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
            val metaData = info.metaData ?: return
            if (metaData.getBoolean("fclPlugin", false)) {
                val driver = metaData.getString("driver") ?: return
                val nativeLibraryDir = info.nativeLibraryDir

                val packageManager = context.packageManager
                val packageName = info.packageName
                val appName = info.loadLabel(packageManager).toString()

                val driverPlugin = Driver(
                    id = packageName,
                    name = driver,
                    summary = "From plugin: $appName",
                    path = nativeLibraryDir
                )
                
                driverList.add(driverPlugin)
                loaded(driverPlugin)
            }
        }
    }
}

/**
 * Represents a GPU driver
 */
data class Driver(
    val id: String,
    val name: String,
    val summary: String = "",
    val path: String
)