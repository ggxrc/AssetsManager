package com.ads.assetsmanager.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "game_entities",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE // Apagou Categoria -> Apaga Entidades filhas
        )
    ],
    indices = [Index(value = ["categoryId"])] // Otimização de busca
)
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val entityId : Int = 0,
    val categoryId : Int,
    val name : String,
    val description : String
)
