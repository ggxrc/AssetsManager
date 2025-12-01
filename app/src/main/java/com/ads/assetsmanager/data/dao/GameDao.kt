package com.ads.assetsmanager.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ads.assetsmanager.data.model.Category
import com.ads.assetsmanager.data.model.CategoryWithEntities
import com.ads.assetsmanager.data.model.EntityResource
import com.ads.assetsmanager.data.model.EntityWithResources
import com.ads.assetsmanager.data.model.GameEntity
import kotlinx.coroutines.flow.Flow

// Arquivo: GameDao.kt
@Dao
interface GameDao {

    // --- Create ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntity(gameEntity: GameEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: EntityResource): Long

    // --- Read (Reactive with Flow) ---
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    // Busca Categoria e suas Entidades (Nível 1 -> Nível 2)
    @Transaction
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    fun getCategoryWithEntities(categoryId: Int): Flow<CategoryWithEntities>

    // Busca Entidade e seus Recursos (Nível 2 -> Nível 3)
    @Transaction
    @Query("SELECT * FROM game_entities WHERE entityId = :entityId")
    fun getEntityWithResources(entityId: Int): Flow<EntityWithResources>

    // --- Update ---
    @Update
    suspend fun updateCategory(category: Category)

    @Update
    suspend fun updateEntity(gameEntity: GameEntity)

    @Update
    suspend fun updateResource(resource: EntityResource)

    // --- Delete ---
    @Delete
    suspend fun deleteCategory(category: Category)

    @Delete
    suspend fun deleteEntity(gameEntity: GameEntity)

    @Delete
    suspend fun deleteResource(resource: EntityResource)
}