package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.MainViewModel
import com.example.util.PdfExporter
import java.io.File

import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Key
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.network.AiModuleConfig

@Composable
fun AiProviderSectionCard(viewModel: MainViewModel) {
    val aiModules by viewModel.aiModules.collectAsState()
    val activeAiModule by viewModel.activeAiModule.collectAsState()
    val context = LocalContext.current

    val providers = listOf(
        "Google GEMINI" to "Google Gemini API (Official)",
        "OpenAI" to "OpenAI ChatGPT API",
        "Claude" to "Anthropic Claude API",
        "OpenCode Zen" to "OpenCode Zen Multi-Model API",
        "Custom API" to "Custom API Endpoint / Provider"
    )

    var selectedProvider by remember(activeAiModule) {
        mutableStateOf(activeAiModule?.provider ?: "Google GEMINI")
    }

    // Find config for currently selected provider
    val currentConfig = remember(aiModules, selectedProvider) {
        aiModules.firstOrNull { it.provider.equals(selectedProvider, ignoreCase = true) }
            ?: AiModuleConfig(
                id = selectedProvider.lowercase().replace(" ", "_"),
                provider = selectedProvider,
                modelName = when (selectedProvider) {
                    "OpenAI" -> "gpt-4o"
                    "Claude" -> "claude-3-5-sonnet-20241022"
                    "OpenCode Zen" -> "opencode-zen-1"
                    "Custom API" -> "custom-model"
                    else -> "gemini-3.5-flash"
                },
                apiKey = "",
                customEndpoint = "",
                isActive = false
            )
    }

    var apiKeyInput by remember(currentConfig) { mutableStateOf(currentConfig.apiKey) }
    var modelNameInput by remember(currentConfig) { mutableStateOf(currentConfig.modelName) }
    var customEndpointInput by remember(currentConfig) { mutableStateOf(currentConfig.customEndpoint) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var testResultMsg by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }

    val presetModels = when {
        selectedProvider.contains("GEMINI", ignoreCase = true) -> listOf("gemini-3.5-flash", "gemini-1.5-pro", "gemini-2.0-flash")
        selectedProvider.contains("OPENAI", ignoreCase = true) -> listOf("gpt-4o", "gpt-4o-mini", "gpt-3.5-turbo")
        selectedProvider.contains("CLAUDE", ignoreCase = true) -> listOf("claude-3-5-sonnet-20241022", "claude-3-haiku-20240307", "claude-3-opus-20240229")
        selectedProvider.contains("OPENCODE", ignoreCase = true) -> listOf("opencode-zen-1", "deepseek-r1", "qwen-2.5-coder", "claude-3.5-sonnet", "gpt-4o")
        else -> listOf("custom-model", "default")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
        border = BorderStroke(1.5.dp, GoldPrimary),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Setup",
                        tint = GoldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MULTI-PROVIDER AI SETTINGS",
                        color = GoldPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                activeAiModule?.let { active ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x3300E5FF))
                            .border(1.dp, NeonCyan, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Active: ${active.provider}",
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Choose your AI provider (Gemini, ChatGPT, Claude, OpenCode Zen, or Custom), enter your secure API key, and select model.",
                color = TextMuted,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Provider Selection Chips
            Text(
                text = "1. SELECT AI PROVIDER:",
                color = GoldPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(providers) { (provKey, provLabel) ->
                    val isSelected = selectedProvider.equals(provKey, ignoreCase = true)
                    val isActiveProvider = activeAiModule?.provider?.equals(provKey, ignoreCase = true) == true

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) GoldPrimary else Color(0x44FFC107),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .background(
                                if (isSelected) GoldPrimary else if (isActiveProvider) Color(0x3300E5FF) else Color(0x33161A26)
                            )
                            .clickable {
                                selectedProvider = provKey
                                testResultMsg = null
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = provKey,
                                color = if (isSelected) TextDark else TextWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isActiveProvider) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Active",
                                    tint = if (isSelected) TextDark else NeonCyan,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Model Selection
            Text(
                text = "2. SELECT / ENTER MODEL:",
                color = GoldPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(presetModels) { mName ->
                    val isModelSelected = modelNameInput.equals(mName, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 1.dp,
                                color = if (isModelSelected) NeonCyan else Color(0x4400E5FF),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(if (isModelSelected) Color(0x4400E5FF) else Color(0x22161A26))
                            .clickable { modelNameInput = mName }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = mName,
                            color = if (isModelSelected) NeonCyan else TextWhite,
                            fontSize = 11.sp,
                            fontWeight = if (isModelSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = modelNameInput,
                onValueChange = { modelNameInput = it },
                label = { Text("Model Name (Custom / Exact ID)", color = TextMuted, fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = Color(0x6600E5FF),
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                singleLine = true
            )

            // Custom Endpoint field if Custom API selected
            if (selectedProvider.contains("Custom", ignoreCase = true) || selectedProvider.contains("OpenCode", ignoreCase = true)) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = customEndpointInput,
                    onValueChange = { customEndpointInput = it },
                    label = { Text("Custom API Base Endpoint (e.g. https://api.opencode.ai/v1)", color = TextMuted, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color(0x6600E5FF),
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Encrypted / Secure API Key Input Field
            Text(
                text = "3. PROVIDER API KEY (SECURE STORAGE):",
                color = GoldPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = { apiKeyInput = it },
                label = { Text("$selectedProvider API Key", color = TextMuted, fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                        Icon(
                            imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Key Visibility",
                            tint = GoldPrimary
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = Color(0x66FFC107),
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Save & Activate | Test Connection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Save & Activate Button
                Button(
                    onClick = {
                        val newConfig = currentConfig.copy(
                            provider = selectedProvider,
                            modelName = modelNameInput.ifBlank { "default" },
                            apiKey = apiKeyInput.trim(),
                            customEndpoint = customEndpointInput.trim(),
                            isActive = true
                        )
                        viewModel.saveOrUpdateAiModule(newConfig)
                        viewModel.setActiveAiModule(newConfig.id)
                        Toast.makeText(context, "✓ $selectedProvider activated successfully!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Save & Activate Provider",
                        color = TextDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Test Connection Button
                Button(
                    onClick = {
                        if (apiKeyInput.isBlank()) {
                            testResultMsg = "❌ API key check karein (Key is empty)"
                            return@Button
                        }
                        isTesting = true
                        testResultMsg = "Testing $selectedProvider connection..."

                        val testConfig = currentConfig.copy(
                            provider = selectedProvider,
                            modelName = modelNameInput.ifBlank { "default" },
                            apiKey = apiKeyInput.trim(),
                            customEndpoint = customEndpointInput.trim()
                        )

                        viewModel.testAiConnection(
                            testConfig,
                            onResult = { success, msg ->
                                isTesting = false
                                testResultMsg = if (success) {
                                    "Connected ✅ ($selectedProvider $modelNameInput is ready!)"
                                } else {
                                    "Failed ❌: $msg"
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NetworkCheck,
                            contentDescription = null,
                            tint = TextDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isTesting) "Testing..." else "Test API",
                            color = TextDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Test Result Banner
            testResultMsg?.let { msg ->
                Spacer(modifier = Modifier.height(10.dp))
                val isPass = msg.contains("Connected") || msg.contains("✅")
                val bannerBg = if (isPass) Color(0x3300C853) else Color(0x33FF1744)
                val bannerBorder = if (isPass) NeonGreen else Color(0xFFFF1744)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, bannerBorder, RoundedCornerShape(10.dp))
                        .background(bannerBg)
                        .padding(10.dp)
                ) {
                    Text(
                        text = msg,
                        color = if (isPass) NeonGreen else Color(0xFFFF8A80),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsPdfScreen(viewModel: MainViewModel) {
    val selectedMarket by viewModel.selectedMarket.collectAsState()
    val weeklyStats by viewModel.weeklyStats.collectAsState()
    val evaluations by viewModel.backtestEvaluations.collectAsState()
    val divisor by viewModel.divisor.collectAsState()

    val wallpaperPath by viewModel.wallpaperPath.collectAsState()
    val bgDimLevel by viewModel.bgDimLevel.collectAsState()

    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userPhone by viewModel.userPhone.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    val context = LocalContext.current
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setWallpaperFromUri(uri, context)
            Toast.makeText(context, "Wallpaper selected and saved in app!", Toast.LENGTH_SHORT).show()
        }
    }

    val serverUrl by viewModel.serverUrl.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    var customUrlInput by remember(serverUrl) { mutableStateOf(serverUrl) }
    var testStatusMessage by remember { mutableStateOf<String?>(null) }
    var isTestingServer by remember { mutableStateOf(false) }
    var showCodeTemplate by remember { mutableStateOf(false) }

    val sampleJsonCode = """
{
  "SHRIDEVI": [
    { "date": "20-07-2026", "open": "149", "jodi": "05", "close": "140" },
    { "date": "21-07-2026", "open": "230", "jodi": "30", "close": "350" },
    { "date": "22-07-2026", "open": "560", "jodi": "71", "close": "100" }
  ],
  "KALYAN": [
    { "date": "20-07-2026", "open": "348", "jodi": "56", "close": "123" },
    { "date": "21-07-2026", "open": "***", "jodi": "**", "close": "***" }
  ]
}
    """.trimIndent()

    val sampleTextCode = """
06-07-2026 / 478 - 95 - 249
07-07-2026 / 566 - 75 - 357
12-07-2026 / *** - ** - ***
    """.trimIndent()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Ultra Stylish Admin Panel Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.5.dp, GoldPrimary),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Panel",
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "STYLISH ADMIN PANEL",
                                color = GoldPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0x3300E676))
                                .border(1.dp, NeonGreen, RoundedCornerShape(20.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "VERIFIED DEVELOPER",
                                    color = NeonGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Main Profile Section: Image + Info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Sachin Profile Avatar Picture
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .border(2.dp, GoldPrimary, CircleShape)
                                .border(4.dp, Color(0x33FFC107), CircleShape)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_sachin_admin_1785594615532),
                                contentDescription = "Sachin Solunke Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userName,
                                color = GoldPrimary,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = userRole,
                                color = TextWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            // Email Tag with click to copy
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x3300E5FF))
                                    .border(0.8.dp, NeonCyan, RoundedCornerShape(8.dp))
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(userEmail))
                                        Toast.makeText(context, "Email copied: $userEmail", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "📧 $userEmail",
                                    color = NeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Email",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "🌐 OFFICIAL CONNECT & SOCIAL LINKS:",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    fun openLink(url: String, name: String) {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            clipboardManager.setText(AnnotatedString(url))
                            Toast.makeText(context, "$name link copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    // Social Action Grid
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // WhatsApp Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x2225D366))
                                    .border(1.dp, Color(0xFF25D366), RoundedCornerShape(10.dp))
                                    .clickable {
                                        openLink("https://chat.whatsapp.com/BSTELXcrbei88V8FS5rl3h?s=cl&p=a&ilr=1", "WhatsApp")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "💬", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "WhatsApp Group",
                                        color = Color(0xFF25D366),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Telegram Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x220088CC))
                                    .border(1.dp, Color(0xFF0088CC), RoundedCornerShape(10.dp))
                                    .clickable {
                                        openLink("https://t.me/Open_network_Sachin", "Telegram")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "✈️", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Telegram Channel",
                                        color = Color(0xFF0088CC),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Instagram Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x22E1306C))
                                    .border(1.dp, Color(0xFFE1306C), RoundedCornerShape(10.dp))
                                    .clickable {
                                        openLink("https://www.instagram.com/black_b.o.y__?igsh=MWp5aWNqdWFqbjc3dg==", "Instagram")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "📸", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "@black_b.o.y__",
                                        color = Color(0xFFE1306C),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Facebook Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x221877F2))
                                    .border(1.dp, Color(0xFF1877F2), RoundedCornerShape(10.dp))
                                    .clickable {
                                        openLink("https://www.facebook.com/share/19Jzg5qiGa/", "Facebook")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "📘", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Facebook Profile",
                                        color = Color(0xFF1877F2),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // GitHub Data Repo Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x22FFC107))
                                .border(1.dp, GoldPrimary, RoundedCornerShape(10.dp))
                                .clickable {
                                    openLink("https://raw.githubusercontent.com/sachin-a23/A23site/", "GitHub Raw Site")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🐙", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "GitHub Raw Data Repository (A23site)",
                                    color = GoldPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Copy All Contact Details Action
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x11FFFFFF))
                            .border(0.8.dp, Color(0x44FFFFFF), RoundedCornerShape(10.dp))
                            .clickable {
                                val allDetails = """
                                    Admin: Sachin Solunke
                                    Email: woldcom87@gmail.com
                                    WhatsApp: https://chat.whatsapp.com/BSTELXcrbei88V8FS5rl3h?s=cl&p=a&ilr=1
                                    Telegram: https://t.me/Open_network_Sachin
                                    Instagram: https://www.instagram.com/black_b.o.y__?igsh=MWp5aWNqdWFqbjc3dg==
                                    Facebook: https://www.facebook.com/share/19Jzg5qiGa/
                                    GitHub Data: https://raw.githubusercontent.com/sachin-a23/A23site/
                                """.trimIndent()
                                clipboardManager.setText(AnnotatedString(allDetails))
                                Toast.makeText(context, "All Admin contact links copied!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = TextWhite,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "📋 Copy All Admin Links & Info",
                                color = TextWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Multi-Provider AI Settings Section
        item {
            AiProviderSectionCard(viewModel = viewModel)
        }

        // Permissions & Toggles Section
        item {
            PermissionsTogglesCard()
        }

        // Background Wallpaper & Dimming Settings Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.dp, GoldPrimary),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Wallpaper,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WALLPAPER & DIMMING SETTINGS",
                            color = GoldPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Select custom background wallpaper from gallery and adjust dim intensity.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dim Level Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔅 Dim Intensity:",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${bgDimLevel.toInt()}%",
                            color = GoldPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Slider(
                        value = bgDimLevel,
                        onValueChange = { viewModel.setDimLevel(it) },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = GoldPrimary,
                            activeTrackColor = GoldPrimary,
                            inactiveTrackColor = Color(0x55FFC107)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "0% (Bright)", color = TextMuted, fontSize = 10.sp)
                        Text(text = "50%", color = TextMuted, fontSize = 10.sp)
                        Text(text = "100% (Dark)", color = TextMuted, fontSize = 10.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset Dimming Level Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(0f to "0% Clear", 10f to "10% Soft", 20f to "20% Default", 35f to "35% Dim", 50f to "50% Dark").forEach { (preset, label) ->
                            val isSel = (bgDimLevel - preset).let { kotlin.math.abs(it) < 4f }
                            Button(
                                onClick = { viewModel.setDimLevel(preset) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSel) GoldPrimary else Color(0x33161A26)
                                ),
                                border = BorderStroke(1.dp, GoldPrimary),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 1.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) TextDark else TextWhite,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "🎨 BUILT-IN APP WALLPAPERS:",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val presets = listOf(
                        Triple("img_circuit_bg_1785562607299", "Gold Circuit", R.drawable.img_circuit_bg_1785562607299),
                        Triple("img_bg_cyber_gold_1785594071449", "Cyber Gold", R.drawable.img_bg_cyber_gold_1785594071449),
                        Triple("img_bg_matrix_green_1785594091879", "Matrix Green", R.drawable.img_bg_matrix_green_1785594091879),
                        Triple("img_bg_neon_cyan_1785594103530", "Neon Cyan", R.drawable.img_bg_neon_cyan_1785594103530),
                        Triple("img_bg_royal_purple_1785594115845", "Royal Purple", R.drawable.img_bg_royal_purple_1785594115845)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(presets) { (resName, title, drawableId) ->
                            val isSelected = (wallpaperPath == "res:$resName") || 
                                (wallpaperPath == null && resName == "img_circuit_bg_1785562607299")

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(82.dp)
                                    .clickable {
                                        viewModel.setPresetWallpaper(resName)
                                        Toast.makeText(context, "$title Wallpaper Applied!", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(82.dp, 120.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) GoldPrimary else Color(0x44FFC107),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Image(
                                        painter = painterResource(id = drawableId),
                                        contentDescription = title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .clip(CircleShape)
                                                .background(GoldPrimary)
                                                .padding(3.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Active",
                                                tint = TextDark,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = title,
                                    color = if (isSelected) GoldPrimary else TextWhite,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Gallery Wallpaper Selection Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = TextDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Choose Custom Gallery Image",
                                    color = TextDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (wallpaperPath != null) {
                            Button(
                                onClick = {
                                    viewModel.resetWallpaper()
                                    Toast.makeText(context, "Reset to default wallpaper", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.height(46.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF1744)),
                                border = BorderStroke(1.dp, Color(0xFFFF1744)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = "Reset",
                                    tint = Color(0xFFFF1744),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (wallpaperPath != null) "✓ Custom wallpaper active & saved in app" else "Default circuit board background active",
                        color = if (wallpaperPath != null) NeonGreen else TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // PDF Export & Download Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.dp, NeonCyan),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PDF REPORT GENERATOR",
                            color = NeonCyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Convert $selectedMarket backtest history & OTC accuracy report into PDF document saved in phone storage.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Generate PDF Button
                    Button(
                        onClick = {
                            val pdf = PdfExporter.generatePdfReport(
                                context = context,
                                marketName = selectedMarket,
                                stats = weeklyStats,
                                evaluations = evaluations
                            )
                            if (pdf != null) {
                                generatedPdfFile = pdf
                                Toast.makeText(context, "✓ PDF Saved in Downloads/GSM_PRO_Reports:\n${pdf.name}", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Failed to generate PDF.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = TextDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Convert & Save PDF to Storage",
                                color = TextDark,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (generatedPdfFile != null) {
                        Spacer(modifier = Modifier.height(10.dp))

                        // Share PDF Button
                        Button(
                            onClick = {
                                generatedPdfFile?.let { file ->
                                    PdfExporter.sharePdfFile(context, file)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = TextDark,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Share PDF Report Document",
                                    color = TextDark,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Formula Settings Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.dp, Color(0x66FFC107)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FORMULA ENGINE DIVISOR CONFIG",
                        color = GoldPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Choose formula divisor calculation rule (÷ 8 standard vs ÷ 3 fast formula):",
                        color = TextMuted,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(8, 3).forEach { divOption ->
                            val isSelected = divisor == divOption
                            Button(
                                onClick = { viewModel.setDivisor(divOption) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) GoldPrimary else Color(0x33161A26)
                                ),
                                border = BorderStroke(1.dp, GoldPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "÷ $divOption Formula ${if (divOption == 8) "(Default)" else "(Fast)"}",
                                    color = if (isSelected) TextDark else TextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // GitHub Data Server Configuration & Setup Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.dp, NeonCyan),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GITHUB DATA SERVER SETUP",
                                color = NeonCyan,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { showCodeTemplate = !showCodeTemplate },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showCodeTemplate) NeonCyan else Color(0x3300E5FF)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    tint = if (showCodeTemplate) TextDark else NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (showCodeTemplate) "Hide Setup Code" else "Show Setup Code",
                                    color = if (showCodeTemplate) TextDark else NeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Set your custom GitHub data.json raw URL here. The app fetches live market results from this link.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Server URL Input
                    OutlinedTextField(
                        value = customUrlInput,
                        onValueChange = { customUrlInput = it },
                        label = { Text("GitHub Server Raw JSON Link", color = TextMuted, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color(0x6600E5FF),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action Buttons: Save URL | Test Connection | Default Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Save Link Button
                        Button(
                            onClick = {
                                viewModel.setServerUrl(customUrlInput)
                                Toast.makeText(context, "✓ Server URL Saved!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Save Link",
                                color = TextDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Test Connection Button
                        Button(
                            onClick = {
                                isTestingServer = true
                                testStatusMessage = "Testing server connection..."
                                viewModel.testServerUrl(customUrlInput) { success, message ->
                                    isTestingServer = false
                                    testStatusMessage = message
                                }
                            },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NetworkCheck,
                                    contentDescription = null,
                                    tint = TextDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isTestingServer) "Testing..." else "Test Server",
                                    color = TextDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Reset Default Button
                        Button(
                            onClick = {
                                val defaultUrl = "https://raw.githubusercontent.com/sachin-a23/A23site/main/data.json"
                                customUrlInput = defaultUrl
                                viewModel.setServerUrl(defaultUrl)
                                Toast.makeText(context, "Default GitHub link restored!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFC107)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                        ) {
                            Text(
                                text = "Default",
                                color = GoldPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Test Result Banner
                    testStatusMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(10.dp))
                        val isPass = msg.contains("Passed") || msg.contains("✓")
                        val bannerColor = if (isPass) Color(0x3300C853) else Color(0x33FF1744)
                        val borderColor = if (isPass) NeonGreen else Color(0xFFFF1744)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                                .background(bannerColor)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = msg,
                                color = if (isPass) NeonGreen else Color(0xFFFF8A80),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Expandable Setup Code Section
                    if (showCodeTemplate) {
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Text(
                            text = "📋 GITHUB SETUP CODE (data.json Format):",
                            color = GoldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // JSON Format Box with Copy Button
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0x66FFC107), RoundedCornerShape(10.dp))
                                .background(Color(0xFF101420))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "JSON Format (Recommended):",
                                    color = NeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(sampleJsonCode))
                                        Toast.makeText(context, "✓ Copied JSON Setup Code!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = null,
                                            tint = TextDark,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Copy JSON",
                                            color = TextDark,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = sampleJsonCode,
                                color = Color(0xFFE0E0E0),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Plain Text Format Box with Copy Button
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0x6600E5FF), RoundedCornerShape(10.dp))
                                .background(Color(0xFF101420))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Plain Text Line Format:",
                                    color = GoldPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(sampleTextCode))
                                        Toast.makeText(context, "✓ Copied Text Format Code!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = null,
                                            tint = TextDark,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Copy Text Format",
                                            color = TextDark,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = sampleTextCode,
                                color = Color(0xFFE0E0E0),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Database Reset Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.dp, Color(0x66FFC107)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Data Server Refresh",
                        color = GoldPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.syncGitHubData() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x44FFC107)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Resync Database from GitHub",
                                color = GoldPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionsTogglesCard() {
    val context = LocalContext.current

    var isNotificationGranted by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    var isStorageGranted by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    var isSmsGranted by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECEIVE_SMS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isNotificationGranted = granted
    }

    val storageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isStorageGranted = granted
    }

    val smsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isSmsGranted = granted
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
        border = BorderStroke(1.5.dp, GoldPrimary),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "System Permissions",
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SYSTEM PERMISSIONS & TOGGLES",
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Accessibility Service Permission Toggle
            PermissionRowItem(
                title = "Accessibility Service",
                subtitle = "Enable Accessibility automation features",
                isChecked = false,
                onToggle = {
                    try {
                        val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        context.startActivity(intent)
                        Toast.makeText(context, "Opening Accessibility Settings...", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Unable to open settings", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Notification Permission
            PermissionRowItem(
                title = "Notification Permission",
                subtitle = "Receive live market alert updates",
                isChecked = isNotificationGranted,
                onToggle = {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        Toast.makeText(context, "Notifications enabled by default on this Android version", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Storage Permission
            PermissionRowItem(
                title = "Storage Access Permission",
                subtitle = "Read/Write files, chart images and PDFs",
                isChecked = isStorageGranted,
                onToggle = {
                    storageLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4. SMS Permission
            PermissionRowItem(
                title = "SMS Permission",
                subtitle = "Read incoming market result SMS messages",
                isChecked = isSmsGranted,
                onToggle = {
                    smsLauncher.launch(android.Manifest.permission.RECEIVE_SMS)
                }
            )
        }
    }
}

@Composable
fun PermissionRowItem(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x3310131E))
            .border(1.dp, Color(0x44FFC107), RoundedCornerShape(12.dp))
            .clickable { onToggle() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = GoldPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            androidx.compose.material3.Switch(
                checked = isChecked,
                onCheckedChange = { onToggle() },
                colors = androidx.compose.material3.SwitchDefaults.colors(
                    checkedThumbColor = GoldPrimary,
                    checkedTrackColor = Color(0xFF388E3C),
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = Color(0x3310131E)
                )
            )
        }
    }
}
