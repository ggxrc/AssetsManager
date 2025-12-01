package com.ads.assetsmanager.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tipos de recursos suportados:
 * - SPRITE: Imagens estáticas (PNG, JPG, SVG, WebP)
 * - ANIMATION: Animações (GIF, Spritesheet PNG)
 * - AUDIO: Sons e músicas (MP3, OGG, WAV)
 * - TEXT: Textos e lore (conteúdo inline)
 * - LINK: URLs externas (referências)
 */
object ResourceType {
    const val SPRITE = "SPRITE"
    const val ANIMATION = "ANIMATION"
    const val AUDIO = "AUDIO"
    const val TEXT = "TEXT"
    const val LINK = "LINK"
    
    // MIME types aceitos por tipo
    val SPRITE_MIME_TYPES = arrayOf("image/png", "image/jpeg", "image/svg+xml", "image/webp")
    val ANIMATION_MIME_TYPES = arrayOf("image/gif", "image/png", "image/webp") // PNG para spritesheets
    val AUDIO_MIME_TYPES = arrayOf("audio/mpeg", "audio/ogg", "audio/wav", "audio/x-wav")
}

@Entity(
    tableName = "entity_resources",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["entityId"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["ownerId"])]
)
data class EntityResource(
    @PrimaryKey(autoGenerate = true) val resourceId: Int = 0,
    val ownerId: Int,
    val type: String,           // SPRITE, ANIMATION, AUDIO, TEXT, LINK
    val value: String,          // URI do arquivo ou conteúdo de texto/link
    val label: String,          // Nome legível do recurso
    val mimeType: String? = null,       // Ex: "image/png", "audio/mp3"
    val fileName: String? = null,       // Nome original do arquivo
    val fileSize: Long? = null          // Tamanho em bytes
)
