package com.ads.assetsmanager.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class EntityWithResources(
    @Embedded val gameEntity: GameEntity,
    @Relation(
        parentColumn = "entityId",
        entityColumn = "ownerId"
    )
    val resources: List<EntityResource>
)