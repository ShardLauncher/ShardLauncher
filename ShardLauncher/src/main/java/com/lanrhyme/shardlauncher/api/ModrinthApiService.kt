package com.lanrhyme.shardlauncher.api

import com.lanrhyme.shardlauncher.model.ModrinthVersion
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ModrinthApiService {
    @GET("v2/project/{id}/version")
    suspend fun getProjectVersions(
        @Path("id") projectId: String,
        @Query("loaders") loaders: String? = null,
        @Query("game_versions") gameVersions: String? = null
    ): List<ModrinthVersion>
}
