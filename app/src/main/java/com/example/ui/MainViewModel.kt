package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.AccentThemeOption
import com.example.data.model.AppSettings
import com.example.data.model.DarkModeOption
import com.example.data.model.PromptItem
import com.example.data.repository.PromptRepository
import com.example.services.AiService
import com.example.services.AudioVoiceService
import com.example.services.PromptGenerationOptions
import com.example.services.VisualGenerationEngine
import androidx.core.content.FileProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

enum class AppNavTab {
    HOME,
    CREATE,
    HISTORY,
    FAVORITES,
    SETTINGS
}

enum class StudioMode {
    TEXT_TO_IMAGE,
    PHOTO_TO_IMAGE,
    VIDEO_AND_VOICE
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PromptRepository
    private val prefs = application.getSharedPreferences("prompt_studio_prefs", Context.MODE_PRIVATE)

    // Audio Voice Service for AI narration
    val audioVoiceService = AudioVoiceService(application)

    // Navigation state
    private val _currentTab = MutableStateFlow(AppNavTab.HOME)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    // Studio Mode (Text to Image, Photo to Image, Video & Voice)
    val studioMode = MutableStateFlow(StudioMode.TEXT_TO_IMAGE)

    // Active Result View
    private val _viewingResult = MutableStateFlow<PromptItem?>(null)
    val viewingResult: StateFlow<PromptItem?> = _viewingResult.asStateFlow()

    // Form states for Create screen (Text to Image)
    val userIdea = MutableStateFlow("")
    val promptLanguage = MutableStateFlow("বাংলা + English")
    val imageStyle = MutableStateFlow("Cinematic")
    val aspectRatio = MutableStateFlow("1:1")
    val camera = MutableStateFlow("DSLR")
    val lighting = MutableStateFlow("Golden Hour")
    val quality = MutableStateFlow("Ultra Detailed")
    val extraDetails = MutableStateFlow("")
    val category = MutableStateFlow("Cinematic")

    // Form states for Photo to Image (Gallery selection)
    val selectedPhotoUri = MutableStateFlow<String?>(null)
    val photoEditPrompt = MutableStateFlow("")

    // Form states for Video & Voice
    val videoInputMode = MutableStateFlow("PROMPT") // "PHOTO" or "PROMPT"
    val videoPrompt = MutableStateFlow("")
    val videoVoiceStyle = MutableStateFlow("সিনেমাটিক বর্ণনা")
    val videoVoiceGender = MutableStateFlow("FEMALE") // "FEMALE" or "MALE"
    val videoAspectRatio = MutableStateFlow("9:16") // "9:16" or "16:9"
    val includeVoiceInVideo = MutableStateFlow(true)

    // Generation state
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationError = MutableStateFlow<String?>(null)
    val generationError: StateFlow<String?> = _generationError.asStateFlow()

    // Toast Events
    private val _toastEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // History search and filter
    val historySearchQuery = MutableStateFlow("")
    val historyCategoryFilter = MutableStateFlow("All")

