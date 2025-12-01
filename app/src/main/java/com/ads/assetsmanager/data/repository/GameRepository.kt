package com.ads.assetsmanager.data.repository

import com.ads.assetsmanager.data.dao.GameDao
import com.ads.assetsmanager.data.model.Category
import com.ads.assetsmanager.data.model.CategoryWithEntities
import com.ads.assetsmanager.data.model.EntityResource
import com.ads.assetsmanager.data.model.EntityWithResources
import com.ads.assetsmanager.data.model.GameEntity
import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {

    // Streams para a UI observar
    val allCategories: Flow<List<Category>> = gameDao.getAllCategories()

    fun getEntitiesByCategory(catId: Int): Flow<CategoryWithEntities> {
        return gameDao.getCategoryWithEntities(catId)
    }

    fun getResourcesByEntity(entId: Int): Flow<EntityWithResources> {
        return gameDao.getEntityWithResources(entId)
    }

    // Operações de escrita (chamadas via Coroutines no ViewModel)
    suspend fun addCategory(category: Category) = gameDao.insertCategory(category)
    suspend fun addEntity(entity: GameEntity) = gameDao.insertEntity(entity)
    suspend fun addResource(res: EntityResource) = gameDao.insertResource(res)

    suspend fun deleteCategory(category: Category) = gameDao.deleteCategory(category)
    suspend fun deleteEntity(entity: GameEntity) = gameDao.deleteEntity(entity)
    suspend fun deleteResource(resource: EntityResource) = gameDao.deleteResource(resource)
    
    // Atualizações
    suspend fun updateEntity(entity: GameEntity) = gameDao.updateEntity(entity)
    suspend fun updateEntityThumbnail(entityId: Int, thumbnailUri: String?) = gameDao.updateEntityThumbnail(entityId, thumbnailUri)
    suspend fun getEntityById(entityId: Int) = gameDao.getEntityById(entityId)
}