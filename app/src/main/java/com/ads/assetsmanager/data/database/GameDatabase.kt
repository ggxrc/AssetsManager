package com.ads.assetsmanager.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ads.assetsmanager.data.dao.GameDao
import com.ads.assetsmanager.data.model.Category
import com.ads.assetsmanager.data.model.EntityResource
import com.ads.assetsmanager.data.model.GameEntity

@Database(
    entities = [Category::class, GameEntity::class, EntityResource::class],
    version = 1,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getDatabase(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "game_assets_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}