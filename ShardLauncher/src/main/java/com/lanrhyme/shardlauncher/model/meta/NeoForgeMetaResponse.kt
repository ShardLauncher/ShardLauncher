package com.lanrhyme.shardlauncher.model.meta

import com.google.gson.annotations.SerializedName

data class NeoForgeMetaResponse(
    @SerializedName("versions") val versions: List<String>
)
