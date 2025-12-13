package com.lanrhyme.shardlauncher.game.account.auth_server.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AuthServerDao {
    @Query("SELECT * FROM servers")
    suspend fun getAllServers(): List<AuthServer>

    @Query("SELECT * FROM servers WHERE baseUrl = :baseUrl")
    suspend fun getServer(baseUrl: String): AuthServer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveServer(server: AuthServer)

    @Delete
    suspend fun deleteServer(server: AuthServer)
}
