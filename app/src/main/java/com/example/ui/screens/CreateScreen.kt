package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.StudioMode
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateScreen(
    studioMode: StudioMode,
    onStudioModeChange: (StudioMode) -> Unit,
    userIdea: String,
    onUserIdeaChange: (String) -> Unit,
    promptLanguage: String,
    onPromptLanguageChange: (String) -> Unit,
    imageStyle: String,
    onImageStyleChange: (String) -> Unit,
    aspectRatio: String,
    onAspectRatioChange: (String) -> Unit,
    camera: String,
    onCameraChange: (String) -> Unit,
    lighting: String,
    onLightingChange: (String) -> Unit,
    quality: String,
    onQualityChange: (String) -> Unit,
    extraDetails: String,
    onExtraDetailsChange: (String) -> Unit,
    selectedPhotoUri: String?,
    onSelectedPhotoChange: (String?) -> Unit,
    photoEditPrompt: String,
    onPhotoEditPromptChange: (String) -> Unit,
    videoInputMode: String,
    onVideoInputModeChange: (String) -> Unit,
    videoPrompt: String,
    onVideoPromptChange: (String) -> Unit,
    videoVoiceGender: String = "FEMALE",
    onVideoVoiceGenderChange: (String) -> Unit = {},
    videoAspectRatio: String = "9:16",
    onVideoAspectRatioChange: (String) -> Unit = {},
    includeVoiceInVideo: Boolean,
    onIncludeVoiceChange: (Boolean) -> Unit,
    isGenerating: Boolean,
    errorMessage: String?,
    onGenerateImageClick: () -> Unit,
    onTransformPhotoClick: () -> Unit,
    onGenerateVideoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onSelectedPhotoChange(uri.toString())
            }
        }
    )

    val styleOptions = listOf(
        "Cinematic", "Photorealistic", "3D", "Anime", "Cartoon",
        "Digital Art", "Fantasy", "Watercolor", "Oil Painting", "Studio Photography"
    )
    val aspectRatioOptions = listOf("1:1", "16:9", "9:16", "4:5")
    val languageOptions = listOf("বাংলা", "English", "বাংলা + English")

    Surface(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Studio Header
            Text(
                text = "এআই ক্রিয়েশন স্টুডিও ✨",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "টেক্সট ও ছবি থেকে ইমেজ, ভিডিও ও ভয়েস তৈরি করুন",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            // Studio Mode Selector Tabs
            val modes = listOf(
                Triple(StudioMode.TEXT_TO_IMAGE, "টেক্সট থেকে ছবি", Icons.Default.Image),
                Triple(StudioMode.PHOTO_TO_IMAGE, "ফটো + প্রম্পট", Icons.Default.PhotoCamera),
                Triple(StudioMode.VIDEO_AND_VOICE, "ভিডিও ও ভয়েস", Icons.Default.Movie)
            )

            TabRow(
                selectedTabIndex = studioMode.ordinal,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[studioMode.ordinal]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                modes.forEach { (mode, title, icon) ->
                    val selected = studioMode == mode
                    Tab(
                        selected = selected,
                        onClick = { onStudioModeChange(mode) },
                        modifier = Modifier.testTag("tab_${mode.name}"),
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // MODE 1: TEXT TO IMAGE
            if (studioMode == StudioMode.TEXT_TO_IMAGE) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "আপনার আইডিয়া বা প্রম্পট লিখুন",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = userIdea,
                            onValueChange = onUserIdeaChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("idea_input_field"),
                            placeholder = {
                                Text(
                                    text = "যেমন: যাযাবর, মরুভূমির সূর্যাস্তে উটের কাফেলা, শান্ত নদীর ধারে গ্রামীণ যুবক, বা যেকোনো দৃশ্য...",
                                    fontSize = 14.sp
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Prompt Suggestions Chips (Quick Prompts)
                        Text(
                            text = "💡 দ্রুত আইডিয়া নির্বাচন করুন:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val quickIdeas = listOf(
                            "যাযাবর",
                            "মরুভূমির সূর্যাস্তে কাফেলা",
                            "সবুজ ধানক্ষেতে কৃষক",
                            "সাইবারপাঙ্ক নিয়ন সিটি",
                            "কিউট ৩ডি কার্টুন চরিত্র",
                            "মেঘলা দিনে নদীর নৌকা"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickIdeas.forEach { idea ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (userIdea == idea) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { onUserIdeaChange(idea) }
                                ) {
                                    Text(
                                        text = idea,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (userIdea == idea) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Style & Aspect Ratio Selection
                StyleAndAspectSection(
                    imageStyle = imageStyle,
                    onImageStyleChange = onImageStyleChange,
                    aspectRatio = aspectRatio,
                    onAspectRatioChange = onAspectRatioChange,
                    styleOptions = styleOptions,
                    aspectRatioOptions = aspectRatioOptions
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Generate Image Button
                Button(
                    onClick = onGenerateImageClick,
                    enabled = !isGenerating && userIdea.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("generate_image_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("এআই ইমেজ তৈরি হচ্ছে...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("✨ এআই ইমেজ তৈরি করুন", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // MODE 2: PHOTO TO IMAGE (GALLERY PICKER + PROMPT)
            if (studioMode == StudioMode.PHOTO_TO_IMAGE) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "১. গ্যালারি থেকে ফটো নির্বাচন করুন",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (selectedPhotoUri != null) {
                            // Selected Image Preview Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(if (selectedPhotoUri.startsWith("/") || selectedPhotoUri.startsWith("content://")) selectedPhotoUri else File(selectedPhotoUri))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Selected photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                IconButton(
                                    onClick = { onSelectedPhotoChange(null) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        .testTag("remove_selected_photo")
                                ) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = Color.White)
                                }

                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                        .testTag("change_photo_button"),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
                                ) {
                                    Text("ফটো পরিবর্তন", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        } else {
                            // Empty state - Click to pick photo
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    }
                                    .testTag("pick_photo_area"),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Pick photo",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "গ্যালারি থেকে ফটো সিলেক্ট করতে ট্যাপ করুন",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "PNG, JPG বা WebP ফরম্যাট",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "২. ফটোটি কীভাবে রূপান্তর করতে চান লিখুন",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = photoEditPrompt,
                            onValueChange = onPhotoEditPromptChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("photo_edit_prompt_input"),
                            placeholder = {
                                Text("যেমন: যাযাবর পোশাকে রূপান্তর করো, ব্যাকগ্রাউন্ডে মরুভূমি ও সূর্যাস্ত যোগ করো, সিনেমাটিক লুক দাও...")
                            },
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick transform presets
                        Text(
                            text = "🎨 জনপ্রিয় রূপান্তর প্রিসেট:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val photoPresets = listOf(
                            "যাযাবর মরুভূমি স্টাইল",
                            "সিনেমাটিক গোল্ডেন আওয়ার",
                            "সাইবারপাঙ্ক নিয়ন লুক",
                            "ভিন্টেজ তেলচিত্র রূপান্তর",
                            "অ্যানিমে পোর্ট্রেট",
                            "৩ডি কার্টুন স্টাইল"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            photoPresets.forEach { preset ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (photoEditPrompt == preset) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { onPhotoEditPromptChange(preset) }
                                ) {
                                    Text(
                                        text = preset,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (photoEditPrompt == preset) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Transform Photo Action Button
                Button(
                    onClick = onTransformPhotoClick,
                    enabled = !isGenerating && selectedPhotoUri != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("transform_photo_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("ফটো রূপান্তর হচ্ছে...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🎨 ফটো রূপান্তর করুন", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // MODE 3: VIDEO & VOICE STUDIO
            if (studioMode == StudioMode.VIDEO_AND_VOICE) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ভিডিও তৈরির মাধ্যম নির্বাচন করুন",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Input Mode Toggle (Photo vs Prompt)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onVideoInputModeChange("PHOTO") }
                                    .testTag("video_mode_photo"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (videoInputMode == "PHOTO") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (videoInputMode == "PHOTO") BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ফটো দিয়ে ভিডিও", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onVideoInputModeChange("PROMPT") }
                                    .testTag("video_mode_prompt"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (videoInputMode == "PROMPT") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (videoInputMode == "PROMPT") BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("প্রম্পট লিখে ভিডিও", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // If Photo Mode, show photo picker
                        if (videoInputMode == "PHOTO") {
                            if (selectedPhotoUri != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(if (selectedPhotoUri.startsWith("/") || selectedPhotoUri.startsWith("content://")) selectedPhotoUri else File(selectedPhotoUri))
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Selected photo for video",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    IconButton(
                                        onClick = { onSelectedPhotoChange(null) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = Color.White)
                                    }
                                }
                            } else {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("গ্যালারি থেকে ফটো সিলেক্ট করুন", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Video Story / Prompt
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (videoInputMode == "PHOTO") "ভিডিওর ক্যাপশন বা গল্প (ঐচ্ছিক)" else "ভিডিওর জন্য প্রম্পট বা গল্প লিখুন",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Presets inspiration chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                modifier = Modifier.clickable {
                                    onVideoPromptChange("এই মেয়ে টি বাংলা য় বলবে একটি অত্যন্ত বাস্তবসম্মত ও সুন্দর সিনেমাটিক ছবি। একটি সাদা সিরামিক ফুলের ডিজাইনের কাপের মধ্যে গরম দুধ-চা, চায়ের উপরে ঘন ফেনা ও অসংখ্য ছোট বুদবুদ দেখা যাচ্ছে। চায়ের ফেনার মাঝখানে সুন্দরভাবে বাংলা ক্যালিগ্রাফি স্টাইলে “মা” লেখা থাকবে। কাপটি একটি পরিষ্কার টেবিলের ওপর রাখা, পেছনে নরম ব্লার করা ফুল ও ঘরের পরিবেশ। উষ্ণ সকালের আলো, নরম ছায়া, shallow depth of field, realistic reflections, highly detailed, photorealistic, cinematic lighting, 4K quality, vertical 9:16 composition। ছবিতে কোনো সোশ্যাল মিডিয়া UI, লাইক, কমেন্ট, ফলো বাটন বা অতিরিক্ত লেখা থাকবে না।")
                                    onVideoVoiceGenderChange("FEMALE")
                                    onVideoAspectRatioChange("9:16")
                                    onIncludeVoiceChange(true)
                                }
                            ) {
                                Text(
                                    text = "☕ মা ও দুধ-চা (মেয়ের কণ্ঠে)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.clickable {
                                    onVideoPromptChange("যাযাবরের জীবনের এক দিন ও পথচলা, মরুভূমির বালিয়াড়িতে দীর্ঘ ছায়া ফেলে হেঁটে চলা এক নিঃসঙ্গ পথিকের সিনেমাটিক গল্প।")
                                    onVideoVoiceGenderChange("MALE")
                                    onVideoAspectRatioChange("16:9")
                                }
                            ) {
                                Text(
                                    text = "🏜️ মরুভূমির যাযাবর পথিক",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = videoPrompt,
                            onValueChange = onVideoPromptChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .testTag("video_prompt_input"),
                            placeholder = {
                                Text("যেমন: যাযাবরের জীবনের এক দিন ও পথচলা, মরুভূমির বালিয়াড়িতে পথ চলার গল্প...")
                            },
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Voiceover settings toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("ভিডিওর সাথে ভয়েস যোগ করুন", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text("এআই স্বয়ংক্রিয়ভাবে ভিডিওতে ভয়েস বলবে", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = includeVoiceInVideo,
                                onCheckedChange = onIncludeVoiceChange,
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("video_voice_switch")
                            )
                        }

                        // Voice Gender options
                        if (includeVoiceInVideo) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "ভয়েসের কণ্ঠের ধরন:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (videoVoiceGender == "FEMALE") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (videoVoiceGender == "FEMALE") BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                    modifier = Modifier.weight(1f).clickable { onVideoVoiceGenderChange("FEMALE") }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("👩 মেয়ের মিষ্টি কণ্ঠ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (videoVoiceGender == "MALE") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (videoVoiceGender == "MALE") BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                    modifier = Modifier.weight(1f).clickable { onVideoVoiceGenderChange("MALE") }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("👨 পুরুষের ভয়েস", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        // Video Aspect Ratio options
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "ভিডিওর অনুপাত / সাইজ:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (videoAspectRatio == "9:16") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (videoAspectRatio == "9:16") BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier.weight(1f).clickable { onVideoAspectRatioChange("9:16") }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📱 9:16 (Vertical Reel)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (videoAspectRatio == "16:9") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (videoAspectRatio == "16:9") BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier.weight(1f).clickable { onVideoAspectRatioChange("16:9") }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🎬 16:9 (Cinema)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Generate Video & Voice Button
                Button(
                    onClick = onGenerateVideoClick,
                    enabled = !isGenerating && (videoInputMode == "PHOTO" && selectedPhotoUri != null || videoInputMode == "PROMPT" && videoPrompt.isNotBlank()),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("generate_video_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("ভিডিও ও ভয়েস তৈরি হচ্ছে...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.Movie, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🎬 ভিডিও ও ভয়েস তৈরি করুন", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Error Message Banner
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (errorMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StyleAndAspectSection(
    imageStyle: String,
    onImageStyleChange: (String) -> Unit,
    aspectRatio: String,
    onAspectRatioChange: (String) -> Unit,
    styleOptions: List<String>,
    aspectRatioOptions: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ছবির স্টাইল (Style)",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                styleOptions.forEach { style ->
                    val isSelected = imageStyle == style
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { onImageStyleChange(style) }
                    ) {
                        Text(
                            text = style,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "অনুপাত (Aspect Ratio)",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                aspectRatioOptions.forEach { ratio ->
                    val isSelected = aspectRatio == ratio
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onAspectRatioChange(ratio) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Text(
                            text = ratio,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
