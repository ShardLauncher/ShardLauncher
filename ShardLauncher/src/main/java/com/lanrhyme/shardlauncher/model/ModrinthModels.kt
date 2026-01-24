package com.lanrhyme.shardlauncher.model

import com.google.gson.annotations.SerializedName

data class ModrinthVersion(
    @SerializedName("name") val name: String,
    @SerializedName("version_number") val versionNumber: String,
    @SerializedName("game_versions") val gameVersions: List<String>,
    @SerializedName("loaders") val loaders: List<String>,
    @SerializedName("id") val id: String,
    @SerializedName("project_id") val projectId: String,
    @SerializedName("date_published") val datePublished: String,
    @SerializedName("files") val files: List<ModrinthFile>
)

data class ModrinthFile(
    @SerializedName("url") val url: String,
    @SerializedName("filename") val filename: String,
    @SerializedName("primary") val primary: Boolean,
    @SerializedName("hashes") val hashes: ModrinthHashes,
    @SerializedName("size") val size: Long
)

data class ModrinthHashes(
    @SerializedName("sha1") val sha1: String,
    @SerializedName("sha512") val sha512: String
)