    // App Settings
    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = PromptRepository(db.promptDao())
        viewModelScope.launch {
            repository.ensureDemoDataIfEmpty()
        }
    }

    val allPrompts: StateFlow<List<PromptItem>> = repository.allPrompts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoritePrompts: StateFlow<List<PromptItem>> = repository.favoritePrompts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredHistoryPrompts: StateFlow<List<PromptItem>> = combine(
        allPrompts,
        historySearchQuery,
        historyCategoryFilter
    ) { prompts, query, catFilter ->
        prompts.filter { item ->
            val matchesCategory = if (catFilter == "All") true else item.category.equals(catFilter, ignoreCase = true)
            val matchesQuery = if (query.isBlank()) true else {
                item.userIdea.contains(query, ignoreCase = true) ||
                        item.generatedPrompt.contains(query, ignoreCase = true) ||
                        item.category.contains(query, ignoreCase = true) ||
                        item.style.contains(query, ignoreCase = true)
            }
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun openResult(prompt: PromptItem) {
        _viewingResult.value = prompt
    }

    fun closeResult() {
        _viewingResult.value = null
    }

    fun onSelectQuickCategory(catName: String) {
        category.value = catName
        imageStyle.value = when (catName) {
            "Portrait" -> "Studio Photography"
            "Cinematic" -> "Cinematic"
            "3D Cartoon" -> "3D"
            "Nature" -> "Photorealistic"
            "City" -> "Photorealistic"
            "Islamic" -> "Cinematic"
            "Fashion" -> "Studio Photography"
            "Animals" -> "Photorealistic"
            "Cars" -> "3D Render"
            "Interior" -> "Photorealistic"
            "Art" -> "Digital Art"
            "Social Media" -> "Digital Art"
            else -> "Cinematic"
        }
        _currentTab.value = AppNavTab.CREATE
    }

    fun onSelectPopularStyle(styleName: String) {
        imageStyle.value = styleName
        _currentTab.value = AppNavTab.CREATE
    }

    fun startCreateWithIdea(idea: String) {
        userIdea.value = idea
        _currentTab.value = AppNavTab.CREATE
    }

    fun setMaaTeaVideoPreset() {
        studioMode.value = StudioMode.VIDEO_AND_VOICE
        videoInputMode.value = "PROMPT"
        videoVoiceGender.value = "FEMALE"
        videoAspectRatio.value = "9:16"
        includeVoiceInVideo.value = true
        videoPrompt.value = "এই মেয়ে টি বাংলা য় বলবে একটি অত্যন্ত বাস্তবসম্মত ও সুন্দর সিনেমাটিক ছবি। একটি সাদা সিরামিক ফুলের ডিজাইনের কাপের মধ্যে গরম দুধ-চা, চায়ের উপরে ঘন ফেনা ও অসংখ্য ছোট বুদবুদ দেখা যাচ্ছে। চায়ের ফেনার মাঝখানে সুন্দরভাবে বাংলা ক্যালিগ্রাফি স্টাইলে “মা” লেখা থাকবে। কাপটি একটি পরিষ্কার টেবিলের ওপর রাখা, পেছনে নরম ব্লার করা ফুল ও ঘরের পরিবেশ। উষ্ণ সকালের আলো, নরম ছায়া, shallow depth of field, realistic reflections, highly detailed, photorealistic, cinematic lighting, 4K quality, vertical 9:16 composition। ছবিতে কোনো সোশ্যাল মিডিয়া UI, লাইক, কমেন্ট, ফলো বাটন বা অতিরিক্ত লেখা থাকবে না।"
        _currentTab.value = AppNavTab.CREATE
    }

    fun generatePrompt(fromHomeScreen: Boolean = false) {
        generateAiImage(fromHomeScreen = fromHomeScreen)
    }

    fun generateAiImage(userIdeaInput: String? = null, fromHomeScreen: Boolean = false) {
        val currentIdea = (userIdeaInput ?: userIdea.value).trim()
        if (currentIdea.isBlank()) {
            _generationError.value = "দয়া করে প্রথমে আপনার ছবির ধারণা বা প্রম্পট লিখুন।"
            showToast("দয়া করে প্রথমে আপনার ছবির ধারণা বা প্রম্পট লিখুন।")
            return
        }

        userIdea.value = currentIdea
        _isGenerating.value = true
        _generationError.value = null

        viewModelScope.launch {
            val options = PromptGenerationOptions(
                userIdea = currentIdea,
                language = promptLanguage.value,
                style = imageStyle.value,
                aspectRatio = aspectRatio.value,
                camera = camera.value,
                lighting = lighting.value,
                quality = quality.value,
                extraDetails = extraDetails.value,
                category = category.value
            )

            // Step 1: Synthesize / generate rich prompt
            val promptResult = AiService.generatePrompt(options)
            val generated = promptResult.getOrNull() ?: AiService.synthesizePrompt(options)

            // Step 2: Generate actual visual artwork/image based on prompt!
            val context = getApplication<Application>()
            val imageResult = VisualGenerationEngine.generateImage(
                context = context,
                promptText = generated.prompt,
                style = imageStyle.value,
                aspectRatio = aspectRatio.value
            )

            val generatedImageUri = imageResult.getOrNull()

            val newPromptItem = PromptItem(
                userIdea = currentIdea,
                generatedPrompt = generated.prompt,
                negativePrompt = generated.negativePrompt,
                language = promptLanguage.value,
                style = imageStyle.value,
                aspectRatio = aspectRatio.value,
                camera = camera.value,
                lighting = lighting.value,
                quality = quality.value,
                extraDetails = extraDetails.value,
                category = category.value,
                isFavorite = false,
                createdAt = System.currentTimeMillis(),
                generatedImageUri = generatedImageUri,
                mediaType = "IMAGE"
            )

            val insertedId = repository.insertPrompt(newPromptItem)
            val savedItem = newPromptItem.copy(id = insertedId)

            _viewingResult.value = savedItem
            _isGenerating.value = false

            if (_settings.value.autoCopy) {
                copyToClipboard(savedItem.generatedPrompt, "Prompt copied!")
            }
            showToast("এআই ইমেজ সফলভাবে তৈরি হয়েছে! ✨")
        }
    }

    fun transformPhotoFromGallery() {
        val photoUri = selectedPhotoUri.value
        if (photoUri.isNullOrBlank()) {
            _generationError.value = "দয়া করে গ্যালারি থেকে একটি ফটো সিলেক্ট করুন।"
            showToast("দয়া করে গ্যালারি থেকে একটি ফটো সিলেক্ট করুন।")
            return
        }

        val prompt = photoEditPrompt.value.trim().ifBlank { "যাযাবর সিনেমাটিক রূপান্তর" }

        _isGenerating.value = true
        _generationError.value = null

        viewModelScope.launch {
            val context = getApplication<Application>()
            val sourceBitmap = VisualGenerationEngine.loadBitmapFromUri(context, photoUri)
            if (sourceBitmap == null) {
                _isGenerating.value = false
                _generationError.value = "ফটো লোড করা সম্ভব হয়নি।"
                showToast("ফটো লোড করা সম্ভব হয়নি।")
                return@launch
            }

            val result = VisualGenerationEngine.generateImage(
                context = context,
                promptText = prompt,
                style = imageStyle.value,
                aspectRatio = aspectRatio.value,
                sourceBitmap = sourceBitmap
            )

            val options = PromptGenerationOptions(
                userIdea = prompt,
                language = promptLanguage.value,
                style = imageStyle.value,
                aspectRatio = aspectRatio.value,
                camera = camera.value,
                lighting = lighting.value,
                quality = quality.value
            )
            val promptResult = AiService.synthesizePrompt(options)

            val transformedImagePath = result.getOrNull()

            val newPromptItem = PromptItem(
                userIdea = prompt,
                generatedPrompt = promptResult.prompt,
                negativePrompt = promptResult.negativePrompt,
                language = promptLanguage.value,
                style = imageStyle.value,
                aspectRatio = aspectRatio.value,
                camera = camera.value,
                lighting = lighting.value,
                quality = quality.value,
                extraDetails = "ফটো গ্যালারি রূপান্তর",
                category = "Photo Edit",
                isFavorite = false,
                createdAt = System.currentTimeMillis(),
                generatedImageUri = transformedImagePath,
                sourceImageUri = photoUri,
                mediaType = "PHOTO_EDIT"
            )

            val insertedId = repository.insertPrompt(newPromptItem)
            val savedItem = newPromptItem.copy(id = insertedId)

            _viewingResult.value = savedItem
            _isGenerating.value = false
            showToast("ফটো সফলভাবে রূপান্তর করা হয়েছে! 🎨")
        }
    }

    fun generateVideoAndVoice() {
        val inputMode = videoInputMode.value
        val sourcePhoto = selectedPhotoUri.value
        val promptText = videoPrompt.value.trim()

        if (inputMode == "PHOTO" && sourcePhoto.isNullOrBlank()) {
            _generationError.value = "দয়া করে ভিডিও তৈরির জন্য একটি ফটো সিলেক্ট করুন।"
            showToast("দয়া করে ভিডিও তৈরির জন্য একটি ফটো সিলেক্ট করুন।")
            return
        }

        if (inputMode == "PROMPT" && promptText.isBlank()) {
            _generationError.value = "দয়া করে ভিডিওর প্রম্পট বা গল্প লিখুন।"
            showToast("দয়া করে ভিডিওর প্রম্পট বা গল্প লিখুন।")
            return
        }

        val effectiveIdea = if (inputMode == "PHOTO") {
            if (promptText.isNotBlank()) promptText else "ফটো থেকে সিনেমাটিক ভিডিও দৃশ্য"
        } else {
            promptText
        }

        val resolvedAspectRatio = if (effectiveIdea.contains("9:16") || effectiveIdea.lowercase().contains("vertical")) {
            "9:16"
        } else if (effectiveIdea.contains("16:9") || effectiveIdea.lowercase().contains("horizontal") || effectiveIdea.lowercase().contains("landscape")) {
            "16:9"
        } else {
            videoAspectRatio.value
        }

        val lowerIdea = effectiveIdea.lowercase()
        val isFemaleVoice = videoVoiceGender.value == "FEMALE" ||
                lowerIdea.contains("মেয়ে") ||
                lowerIdea.contains("মেয়ে") ||
                lowerIdea.contains("female") ||
                lowerIdea.contains("girl")

        _isGenerating.value = true
        _generationError.value = null

        viewModelScope.launch {
            val context = getApplication<Application>()
            var visualPath: String? = null

            if (inputMode == "PHOTO" && !sourcePhoto.isNullOrBlank()) {
                val sourceBitmap = VisualGenerationEngine.loadBitmapFromUri(context, sourcePhoto)
                if (sourceBitmap != null) {
                    val result = VisualGenerationEngine.generateImage(
                        context = context,
                        promptText = effectiveIdea,
                        style = "Cinematic",
                        aspectRatio = resolvedAspectRatio,
                        sourceBitmap = sourceBitmap
                    )
                    visualPath = result.getOrNull()
                }
            } else {
                val result = VisualGenerationEngine.generateImage(
                    context = context,
                    promptText = effectiveIdea,
                    style = "Cinematic",
                    aspectRatio = resolvedAspectRatio
                )
                visualPath = result.getOrNull()
            }

            // Generate Voice narration script
            val isBengali = promptLanguage.value.contains("বাংলা") || effectiveIdea.any { it in '\u0980'..'\u09FF' }
            val voiceScript = AudioVoiceService.generateNarrationScript(effectiveIdea, imageStyle.value, isBengali)

            val newPromptItem = PromptItem(
                userIdea = effectiveIdea,
                generatedPrompt = "Cinematic video generation prompt: $effectiveIdea",
                negativePrompt = "flickering, jitter, low fps, blurry video, warped anatomy, extra text, social media ui",
                language = promptLanguage.value,
                style = "Cinematic",
                aspectRatio = resolvedAspectRatio,
                camera = if (resolvedAspectRatio == "9:16") "Vertical Cinematic Camera" else "Drone Shot",
                lighting = "Golden Morning Sunlight",
                quality = "4K Photorealistic",
                extraDetails = if (isFemaleVoice) "মেয়ের কণ্ঠে বাংলা ভয়েস ও ভিডিও" else "এআই ভিডিও ও ভয়েস ওভার",
                category = "Video",
                isFavorite = false,
                createdAt = System.currentTimeMillis(),
                generatedImageUri = visualPath,
                sourceImageUri = sourcePhoto,
                mediaType = "VIDEO",
                voiceScript = voiceScript
            )

            val insertedId = repository.insertPrompt(newPromptItem)
            val savedItem = newPromptItem.copy(id = insertedId)

            _viewingResult.value = savedItem
            _isGenerating.value = false

            // Trigger voice playback with gender modulation
            if (includeVoiceInVideo.value) {
                audioVoiceService.speak(voiceScript, isFemale = isFemaleVoice)
            }
            showToast("এআই ভিডিও ও ভয়েস সফলভাবে তৈরি হয়েছে! 🎬")
        }
    }

    fun saveImageToGallery(prompt: PromptItem) {
        val imageUri = prompt.generatedImageUri
        if (imageUri.isNullOrBlank()) {
            showToast("সংরক্ষণ করার মতো কোনো ইমেজ পাওয়া যায়নি।")
            return
        }

        viewModelScope.launch {
            val context = getApplication<Application>()
            val bitmap = VisualGenerationEngine.loadBitmapFromUri(context, imageUri)
            if (bitmap != null) {
                val success = VisualGenerationEngine.saveBitmapToGallery(context, bitmap, prompt.userIdea)
                if (success) {
                    showToast("ছবিটি ফোনের গ্যালারিতে সেভ হয়েছে! 🖼️")
                } else {
                    showToast("গ্যালারিতে সেভ করা সম্ভব হয়নি।")
                }
            } else {
                showToast("ইমেজ লোড করা যায়নি।")
            }
        }
    }

    fun regenerateCurrentPrompt() {
        val current = _viewingResult.value ?: return
        userIdea.value = current.userIdea
        promptLanguage.value = current.language
        imageStyle.value = current.style
        aspectRatio.value = current.aspectRatio
        camera.value = current.camera
        lighting.value = current.lighting
        quality.value = current.quality
        extraDetails.value = current.extraDetails
        category.value = current.category
        generatePrompt()
    }

    fun toggleFavorite(prompt: PromptItem) {
        viewModelScope.launch {
            repository.toggleFavorite(prompt)
            if (_viewingResult.value?.id == prompt.id) {
                _viewingResult.value = prompt.copy(isFavorite = !prompt.isFavorite)
            }
            val status = if (!prompt.isFavorite) "পছন্দের তালিকায় যুক্ত হয়েছে ⭐" else "পছন্দের তালিকা থেকে সরানো হয়েছে"
            showToast(status)
        }
    }

    fun deletePrompt(prompt: PromptItem) {
        viewModelScope.launch {
            repository.deletePrompt(prompt)
            if (_viewingResult.value?.id == prompt.id) {
                _viewingResult.value = null
            }
            showToast("Prompt মুছে ফেলা হয়েছে 🗑️")
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
            _viewingResult.value = null
            showToast("সমস্ত হিস্ট্রি মুছে ফেলা হয়েছে 🧹")
        }
    }

    fun copyToClipboard(text: String, successToastMessage: String = "Prompt copied!") {
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("AI Prompt", text)
        clipboard.setPrimaryClip(clip)
        showToast(successToastMessage)
    }

    fun sharePrompt(prompt: PromptItem) {
        val context = getApplication<Application>()
        val shareText = buildString {
            append("🎨 Prompt Studio বাংলা\n\n")
            append("Idea: ${prompt.userIdea}\n\n")
            if (prompt.mediaType == "VIDEO" && !prompt.voiceScript.isNullOrBlank()) {
                append("🎬 AI Video Narration Voice Script:\n")
                append(prompt.voiceScript)
                append("\n\n")
            }
            append("PROMPT:\n${prompt.generatedPrompt}\n\n")
            if (prompt.negativePrompt.isNotBlank()) {
                append("NEGATIVE PROMPT:\n${prompt.negativePrompt}\n\n")
            }
            append("Parameters: ${prompt.style} | ${prompt.lighting} | ${prompt.camera} | ${prompt.aspectRatio} | ${prompt.quality}")
        }

        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                val imgUriStr = prompt.generatedImageUri
                if (!imgUriStr.isNullOrBlank()) {
                    val imgFile = File(imgUriStr)
                    if (imgFile.exists()) {
                        val contentUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            imgFile
                        )
                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        type = "image/png"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    } else {
                        type = "text/plain"
                    }
                } else {
                    type = "text/plain"
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share AI Studio Creation").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            copyToClipboard(shareText, "Share not supported, copied to clipboard!")
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioVoiceService.destroy()
    }

    fun downloadTxt(prompt: PromptItem) {
        val context = getApplication<Application>()
        val fileContent = buildString {
            append("==================================================\n")
            append("PROMPT STUDIO বাংলা - AI IMAGE PROMPT\n")
            append("==================================================\n\n")
            append("IDEA: ${prompt.userIdea}\n")
            append("CATEGORY: ${prompt.category}\n")
            append("LANGUAGE: ${prompt.language}\n")
            append("CREATED AT: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(prompt.createdAt))}\n\n")
            append("--------------------------------------------------\n")
            append("PROMPT:\n")
            append(prompt.generatedPrompt)
            append("\n\n")
            if (prompt.negativePrompt.isNotBlank()) {
                append("--------------------------------------------------\n")
                append("NEGATIVE PROMPT:\n")
                append(prompt.negativePrompt)
                append("\n\n")
            }
            append("--------------------------------------------------\n")
            append("PARAMETERS:\n")
            append("Style: ${prompt.style}\n")
            append("Lighting: ${prompt.lighting}\n")
            append("Camera: ${prompt.camera}\n")
            append("Aspect Ratio: ${prompt.aspectRatio}\n")
            append("Quality: ${prompt.quality}\n")
            if (prompt.extraDetails.isNotBlank()) {
                append("Extra Details: ${prompt.extraDetails}\n")
            }
            append("==================================================\n")
        }

        try {
            val fileName = "ai-image-prompt-${System.currentTimeMillis()}.txt"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { it.write(fileContent.toByteArray()) }

            // Trigger share/save intent for txt
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, fileContent)
                putExtra(Intent.EXTRA_TITLE, "ai-image-prompt.txt")
                type = "text/plain"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(sendIntent, "Download / Save TXT").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
            showToast("ai-image-prompt.txt প্রস্তুত হয়েছে 📥")
        } catch (e: Exception) {
            copyToClipboard(fileContent, "Downloaded content copied to clipboard!")
        }
    }

    fun showToast(msg: String) {
        viewModelScope.launch {
            _toastEvent.emit(msg)
        }
    }

    fun updateDarkMode(option: DarkModeOption) {
        _settings.value = _settings.value.copy(darkMode = option)
        saveSettings()
    }

    fun updateAccentTheme(option: AccentThemeOption) {
        _settings.value = _settings.value.copy(accentTheme = option)
        saveSettings()
    }

    fun updateDefaultLanguage(lang: String) {
        _settings.value = _settings.value.copy(defaultLanguage = lang)
        promptLanguage.value = lang
        saveSettings()
    }

    fun toggleAutoCopy(enabled: Boolean) {
        _settings.value = _settings.value.copy(autoCopy = enabled)
        saveSettings()
    }

    fun toggleNotifications(enabled: Boolean) {
        _settings.value = _settings.value.copy(notificationsEnabled = enabled)
        saveSettings()
    }

    private fun loadSettings(): AppSettings {
        val darkModeStr = prefs.getString("dark_mode", DarkModeOption.SYSTEM.name) ?: DarkModeOption.SYSTEM.name
        val accentStr = prefs.getString("accent_theme", AccentThemeOption.VIOLET.name) ?: AccentThemeOption.VIOLET.name
        val defaultLang = prefs.getString("default_language", "বাংলা + English") ?: "বাংলা + English"
        val autoCopy = prefs.getBoolean("auto_copy", false)
        val notifications = prefs.getBoolean("notifications", true)

        val darkMode = try { DarkModeOption.valueOf(darkModeStr) } catch (e: Exception) { DarkModeOption.SYSTEM }
        val accent = try { AccentThemeOption.valueOf(accentStr) } catch (e: Exception) { AccentThemeOption.VIOLET }

        return AppSettings(
            darkMode = darkMode,
            accentTheme = accent,
            defaultLanguage = defaultLang,
            autoCopy = autoCopy,
            notificationsEnabled = notifications
        )
    }

    private fun saveSettings() {
        val s = _settings.value
        prefs.edit().apply {
            putString("dark_mode", s.darkMode.name)
            putString("accent_theme", s.accentTheme.name)
            putString("default_language", s.defaultLanguage)
            putBoolean("auto_copy", s.autoCopy)
            putBoolean("notifications", s.notificationsEnabled)
            apply()
        }
    }
}
