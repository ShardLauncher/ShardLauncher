package com.lanrhyme.shardlauncher.game.account.wardrobe

/**
 * 皮肤模型枚举
 */
enum class SkinModelType(val string: String, val targetParity: Int, val modelType: String) {
    /** 未设定 */
    NONE("none", -1, ""),
    /** 粗臂类型 */
    STEVE("wide", 0, "classic"),
    /** 细臂类型 */
    ALEX("slim", 1, "slim")
}
