package com.ads.assetsmanager.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "entity_resources",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["entityId"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE // Apagou Entidade -> Apaga Recursos filhos
        )
    ],
    indices = [Index(value = ["ownerId"])]
)
data class EntityResource(
    @PrimaryKey(autoGenerate = true) val resourceId: Int = 0,
    val ownerId: Int,
    val type: String, // "IMAGE", "AUDIO", "TEXT", "LINK"
    val value: String, // URI ou Texto
    val label: String
)
