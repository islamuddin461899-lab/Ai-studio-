package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.services.AiService
import com.example.services.AudioVoiceService
import com.example.services.PromptGenerationOptions
import com.example.services.VisualGenerationEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Prompt Studio", appName)
  }

  @Test
  fun `test prompt synthesis engine with nomad prompt`() {
    val options = PromptGenerationOptions(
      userIdea = "যাযাবর",
      language = "বাংলা + English",
      style = "Cinematic",
      aspectRatio = "16:9",
      camera = "DSLR",
      lighting = "Golden Hour",
      quality = "Ultra Detailed"
    )
    val result = AiService.synthesizePrompt(options)
    assertNotNull(result)
    assertTrue(result.prompt.contains("যাযাবর") || result.prompt.contains("মরুভূমি") || result.prompt.contains("nomad") || result.prompt.contains("wanderer"))
    assertTrue(result.negativePrompt.isNotBlank())
    assertEquals("Cinematic", result.parameters["Style"])
  }

  @Test
  fun `test visual generation engine generates nomad artwork`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val result = VisualGenerationEngine.generateImage(
      context = context,
      promptText = "যাযাবর মরুভূমির সূর্যাস্তে উটের কাফেলা",
      style = "Cinematic",
      aspectRatio = "16:9"
    )
    assertTrue("Image generation should succeed", result.isSuccess)
    val filePath = result.getOrNull()
    assertNotNull(filePath)
    val file = File(filePath!!)
    assertTrue("Generated file should exist", file.exists())
    assertTrue("Generated file should not be empty", file.length() > 0)
  }

  @Test
  fun `test voice narration script generation`() {
    val scriptBn = AudioVoiceService.generateNarrationScript("যাযাবর", "Cinematic", isBengali = true)
    assertNotNull(scriptBn)
    assertTrue(scriptBn.isNotBlank())
    assertTrue(scriptBn.contains("যাযাবর") || scriptBn.contains("পথ") || scriptBn.contains("মরুভূমি"))

    val scriptEn = AudioVoiceService.generateNarrationScript("Desert Nomad", "Cinematic", isBengali = false)
    assertNotNull(scriptEn)
    assertTrue(scriptEn.isNotBlank())
  }
}
