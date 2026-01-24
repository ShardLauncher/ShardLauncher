package com.lanrhyme.shardlauncher.model.meta

import com.google.gson.annotations.SerializedName

data class QuiltMetaResponse(
    @SerializedName("loader") val loader: QuiltLoaderInfo
)

data class QuiltLoaderInfo(
    @SerializedName("version") val version: String
)
