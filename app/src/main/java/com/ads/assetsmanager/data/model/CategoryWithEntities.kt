package com.ads.assetsmanager.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class CategoryWithEntities(
    @Embedded val category: Category,
    @Relation(
        parentColumn = "id",
        entityColumn = "categoryId"
    )
    val entities: List<GameEntity>
)