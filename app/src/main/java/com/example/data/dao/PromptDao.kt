package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PromptItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptDao {
    @Query("SELECT * FROM prompts ORDER BY createdAt DESC")
    fun getAllPrompts(): Flow<List<PromptItem>>

    @Query("SELECT * FROM prompts WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoritePrompts(): Flow<List<PromptItem>>

    @Query("SELECT * FROM prompts WHERE userIdea LIKE '%' || :query || '%' OR generatedPrompt LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchPrompts(query: String): Flow<List<PromptItem>>

    @Query("SELECT * FROM prompts WHERE id = :id LIMIT 1")
    suspend fun getPromptById(id: Long): PromptItem?

    @Query("SELECT COUNT(*) FROM prompts")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: PromptItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prompts: List<PromptItem>)

    @Update
    suspend fun updatePrompt(prompt: PromptItem)

    @Delete
    suspend fun deletePrompt(prompt: PromptItem)

    @Query("DELETE FROM prompts WHERE id = :id")
    suspend fun deletePromptById(id: Long)

    @Query("DELETE FROM prompts")
    suspend fun clearAll()
}
