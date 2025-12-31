/*
 * Shard Launcher
 */

package com.lanrhyme.shardlauncher.components

import android.content.Context
import com.lanrhyme.shardlauncher.path.PathManager

class UnpackComponentsTask(context: Context, val component: Components) : UnpackSingleTask(
    context = context,
    rootDir = PathManager.DIR_COMPONENTS,
    assetsDirName = "components",
    fileDirName = component.component
)
