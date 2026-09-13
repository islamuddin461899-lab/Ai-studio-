package com.example.services

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GeneratedPromptResult(
    val prompt: String,
    val negativePrompt: String,
    val parameters: Map<String, String>,
    val isFromGemini: Boolean,
    val rawResponse: String = ""
)

data class PromptGenerationOptions(
    val userIdea: String,
    val language: String = "বাংলা + English",
    val style: String = "Cinematic",
    val aspectRatio: String = "16:9",
    val camera: String = "DSLR",
    val lighting: String = "Golden Hour",
    val quality: String = "Ultra Detailed",
    val extraDetails: String = "",
    val category: String = "Cinematic"
)

object AiService {
    private const val TAG = "AiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    fun isGeminiKeyConfigured(): Boolean {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            key.isNotBlank() && key != "MY_GEMINI_API_KEY"
        } catch (e: Throwable) {
            false
        }
    }

    suspend fun generatePrompt(options: PromptGenerationOptions): Result<GeneratedPromptResult> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val geminiResult = callGeminiApi(apiKey, options)
                if (geminiResult != null) {
                    return@withContext Result.success(geminiResult)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini API call failed, falling back to intelligent synthesizer: ${e.message}")
            }
        }

        // Offline / intelligent synthesizer fallback that satisfies all prompt requirements
        val synthesized = synthesizePrompt(options)
        Result.success(synthesized)
    }

    private fun callGeminiApi(apiKey: String, options: PromptGenerationOptions): GeneratedPromptResult? {
        val url = "$BASE_URL?key=$apiKey"

        val systemInstructionText = """
            You are an expert AI image prompt engineer. Transform the user's idea into a detailed image-generation prompt while preserving the original intent.
            Ensure you include:
            - Subject and character description (pose, expression, clothing if applicable)
            - Environment, background, and atmosphere
            - Specific lighting, camera angle, lens, depth of field
            - Visual texture, color palette, materials, artistic style, and rendering quality
            - Do not alter the user's core idea. Do not add unnecessary irrelevant objects.
            
            Language rules:
            - If Language is 'বাংলা', generate the main prompt in fluent, poetic, detailed Bengali.
            - If Language is 'English', generate the main prompt in rich, vivid English suitable for Midjourney/Flux/Stable Diffusion.
            - If Language is 'বাংলা + English', provide the Bengali prompt first, then the English prompt.
            
            CRITICAL OUTPUT FORMAT:
            You MUST follow this exact structure without markdown code blocks:
            
            PROMPT:
            [The generated detailed prompt text here]
            
            NEGATIVE PROMPT:
            [The negative prompt to avoid bad artifacts, extra limbs, blur, low quality, etc.]
            
            PARAMETERS:
            Style: ${options.style}
            Lighting: ${options.lighting}
            Camera: ${options.camera}
            Aspect Ratio: ${options.aspectRatio}
            Quality: ${options.quality}
        """.trimIndent()

        val userPromptText = """
            User Idea: ${options.userIdea}
            Category: ${options.category}
            Selected Style: ${options.style}
            Aspect Ratio: ${options.aspectRatio}
            Camera / Photography: ${options.camera}
            Lighting: ${options.lighting}
            Quality: ${options.quality}
            Extra Details: ${if (options.extraDetails.isNotBlank()) options.extraDetails else "None"}
            Requested Language: ${options.language}
        """.trimIndent()

        val requestJson = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemInstructionText) })
                })
            })
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", userPromptText) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("topP", 0.95)
                put("topK", 40)
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            val response = httpClient.newCall(request).execute()
            response.use { resp ->
                if (!resp.isSuccessful) {
                    val errorBody = resp.body?.string() ?: ""
                    if (resp.code == 429) {
                        Log.w(TAG, "Gemini text prompt quota exceeded (429): $errorBody")
                    } else {
                        Log.w(TAG, "Gemini error: ${resp.code} $errorBody")
                    }
                    return null
                }

                val responseString = resp.body?.string() ?: return null
                val responseObj = JSONObject(responseString)
                val text = responseObj.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text") ?: return null

                return parseStructuredPrompt(text, options, isFromGemini = true)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Gemini API call failed with exception: ${e.message}")
            return null
        }
    }

    private fun parseStructuredPrompt(text: String, options: PromptGenerationOptions, isFromGemini: Boolean): GeneratedPromptResult {
        var promptPart = ""
        var negativePromptPart = ""
        val paramMap = mutableMapOf<String, String>()

        val promptMarker = "PROMPT:"
        val negativeMarker = "NEGATIVE PROMPT:"
        val paramMarker = "PARAMETERS:"

        val cleanText = text.trim()

        if (cleanText.contains(promptMarker) && cleanText.contains(negativeMarker)) {
            val promptStart = cleanText.indexOf(promptMarker) + promptMarker.length
            val negativeStart = cleanText.indexOf(negativeMarker)
            promptPart = cleanText.substring(promptStart, negativeStart).trim()

            if (cleanText.contains(paramMarker)) {
                val paramStart = cleanText.indexOf(paramMarker)
                negativePromptPart = cleanText.substring(negativeStart + negativeMarker.length, paramStart).trim()
                val paramsSection = cleanText.substring(paramStart + paramMarker.length).trim()
                paramsSection.lines().forEach { line ->
                    val colonIndex = line.indexOf(':')
                    if (colonIndex != -1) {
                        val k = line.substring(0, colonIndex).trim()
                        val v = line.substring(colonIndex + 1).trim()
                        if (k.isNotEmpty() && v.isNotEmpty()) {
                            paramMap[k] = v
                        }
                    }
                }
            } else {
                negativePromptPart = cleanText.substring(negativeStart + negativeMarker.length).trim()
            }
        } else {
            promptPart = cleanText
            negativePromptPart = "blurry, low quality, deformed, distorted, extra limbs, bad anatomy, artifacts, pixelated"
        }

        if (paramMap.isEmpty()) {
            paramMap["Style"] = options.style
            paramMap["Lighting"] = options.lighting
            paramMap["Camera"] = options.camera
            paramMap["Aspect Ratio"] = options.aspectRatio
            paramMap["Quality"] = options.quality
        }

        return GeneratedPromptResult(
            prompt = promptPart.ifBlank { cleanText },
            negativePrompt = negativePromptPart.ifBlank { "blurry, low quality, low resolution, deformed, distorted artifacts" },
            parameters = paramMap,
            isFromGemini = isFromGemini,
            rawResponse = text
        )
    }

    fun synthesizePrompt(options: PromptGenerationOptions): GeneratedPromptResult {
        val styleModifier = when (options.style) {
            "Photorealistic" -> "hyperrealistic ultra-photoreal photography, sharp focus, lifelike skin texture and surface reflections"
            "Cinematic" -> "cinematic film still, 35mm film grain, anamorphic lens flare, award-winning movie cinematography"
            "3D", "3D Render" -> "stylized 3D digital render, Pixar Disney octane rendering, smooth subsurface scattering, ray-traced shadows"
            "Anime" -> "masterpiece anime key visual, Makoto Shinkai aesthetic, crisp linework, glowing cel shading, emotional atmosphere"
            "Cartoon" -> "vibrant 2D cartoon illustration, playful dynamic expressions, expressive character design, bold outlines"
            "Digital Art" -> "detailed digital concept art, trending on ArtStation, dynamic brushwork, imaginative color palette"
            "Fantasy" -> "ethereal epic fantasy atmosphere, mystical glowing particles, enchanting worldbuilding, dramatic composition"
            "Watercolor" -> "loose watercolor wash painting, fluid splashes, visible paper texture, soft blended gradients"
            "Oil Painting" -> "classical oil on textured canvas, thick impasto strokes, Rembrandt lighting, rich deep pigments"
            "Studio Photography" -> "professional high-end studio photography, softbox rim lighting, clean backdrop, Hasselblad medium format"
            else -> "high aesthetic quality, immaculate composition"
        }

        val cameraModifier = when (options.camera) {
            "DSLR" -> "shot on Canon EOS R5 DSLR, prime 50mm f/1.2 lens, crystal clear optical sharpness"
            "Mirrorless" -> "Sony A7R V mirrorless camera, 85mm G-Master lens, high dynamic range"
            "Smartphone" -> "modern flagship mobile photography, natural depth portrait mode, authentic handheld realism"
            "Portrait Photography" -> "tight portrait framing, creamy bokeh, 85mm telephoto lens, f/1.4 aperture, expressive gaze"
            "Wide Angle" -> "expansive 16mm ultra-wide lens, immersive perspective, sweeping panoramic depth"
            "Macro" -> "intricate macro close-up, extreme fine details, microscopic texture depth, 100mm macro lens"
            "Drone Shot" -> "aerial drone shot, top-down bird's-eye perspective, sweeping landscape panorama"
            else -> "professional camera framing"
        }

        val lightingModifier = when (options.lighting) {
            "Natural Light" -> "soft natural ambient daylight, balanced gentle shadows, genuine organic feel"
            "Golden Hour" -> "magical golden hour sunlight, warm amber and rose tones, gentle rim glow, long romantic shadows"
            "Soft Light" -> "diffused soft illumination, gentle highlight roll-off, flattering subtle shadow transitions"
            "Studio Light" -> "three-point studio lighting setup, key light, fill light, delicate hair rim glow"
            "Neon Light" -> "cyberpunk neon glow, electric cyan and magenta highlights, vivid moody reflections"
            "Dramatic Light" -> "chiaroscuro dramatic high-contrast lighting, bold shadows, intense cinematic mood"
            "Volumetric Light" -> "ethereal volumetric god rays, misty dust motes dancing in sunbeams, dreamy illumination"
            else -> "atmospheric cinematic lighting"
        }

        val qualityTag = when (options.quality) {
            "Standard" -> "high resolution, clean composition"
            "High Quality" -> "high definition, finely rendered, sharp focus"
            "Ultra Detailed" -> "ultra-detailed, intricately rendered textures, 8k resolution, masterpiece quality"
            "4K" -> "4k crisp resolution, photorealistic clarity, noise-free"
            "8K" -> "8k UHD, extreme detail fidelity, Octane render, photorealistic masterpiece"
            else -> "8k ultra detailed"
        }

        val extra = if (options.extraDetails.isNotBlank()) ", ${options.extraDetails.trim()}" else ""

        val englishPrompt = "${options.userIdea.trim()}, $styleModifier, $lightingModifier, $cameraModifier, $qualityTag$extra --ar ${options.aspectRatio.replace(":", "/")}"

        val bengaliPrompt = buildString {
            append("একটি মনোমুগ্ধকর ${options.style} দৃশ্য: ")
            append(options.userIdea.trim())
            if (options.extraDetails.isNotBlank()) {
                append("। সাথে ${options.extraDetails.trim()}")
            }
            append("। আলো: ${options.lighting}, ক্যামেরা ভিউ: ${options.camera}, মান: ${options.quality}, সিনেমাটিক আবহ ও নিখুঁত ডিটেইল।")
        }

        val finalPrompt = when (options.language) {
            "বাংলা" -> bengaliPrompt
            "English" -> englishPrompt
            else -> "$bengaliPrompt\n\n---\n[English Image Prompt for AI Generator]:\n$englishPrompt"
        }

        val negativePrompt = when (options.style) {
            "Anime", "Cartoon" -> "photorealistic skin, 3D render, noise, deformed hands, extra fingers, missing fingers, bad anatomy, low resolution, watermark, text"
            "3D", "3D Render" -> "flat 2D, rough sketch, noisy background, deformed anatomy, uncanny valley, blurry textures, bad lighting, watermark"
            else -> "blurry, out of focus, low resolution, bad anatomy, deformed limbs, extra fingers, mutated hands, unnatural skin texture, oversaturated, watermark, signature, cropped, low quality artifacts"
        }

        val paramMap = mapOf(
            "Style" to options.style,
            "Lighting" to options.lighting,
            "Camera" to options.camera,
            "Aspect Ratio" to options.aspectRatio,
            "Quality" to options.quality,
            "Engine" to "Prompt Studio Synthesizer"
        )

        return GeneratedPromptResult(
            prompt = finalPrompt,
            negativePrompt = negativePrompt,
            parameters = paramMap,
            isFromGemini = false
        )
    }
}
