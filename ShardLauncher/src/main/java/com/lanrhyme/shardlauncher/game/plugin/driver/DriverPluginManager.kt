/*
 * Shard Launcher
 * Adapted from Zalith Launcher 2
 */

package com.lanrhyme.shardlauncher.game.plugin.driver

import android.content.Context
import android.content.pm.ApplicationInfo
object DriverPluginManager {
    private val driverList: MutableList<Driver> = mutableListOf()

    @JvmStatic
    fun getDriverList(): List<Driver> = driverList.toList()

    private lateinit var currentDriver: Driver
    private var preferredDriverId: String = "default"

    @JvmStatic
    fun getDriver(): Driver = currentDriver

    @JvmStatic
    fun setDriverById(driverId: String) {
        currentDriver = driverList.find { it.id == driverId } ?: driverList[0]
    }

    @JvmStatic
    fun setPreferredDriverId(driverId: String) {
        preferredDriverId = driverId
        if (::currentDriver.isInitialized) {
            setDriverById(driverId)
        }
    }

    /**
     * Initialize drivers
     */
    fun initDriver(context: Context, reset: Boolean = false) {
        if (reset) driverList.clear()
        
        val applicationInfo = context.applicationInfo
        driverList.add(
            Driver(
                id = "default",
                name = "Turnip",
                path = applicationInfo.nativeLibraryDir
            )
        )
        
        setDriverById(preferredDriverId)
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