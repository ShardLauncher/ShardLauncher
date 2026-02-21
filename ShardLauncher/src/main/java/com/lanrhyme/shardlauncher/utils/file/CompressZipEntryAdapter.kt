package com.lanrhyme.shardlauncher.utils.file

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry

class CompressZipEntryAdapter(val entry: ZipArchiveEntry) : ZipEntryBase {
    override val name: String = entry.name
    override val isDirectory: Boolean = entry.isDirectory
}