package com.lanrhyme.shardlauncher.model.meta

import com.google.gson.annotations.SerializedName

data class FabricMetaResponse(
    @SerializedName("loader") val loader: FabricLoaderInfo
)

data class FabricLoaderInfo(
    @SerializedName("version") val version: String,
    @SerializedName("stable") val stable: Boolean?
)
