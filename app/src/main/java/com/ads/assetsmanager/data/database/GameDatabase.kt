package com.ads.assetsmanager.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ads.assetsmanager.data.dao.GameDao
import com.ads.assetsmanager.data.model.Category
import com.ads.assetsmanager.data.model.EntityResource
import com.ads.assetsmanager.data.model.GameEntity

@Database(
    entities = [Category::class, GameEntity::class, EntityResource::class],
    version = 3,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null
        
        // Migração da versão 1 para 2: adiciona thumbnailUri e lore na GameEntity
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE game_entities ADD COLUMN thumbnailUri TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE game_entities ADD COLUMN lore TEXT DEFAULT NULL")
            }
        }
        
        // Migração da versão 2 para 3: adiciona mimeType, fileName e fileSize no EntityResource
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE entity_resources ADD COLUMN mimeType TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE entity_resources ADD COLUMN fileName TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE entity_resources ADD COLUMN fileSize INTEGER DEFAULT NULL")
            }
        }

        fun getDatabase(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "game_assets_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}