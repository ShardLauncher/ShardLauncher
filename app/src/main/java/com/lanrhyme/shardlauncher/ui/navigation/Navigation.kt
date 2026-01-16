package com.lanrhyme.shardlauncher.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "主页", Icons.Filled.Home)
    object Version : Screen("version", "版本", Icons.Filled.Info)
    object Download : Screen("download", "下载", Icons.Filled.Download)
    object Online : Screen("online", "联机", Icons.Filled.Cloud)
    object Settings : Screen("settings", "设置", Icons.Filled.Settings)
    object DeveloperOptions : Screen("developer_options", "开发者选项", Icons.Filled.DeveloperMode)
    object Account : Screen("account", "账户", Icons.Filled.AccountCircle)
}

val navigationItems = listOf(
    Screen.Home,
    Screen.Version,
    Screen.Download,
    Screen.Online,
    Screen.Settings
)

val routeHierarchy = mapOf(
    Screen.DeveloperOptions.route to Screen.Settings.route,
    "component_demo" to Screen.DeveloperOptions.route,
    "log_viewer" to Screen.DeveloperOptions.route,
    "version_detail/{versionId}" to Screen.Download.route,
    Screen.Account.route to Screen.Home.route
)

fun getRootRoute(route: String?): String? {
    if (route == null) return null
    var currentRoute = route
    while (routeHierarchy.containsKey(currentRoute)) {
        currentRoute = routeHierarchy[currentRoute]!!
    }
    return currentRoute
}
