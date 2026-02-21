package com.lanrhyme.shardlauncher.utils.file

interface ZipEntryBase {
    val name: String
    val isDirectory: Boolean
}