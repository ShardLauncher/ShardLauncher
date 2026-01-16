package com.lanrhyme.shardlauncher.game.addons.mirror

import com.lanrhyme.shardlauncher.settings.enums.MirrorSourceType

private const val ROOT = "https://bmclapi2.bangbang93.com"

enum class BMCLAPI(val url: String) {
    BASE_URL(ROOT),
    MAVEN("$ROOT/maven"),
    ASSETS("$ROOT/assets"),
    LIBRARIES("$ROOT/libraries")
}

/**
 * [Modified from HMCL](https://github.com/HMCL-dev/HMCL/blob/9aa1367/HMCLCore/src/main/java/org/jackhuang/hmcl/download/BMCLAPIDownloadProvider.java#L64-L83)
 */
private val REPLACE_MIRROR_HOLDERS = mapOf(
    Pair(BMCLAPI.BASE_URL.url, BMCLAPI.BASE_URL.url),
    Pair("https://launchermeta.mojang.com", BMCLAPI.BASE_URL.url),
    Pair("https://piston-meta.mojang.com", BMCLAPI.BASE_URL.url),
    Pair("https://piston-data.mojang.com", BMCLAPI.BASE_URL.url),
    Pair("https://launcher.mojang.com", BMCLAPI.BASE_URL.url),
    Pair("https://libraries.minecraft.net", BMCLAPI.LIBRARIES.url),
    Pair("https://resources.download.minecraft.net", BMCLAPI.ASSETS.url),
    Pair("http://files.minecraftforge.net/maven", BMCLAPI.MAVEN.url),
    Pair("https://files.minecraftforge.net/maven", BMCLAPI.MAVEN.url),
    Pair("https://maven.minecraftforge.net", BMCLAPI.MAVEN.url),
    Pair("https://maven.neoforged.net/releases/net/neoforged/forge", BMCLAPI.MAVEN.url + "/net/neoforged/forge"),
    Pair("https://maven.neoforged.net/releases/net/neoforged/neoforge", BMCLAPI.MAVEN.url + "/net/neoforged/neoforge"),
    Pair("http://dl.liteloader.com/versions/versions.json", BMCLAPI.MAVEN.url + "/com/mumfrey/liteloader/versions.json"),
    Pair("http://dl.liteloader.com/versions", BMCLAPI.MAVEN.url),
    Pair("https://meta.fabricmc.net", BMCLAPI.BASE_URL.url + "/fabric-meta"),
    Pair("https://maven.fabricmc.net", BMCLAPI.MAVEN.url),
    Pair("https://authlib-injector.yushi.moe", BMCLAPI.BASE_URL.url + "/mirrors/authlib-injector"),
    Pair("https://repo1.maven.org/maven2", "https://mirrors.cloud.tencent.com/nexus/repository/maven-public")
)

/**
 * 替换为 BMCL API 镜像源链接，若如匹配的链接，则仅返回官方链接集合
 */
fun String.mapMirrorableUrls(fileDownloadSource: MirrorSourceType = MirrorSourceType.OFFICIAL_FIRST): List<String> {
    val mirrorUrl = REPLACE_MIRROR_HOLDERS.entries.find { (key, _) ->
        this.startsWith(key)
    }?.let { (origin, mirror) ->
        this.replaceFirst(origin, mirror)
    }

    // 尊重用户的下载源设置，包括资源文件
    // 资源文件数量较多，但用户可能因网络原因需要镜像源
    return when (fileDownloadSource) {
        MirrorSourceType.OFFICIAL_FIRST -> listOfNotNull(this, mirrorUrl)
        MirrorSourceType.MIRROR_FIRST -> listOfNotNull(mirrorUrl, this)
    }
}