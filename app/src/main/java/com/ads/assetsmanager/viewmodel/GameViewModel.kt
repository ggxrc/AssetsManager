package com.ads.assetsmanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.ads.assetsmanager.data.model.Category
import com.ads.assetsmanager.data.model.CategoryWithEntities
import com.ads.assetsmanager.data.model.EntityResource
import com.ads.assetsmanager.data.model.EntityWithResources
import com.ads.assetsmanager.data.model.GameEntity
import com.ads.assetsmanager.data.repository.GameRepository
import kotlinx.coroutines.launch

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    // --- Fontes de Dados (Observáveis) ---
    // Converte o Flow do Room para LiveData para facilitar o uso no XML/Activity
    val allCategories: LiveData<List<Category>> = repository.allCategories.asLiveData()

    // Usamos Transformations.switchMap para buscar dados baseados em um ID dinâmico
    private val _currentCategoryId = MutableLiveData<Int>()
    val entitiesByCategory: LiveData<CategoryWithEntities> = _currentCategoryId.switchMap { id ->
        repository.getEntitiesByCategory(id).asLiveData()
    }

    private val _currentEntityId = MutableLiveData<Int>()
    val resourcesByEntity: LiveData<EntityWithResources> = _currentEntityId.switchMap { id ->
        repository.getResourcesByEntity(id).asLiveData()
    }

    // --- Ações de Navegação (Setters) ---
    fun selectCategory(categoryId: Int) {
        _currentCategoryId.value = categoryId
    }

    fun selectEntity(entityId: Int) {
        _currentEntityId.value = entityId
    }

    // --- Operações de Escrita (Coroutines) ---
    fun insertCategory(name: String) = viewModelScope.launch {
        repository.addCategory(Category(name = name))
    }

    fun insertEntity(categoryId: Int, name: String, desc: String) = viewModelScope.launch {
        repository.addEntity(GameEntity(categoryId = categoryId, name = name, description = desc))
    }

    fun insertResource(ownerId: Int, type: String, value: String, label: String) = viewModelScope.launch {
        repository.addResource(
            EntityResource(
                ownerId = ownerId,
                type = type,
                value = value,
                label = label
            )
        )
    }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        repository.deleteCategory(category)
    }

    fun deleteEntity(entity: GameEntity) = viewModelScope.launch {
        repository.deleteEntity(entity)
    }

    fun deleteResource(resource: EntityResource) = viewModelScope.launch {
        repository.deleteResource(resource)
    }
}