package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.PromptDao
import com.example.data.model.PromptItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [PromptItem::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun promptDao(): PromptDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "prompt_studio_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val DEMO_PROMPTS = listOf(
            PromptItem(
                userIdea = "গ্রামের রাস্তার পাশে দাঁড়িয়ে থাকা একটি যুবক",
                generatedPrompt = "A cinematic portrait of a young South Asian man standing beside a rural road during golden hour, wearing traditional casual cotton attire, gentle authentic smile, atmospheric countryside backdrop with swaying tall grass and palm trees in distance, warm sunlight rim lighting, shallow depth of field, 85mm portrait lens, f/1.4 aperture, highly detailed photorealistic 8k octane render.",
                negativePrompt = "blurry, oversaturated, deformed hands, distorted face, extra limbs, low resolution, artifacts, cartoon, anime, text, watermark",
                language = "বাংলা + English",
                style = "Cinematic",
                aspectRatio = "16:9",
                camera = "Portrait Photography",
                lighting = "Golden Hour",
                quality = "Ultra Detailed",
                extraDetails = "নরম সোনালী আলো, সিনেমাটিক ডেপথ অফ ফিল্ড",
                category = "Portrait",
                isFavorite = true
            ),
            PromptItem(
                userIdea = "সবুজ ধানক্ষেতের আঁকাবাঁকা পথ ও সূর্যাস্ত",
                generatedPrompt = "একটি অত্যন্ত সুন্দর সিনেমাটিক গ্রামের দৃশ্য, দিগন্তজোড়া সবুজ ধানক্ষেতের মধ্য দিয়ে বয়ে যাওয়া আঁকাবাঁকা মাটির পথ, দূরবর্তী কুঁড়েঘর ও বাঁশবাগান, রক্তিম সূর্যাস্তের নরম সোনালী ও বেগুনি আভা, শান্ত ও স্নিগ্ধ গ্রামীণ পরিবেশ, হাইপার-ডিটেইল্ড 8k আল্ট্রা রিয়ালিস্টিক মাস্টারপিস।",
                negativePrompt = "ঝাপসা, খারাপ কোয়ালিটি, কৃত্রিম আলো, অতিরিক্ত উজ্জ্বলতা, বিকৃত দৃশ্য, নয়েজ, ওয়াটারমার্ক",
                language = "বাংলা",
                style = "Photorealistic",
                aspectRatio = "16:9",
                camera = "Wide Angle",
                lighting = "Golden Hour",
                quality = "8K",
                extraDetails = "দিগন্তজোড়া ধানক্ষেত, শান্ত আবহাওয়া",
                category = "Nature",
                isFavorite = false
            ),
            PromptItem(
                userIdea = "রঙিন পোশাক পরা একটি কিউট কার্টুন ক্যারেক্টার",
                generatedPrompt = "A cute 3D cartoon character standing in a colorful whimsical environment, adorable expressive oversized eyes, joyful playful pose, wearing a stylish vibrant pastel hoodie and sneakers, soft rounded shapes, glowing ambient studio rim lighting, dreamy candy-colored fantasy backdrop, Pixar Disney aesthetic, octane 3D render, ray tracing, 4k crisp detail.",
                negativePrompt = "scary, dark, realistic skin, deformed limbs, messy render, low poly, noisy background, muted colors",
                language = "English",
                style = "3D",
                aspectRatio = "1:1",
                camera = "Mirrorless",
                lighting = "Soft Light",
                quality = "4K",
                extraDetails = "Pastel aesthetic, whimsical glowing background",
                category = "3D Cartoon",
                isFavorite = true
            )
        )
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    database.promptDao().insertAll(DEMO_PROMPTS)
                }
            }
        }
    }
}
