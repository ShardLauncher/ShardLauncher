package com.lanrhyme.shardlauncher.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lanrhyme.shardlauncher.game.account.Account
import com.lanrhyme.shardlauncher.game.account.AccountDao
import com.lanrhyme.shardlauncher.game.account.auth_server.data.AuthServer
import com.lanrhyme.shardlauncher.game.account.auth_server.data.AuthServerDao

@Database(
    entities = [Account::class, AuthServer::class],
    version = 1,
    exportSchema = false
)
// @TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    /**
     * 启动器账号
     */
    abstract fun accountDao(): AccountDao

    /**
     * 认证服务器
     */
    abstract fun authServerDao(): AuthServerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * 获取全局数据库实例
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "launcher_data.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
