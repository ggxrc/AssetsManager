package com.ads.assetsmanager.viewmodel

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    // --- Fontes de Dados (Observáveis) ---
    val allCategories: LiveData<List<Category>> = repository.allCategories.asLiveData()

    private val _currentCategoryId = MutableLiveData<Int>()
    val entitiesByCategory: LiveData<CategoryWithEntities> = _currentCategoryId.switchMap { id ->
        repository.getEntitiesByCategory(id).asLiveData()
    }

    private val _currentEntityId = MutableLiveData<Int>()
    val resourcesByEntity: LiveData<EntityWithResources> = _currentEntityId.switchMap { id ->
        repository.getResourcesByEntity(id).asLiveData()
    }
    
    // Estado de exportação
    private val _exportState = MutableLiveData<ExportState>()
    val exportState: LiveData<ExportState> = _exportState

    // --- Ações de Navegação ---
    fun selectCategory(categoryId: Int) {
        _currentCategoryId.value = categoryId
    }

    fun selectEntity(entityId: Int) {
        _currentEntityId.value = entityId
    }

    // --- Operações de Escrita ---
    fun insertCategory(name: String) = viewModelScope.launch {
        repository.addCategory(Category(name = name))
    }

    fun insertEntity(categoryId: Int, name: String, desc: String, lore: String? = null) = viewModelScope.launch {
        repository.addEntity(GameEntity(categoryId = categoryId, name = name, description = desc, lore = lore))
    }

    fun insertResource(
        ownerId: Int,
        type: String,
        value: String,
        label: String,
        mimeType: String? = null,
        fileName: String? = null,
        fileSize: Long? = null
    ) = viewModelScope.launch {
        repository.addResource(
            EntityResource(
                ownerId = ownerId,
                type = type,
                value = value,
                label = label,
                mimeType = mimeType,
                fileName = fileName,
                fileSize = fileSize
            )
        )
    }
    
    // Define um sprite como perfil/thumbnail da entidade
    fun setAsEntityThumbnail(entityId: Int, imageUri: String) = viewModelScope.launch {
        repository.updateEntityThumbnail(entityId, imageUri)
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
    
    // --- Exportação ---
    fun exportEntityAsZip(
        context: Context,
        entity: GameEntity,
        resources: List<EntityResource>,
        outputUri: Uri
    ) = viewModelScope.launch {
        _exportState.value = ExportState.Loading
        try {
            withContext(Dispatchers.IO) {
                context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                    ZipOutputStream(outputStream).use { zipOut ->
                        // Criar metadata.json
                        val metadata = buildMetadataJson(entity, resources)
                        zipOut.putNextEntry(ZipEntry("metadata.json"))
                        zipOut.write(metadata.toByteArray())
                        zipOut.closeEntry()
                        
                        // Adicionar cada recurso
                        resources.forEach { resource ->
                            if (resource.type != "TEXT" && resource.type != "LINK") {
                                try {
                                    val resourceUri = Uri.parse(resource.value)
                                    val folder = when (resource.type) {
                                        "SPRITE", "IMAGE" -> "sprites/"
                                        "ANIMATION" -> "animations/"
                                        "AUDIO" -> "audio/"
                                        else -> "other/"
                                    }
                                    val fileName = resource.fileName ?: "${resource.label}.${getExtension(resource.mimeType)}"
                                    
                                    context.contentResolver.openInputStream(resourceUri)?.use { inputStream ->
                                        zipOut.putNextEntry(ZipEntry("$folder$fileName"))
                                        inputStream.copyTo(zipOut)
                                        zipOut.closeEntry()
                                    }
                                } catch (e: Exception) {
                                    // Arquivo não acessível, pular
                                }
                            }
                        }
                    }
                }
            }
            _exportState.value = ExportState.Success
        } catch (e: Exception) {
            _exportState.value = ExportState.Error(e.message ?: "Erro ao exportar")
        }
    }
    
    fun exportEntityAsFolder(
        context: Context,
        entity: GameEntity,
        resources: List<EntityResource>,
        outputDirUri: Uri
    ) = viewModelScope.launch {
        _exportState.value = ExportState.Loading
        try {
            withContext(Dispatchers.IO) {
                val resolver = context.contentResolver
                val docUri = DocumentFile.fromTreeUri(context, outputDirUri)
                
                // Criar pasta principal
                val entityFolder = docUri?.createDirectory(entity.name.replace(Regex("[^a-zA-Z0-9]"), "_"))
                
                entityFolder?.let { folder ->
                    // Criar metadata.json
                    val metaFile = folder.createFile("application/json", "metadata.json")
                    metaFile?.uri?.let { uri ->
                        resolver.openOutputStream(uri)?.use { out ->
                            out.write(buildMetadataJson(entity, resources).toByteArray())
                        }
                    }
                    
                    // Criar subpastas
                    val spritesFolder = folder.createDirectory("sprites")
                    val animationsFolder = folder.createDirectory("animations")
                    val audioFolder = folder.createDirectory("audio")
                    
                    resources.forEach { resource ->
                        if (resource.type != "TEXT" && resource.type != "LINK") {
                            try {
                                val resourceUri = Uri.parse(resource.value)
                                val targetFolder = when (resource.type) {
                                    "SPRITE", "IMAGE" -> spritesFolder
                                    "ANIMATION" -> animationsFolder
                                    "AUDIO" -> audioFolder
                                    else -> folder
                                }
                                
                                val fileName = resource.fileName ?: "${resource.label}.${getExtension(resource.mimeType)}"
                                val mimeType = resource.mimeType ?: "application/octet-stream"
                                
                                val newFile = targetFolder?.createFile(mimeType, fileName)
                                newFile?.uri?.let { destUri ->
                                    resolver.openInputStream(resourceUri)?.use { input ->
                                        resolver.openOutputStream(destUri)?.use { output ->
                                            input.copyTo(output)
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                // Arquivo não acessível, pular
                            }
                        }
                    }
                }
            }
            _exportState.value = ExportState.Success
        } catch (e: Exception) {
            _exportState.value = ExportState.Error(e.message ?: "Erro ao exportar")
        }
    }
    
    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }
    
    private fun buildMetadataJson(entity: GameEntity, resources: List<EntityResource>): String {
        return """
        {
            "name": "${entity.name}",
            "description": "${entity.description}",
            "lore": ${entity.lore?.let { "\"$it\"" } ?: "null"},
            "thumbnailUri": ${entity.thumbnailUri?.let { "\"$it\"" } ?: "null"},
            "resources": [
                ${resources.joinToString(",\n                ") { res ->
                    """{
                    "label": "${res.label}",
                    "type": "${res.type}",
                    "value": "${res.value}",
                    "mimeType": ${res.mimeType?.let { "\"$it\"" } ?: "null"},
                    "fileName": ${res.fileName?.let { "\"$it\"" } ?: "null"}
                }"""
                }}
            ]
        }
        """.trimIndent()
    }
    
    private fun getExtension(mimeType: String?): String {
        return when (mimeType) {
            "image/png" -> "png"
            "image/jpeg" -> "jpg"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            "image/svg+xml" -> "svg"
            "audio/mpeg" -> "mp3"
            "audio/ogg" -> "ogg"
            "audio/wav", "audio/x-wav" -> "wav"
            else -> "bin"
        }
    }
}

sealed class ExportState {
    object Idle : ExportState()
    object Loading : ExportState()
    object Success : ExportState()
    data class Error(val message: String) : ExportState()
}