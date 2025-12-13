package com.lanrhyme.shardlauncher.game.account.wardrobe

import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

fun legacyStrFill(str: String, code: Char, length: Int): String {
    return if (str.length > length) {
        str.take(length)
    } else {
        str.padEnd(length, code).drop(str.length) + str
    }
}

private fun getLocalUuid(name: String): String {
    val lenHex = name.length.toString(16)
    val lengthPart = legacyStrFill(lenHex, '0', 16)

    val hashCode = name.hashCode().toLong() and 0xFFFFFFFFL
    val hashHex = hashCode.toString(16)
    val hashPart = legacyStrFill(hashHex, '0', 16) //确保最长16位

    return buildString(34) {
        append(lengthPart.substring(0, 12))
        append('3')
        append(lengthPart.substring(13, 16))
        append('9')
        append(hashPart.substring(0, 15))
    }
}

/**
 * 根据皮肤模型类型，生成 profileId
 */
fun getLocalUUIDWithSkinModel(userName: String, skinModelType: SkinModelType): String {
    val baseUuid = getLocalUuid(userName)
    if (skinModelType == SkinModelType.NONE) return baseUuid

    val prefix = baseUuid.substring(0, 27)
    val a = baseUuid[7].digitToInt(16)
    val b = baseUuid[15].digitToInt(16)
    val c = baseUuid[23].digitToInt(16)

    var suffix = baseUuid.substring(27).toLong(16)
    val maxSuffix = 0xFFFFFL

    repeat(maxSuffix.toInt() + 1) {
        val currentD = (suffix and 0xFL).toInt()
        if ((a xor b xor c xor currentD) % 2 == skinModelType.targetParity) {
            return prefix + suffix.toString(16).padStart(5, '0').uppercase()
        }
        suffix = if (suffix == maxSuffix) 0L else suffix + 1
    }

    return prefix + suffix.toString(16).padStart(5, '0').uppercase()
}

/**
 * 检查皮肤像素合法性，Minecraft仅支持使用64x64或64x32像素的皮肤
 */
suspend fun validateSkinFile(skinFile: File): Boolean {
    return withContext(Dispatchers.IO) {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(skinFile.absolutePath, options)

        val width = options.outWidth
        val height = options.outHeight

        //像素尺寸是否满足 64x64 或 32x32
        (width == 64 && height == 32) || (width == 64 && height == 64)
    }
}
