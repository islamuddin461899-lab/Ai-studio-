package com.example.services

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.Locale

class AudioVoiceService(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speechProgress = MutableStateFlow(0f)
    val speechProgress: StateFlow<Float> = _speechProgress.asStateFlow()

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("AudioVoiceService", "TTS init error: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            setupLanguage()
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    _speechProgress.value = 0f
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    _speechProgress.value = 1f
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
        } else {
            Log.e("AudioVoiceService", "TTS initialization failed: $status")
        }
    }

    private var isFemaleVoiceMode = false

    private fun setupLanguage() {
        val tts = this.tts ?: return
        try {
            val bengaliLocale = Locale.forLanguageTag("bn-BD")
            val checkResult = tts.isLanguageAvailable(bengaliLocale)
            if (checkResult >= TextToSpeech.LANG_AVAILABLE) {
                tts.language = bengaliLocale
            } else {
                val genericBengali = Locale.forLanguageTag("bn")
                if (tts.isLanguageAvailable(genericBengali) >= TextToSpeech.LANG_AVAILABLE) {
                    tts.language = genericBengali
                } else {
                    tts.language = Locale.US
                }
            }
            applyVoiceGender(isFemaleVoiceMode)
        } catch (e: Exception) {
            Log.w("AudioVoiceService", "Language setup error: ${e.message}")
        }
    }

    fun setVoiceGender(isFemale: Boolean) {
        isFemaleVoiceMode = isFemale
        applyVoiceGender(isFemale)
    }

    private fun applyVoiceGender(isFemale: Boolean) {
        val tts = this.tts ?: return
        try {
            if (isFemale) {
                // Tuned for warm, melodic, feminine voice in Bengali / Multi-lingual TTS
                tts.setPitch(1.26f)
                tts.setSpeechRate(0.92f)
                // Attempt to pick female voice if available in system TTS voices
                val voices = tts.voices
                if (voices != null) {
                    val femaleVoice = voices.firstOrNull { v ->
                        v.locale.language == "bn" && (v.name.lowercase().contains("female") || v.name.lowercase().contains("women"))
                    } ?: voices.firstOrNull { v ->
                        v.name.lowercase().contains("female")
                    }
                    if (femaleVoice != null) {
                        tts.voice = femaleVoice
                    }
                }
            } else {
                tts.setPitch(0.96f)
                tts.setSpeechRate(0.95f)
            }
        } catch (e: Exception) {
            Log.w("AudioVoiceService", "Voice gender setup: ${e.message}")
        }
    }

    fun speak(
        text: String,
        isFemale: Boolean = isFemaleVoiceMode,
        onStart: () -> Unit = {},
        onDone: () -> Unit = {}
    ) {
        if (tts == null) return

        val cleanText = text.replace("*", "").replace("#", "").trim()
        if (cleanText.isBlank()) return

        stop()
        applyVoiceGender(isFemale)

        _isSpeaking.value = true
        onStart()

        val params = android.os.Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ai_voice_${System.currentTimeMillis()}")

        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, "ai_voice_id")
    }

    fun stop() {
        try {
            tts?.stop()
            _isSpeaking.value = false
            _speechProgress.value = 0f
        } catch (e: Exception) {
            Log.e("AudioVoiceService", "Error stopping TTS: ${e.message}")
        }
    }

    fun destroy() {
        try {
            stop()
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            Log.e("AudioVoiceService", "TTS shutdown error: ${e.message}")
        }
    }

    companion object {
        /**
         * Generates an expressive, cinematic voice narration script tailored to the theme
         */
        fun generateNarrationScript(promptIdea: String, style: String, isBengali: Boolean = true): String {
            val lower = promptIdea.lowercase()
            val isMaaTea = lower.contains("মা") || lower.contains("দুধ-চা") || lower.contains("দুধ চা") || lower.contains("চা") || lower.contains("সিরামিক") || lower.contains("cup") || lower.contains("tea") || lower.contains("mother")
            val isNomad = lower.contains("যাযাবর") || lower.contains("nomad") || lower.contains("wanderer") || lower.contains("মরুভূমি")
            val isRural = lower.contains("গ্রাম") || lower.contains("নদী") || lower.contains("ধানক্ষেত") || lower.contains("নৌকা") || lower.contains("প্রকৃতি")
            val isCyber = lower.contains("সাইবার") || lower.contains("cyber") || lower.contains("ফিউচার") || lower.contains("রোবট")
            val isIslamic = lower.contains("ইসলাম") || lower.contains("মসজিদ") || lower.contains("মিনার") || lower.contains("চাঁদ")

            // Check if user explicitly wrote quotes for the speaker to say
            val quoteRegex = Regex("""[“"']([^“”"']{10,})[“”"']""")
            val match = quoteRegex.find(promptIdea)
            val extractedSpeech = match?.groupValues?.get(1)?.trim()

            return if (isBengali) {
                when {
                    isMaaTea -> "একটি স্নিগ্ধ সকালের মিষ্টি আলোয় সাজানো এক কাপ ধোঁয়া ওঠা গরম দুধ-চা। চায়ের ঘন ফেনায় মমতাময়ী ক্যালিগ্রাফিতে ফুটে উঠেছে একটিই পবিত্র নাম—'মা'। যার স্নেহের স্পর্শে পৃথিবীর প্রতিটি সকাল হয়ে ওঠে মধুর, ভালোবাসায় পূর্ণ আর প্রশান্তিময়।"
                    extractedSpeech != null && extractedSpeech.isNotBlank() -> extractedSpeech
                    isNomad -> "অনন্ত বালুকাময় মরুভূমির মাঝে এক যাযাবর পথিক। যার কোনো নির্দিষ্ট ঠিকানা নেই, সমগ্র বিশ্বই যার ঘর। সূর্যাস্তের রক্তিম আভায় প্রতিটি পদক্ষেপ নতুন এক অনুপ্রেরণা আর স্বাধীনতার গান গায়।"
                    isRural -> "সবুজ শ্যামল বাংলার এক মনজুড়ানো দৃশ্য। শান্ত নদীর জলে ভেসে চলা পালতোলা নৌকা আর সোনালী সন্ধ্যার মৃদুমন্দ বাতাস হৃদয়ে প্রশান্তির পরশ বুলিয়ে দেয়।"
                    isCyber -> "ভবিষ্যতের এক কল্পনাতীত মহানগরী। নিয়ন আলোর উজ্জ্বল ঝলকানিতে প্রযুক্তি আর মানুষের স্বপ্নের অপূর্ব মেলবন্ধন রচিত হয়েছে এখানে।"
                    isIslamic -> "নিস্তব্ধ রাতের স্নিগ্ধ আলোয় উদ্ভাসিত মনোরম স্থাপত্য। আধ্যাত্মিক প্রশান্তি আর সৌন্দর্যের এক পবিত্র সংমিশ্রণ চারপাশের পরিবেশকে মহিমান্বিত করে তুলেছে।"
                    else -> "দৃষ্টির সীমানা ছাড়িয়ে জেগে ওঠা এক অসাধারণ দৃশ্য: $promptIdea। যেখানে প্রতিটি রঙের তুলিতে আঁকা হয়েছে অনন্য সৌন্দর্য ও অনুভূতির এক অপূর্ব প্রকাশ।"
                }
            } else {
                when {
                    isMaaTea -> "A tranquil morning illuminated by soft golden rays, holding a warm cup of rich milk tea. In the delicate velvety foam rests the sacred word 'Maa'—a timeless tribute to unconditional maternal love."
                    extractedSpeech != null && extractedSpeech.isNotBlank() -> extractedSpeech
                    isNomad -> "Across the vast desert dunes walks the wanderer. Bound by no borders, the horizon is their home. Every sunset tells a story of relentless freedom and enduring soul."
                    isRural -> "A serene countryside beneath the golden twilight sky. The gentle river flows with tranquil rhythm, echoing the timeless harmony of nature."
                    isCyber -> "Step into the hyper-futuristic metropolis, illuminated by neon glows and high-tech wonder. Where dreams and innovation converge."
                    else -> "An extraordinary cinematic vision brought to life: $promptIdea. Every detail resonates with atmospheric depth and artistic wonder."
                }
            }
        }
    }
}
