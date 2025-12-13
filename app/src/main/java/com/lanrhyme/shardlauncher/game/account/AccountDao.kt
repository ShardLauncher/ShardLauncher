package com.lanrhyme.shardlauncher.game.account

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts")
    suspend fun getAllAccounts(): List<Account>

    @Query("SELECT * FROM accounts WHERE uniqueUUID = :uuid")
    suspend fun getAccount(uuid: String): Account?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAccount(account: Account)

    @Delete
    suspend fun deleteAccount(account: Account)

    @Query("SELECT * FROM accounts WHERE profileId = :profileId")
    suspend fun getAccountByProfileId(profileId: String): Account?
}
