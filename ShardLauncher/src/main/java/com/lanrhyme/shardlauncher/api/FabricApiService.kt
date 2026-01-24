package com.lanrhyme.shardlauncher.api

import com.lanrhyme.shardlauncher.model.meta.FabricMetaResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface FabricApiService {
    @GET("v2/versions/loader/{mcVersion}")
    suspend fun getLoaderVersions(@Path("mcVersion") mcVersion: String): List<FabricMetaResponse>
}
