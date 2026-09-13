package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prompts")
data class PromptItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userIdea: String,
    val generatedPrompt: String,
    val negativePrompt: String = "",
    val language: String = "বাংলা + English",
    val style: String = "Cinematic",
    val aspectRatio: String = "16:9",
    val camera: String = "DSLR",
    val lighting: String = "Golden Hour",
    val quality: String = "Ultra Detailed",
    val extraDetails: String = "",
    val category: String = "Cinematic",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val generatedImageUri: String? = null,
    val sourceImageUri: String? = null,
    val mediaType: String = "IMAGE", // "IMAGE", "PHOTO_EDIT", "VIDEO"
    val voiceScript: String? = null
)
