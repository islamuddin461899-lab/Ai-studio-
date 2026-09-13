package com.example.data.repository

import com.example.data.dao.PromptDao
import com.example.data.database.AppDatabase
import com.example.data.model.PromptItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PromptRepository(private val promptDao: PromptDao) {

    val allPrompts: Flow<List<PromptItem>> = promptDao.getAllPrompts()
    val favoritePrompts: Flow<List<PromptItem>> = promptDao.getFavoritePrompts()

    fun searchPrompts(query: String): Flow<List<PromptItem>> {
        return promptDao.searchPrompts(query)
    }

    suspend fun insertPrompt(prompt: PromptItem): Long = withContext(Dispatchers.IO) {
        promptDao.insertPrompt(prompt)
    }

    suspend fun updatePrompt(prompt: PromptItem) = withContext(Dispatchers.IO) {
        promptDao.updatePrompt(prompt)
    }

    suspend fun toggleFavorite(prompt: PromptItem) = withContext(Dispatchers.IO) {
        promptDao.updatePrompt(prompt.copy(isFavorite = !prompt.isFavorite))
    }

    suspend fun deletePrompt(prompt: PromptItem) = withContext(Dispatchers.IO) {
        promptDao.deletePrompt(prompt)
    }

    suspend fun deletePromptById(id: Long) = withContext(Dispatchers.IO) {
        promptDao.deletePromptById(id)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        promptDao.clearAll()
    }

    suspend fun ensureDemoDataIfEmpty() = withContext(Dispatchers.IO) {
        if (promptDao.getCount() == 0) {
            promptDao.insertAll(AppDatabase.DEMO_PROMPTS)
        }
    }
}
