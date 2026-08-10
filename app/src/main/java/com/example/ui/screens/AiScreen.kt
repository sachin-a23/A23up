package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import com.example.network.ChatMessage
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.MainViewModel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun AiScreen(
    viewModel: MainViewModel,
    onOpenSettings: () -> Unit
) {
    val activeAiModule by viewModel.activeAiModule.collectAsState()
    val chatMessages by viewModel.aiChatMessages.collectAsState()
    val selectedMarket by viewModel.selectedMarket.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()
    val aiThinkingStatus by viewModel.aiThinkingStatus.collectAsState()
    val pendingAiAction by viewModel.pendingAiAction.collectAsState()
    val aiLanguage by viewModel.aiLanguage.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var userInputText by rememberSaveable { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Auto-scroll to latest message when new messages arrive or when AI is thinking
    LaunchedEffect(chatMessages.size, isAiThinking) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    val handleSend = {
        val query = userInputText.trim()
        if (query.isNotBlank() && !isSending && !isAiThinking) {
            isSending = true
            userInputText = ""
            viewModel.sendAiQuery(
                query = query,
                onComplete = {
                    isSending = false
                }
            )
        }
    }

    val quickPrompts = listOf(
        "🎯 Predict Today's OTC Digits",
        "🔥 Kalyan & Market Pattern Analysis",
        "🎰 Calculate Best Jodi & Panel",
        "💡 Explain Cut Total Math Formula",
        "📈 Analyze Weekly Chart History"
    )

    // Action Confirmation Popup Dialog (e.g. Save Record Permission)
    pendingAiAction?.let { action ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelPendingAiAction() },
            containerColor = Color(0xFF1E293B),
            title = {
                Text(
                    text = "🤖 AI Action Confirmation",
                    color = GoldPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "AI ye naya record history me save karna chahta hai. Kya aap allow karna chahte hain?",
                        color = TextWhite,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, GoldPrimary, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("🏛️ Market: ${action.market}", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("📅 Date: ${action.date}", color = TextWhite, fontSize = 12.sp)
                            Text("🎰 Result: ${action.openPanel} - ${action.jodi} - ${action.closePanel}", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmPendingAiAction() },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Allow (Save Karein)", color = TextDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelPendingAiAction() }) {
                    Text("Cancel (Nahi)", color = TextMuted)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Active AI Provider Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCardBg),
            border = BorderStroke(1.5.dp, GoldPrimary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Studio",
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "A23 MULTI-PROVIDER AI",
                            color = GoldPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    val providerName = activeAiModule?.provider ?: "Google GEMINI"
                    val modelName = activeAiModule?.modelName ?: "gemini-3.5-flash"
                    Text(
                        text = "Active: $providerName ($modelName)",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Change Provider Button -> Navigates to Settings
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33FFC107))
                        .border(1.dp, GoldPrimary, RoundedCornerShape(12.dp))
                        .clickable { onOpenSettings() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = GoldPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Change Provider",
                            color = GoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // AI Response Language Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "🌐 AI Language:",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val languages = listOf(
                    "Hinglish" to "Hinglish (Hindi+Eng)",
                    "Hindi" to "हिंदी (Hindi)",
                    "English" to "English"
                )
                languages.forEach { (key, label) ->
                    val isSelected = aiLanguage.equals(key, ignoreCase = true) || (key == "Hinglish" && aiLanguage.contains("hing", ignoreCase = true))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) GoldPrimary else Color(0x22161A26))
                            .border(1.dp, if (isSelected) GoldPrimary else Color(0x4400E5FF), RoundedCornerShape(8.dp))
                            .clickable { viewModel.setAiLanguage(key) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) TextDark else TextWhite,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Analysis Prompts
        Text(
            text = "QUICK AI ANALYSIS COMMANDS:",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickPrompts) { prompt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0x6600E5FF), RoundedCornerShape(12.dp))
                        .background(Color(0x22161A26))
                        .clickable {
                            userInputText = prompt
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = prompt,
                        color = TextWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chat Output Log List
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F131E))
                .border(1.dp, Color(0x33FFC107), RoundedCornerShape(16.dp))
                .padding(10.dp)
        ) {
            if (chatMessages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = GoldPrimary.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "A23 Multi-Provider AI Studio",
                        color = GoldPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Select a quick prompt or type your query below to analyze $selectedMarket market patterns using your configured AI model.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(chatMessages, key = { it.id }) { msg ->
                        AiMessageBubble(
                            message = msg,
                            onCopy = { text ->
                                clipboardManager.setText(AnnotatedString(text))
                                Toast.makeText(context, "Copied AI response!", Toast.LENGTH_SHORT).show()
                            },
                            onShare = { text ->
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_TEXT, text)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share AI Prediction"))
                            },
                            onOpenSettings = onOpenSettings
                        )
                    }
                }
            }
        }

        // AI Thinking / Status Loading Indicator Badge
        AnimatedVisibility(
            visible = isAiThinking,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x3300E5FF))
                    .border(1.dp, NeonCyan, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Thinking",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = aiThinkingStatus ?: "🤖 AI soch raha hai... data check kar raha hai...",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Input Field & Send Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userInputText,
                onValueChange = { userInputText = it },
                placeholder = { Text("Ask AI for $selectedMarket OTC prediction...", color = TextMuted, fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { handleSend() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = Color(0x66FFC107),
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedContainerColor = CyberCardBg,
                    unfocusedContainerColor = CyberCardBg
                ),
                singleLine = true,
                maxLines = 1
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { handleSend() },
                modifier = Modifier.height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(14.dp),
                enabled = !isSending && !isAiThinking && userInputText.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = TextDark,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AiMessageBubble(
    message: ChatMessage,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    val isUser = message.sender == "user"
    val isErr = message.isError || message.text.startsWith("Error") || message.text.contains("API key check")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.82f else 0.95f)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isUser) Color(0x44FFC107)
                    else if (isErr) Color(0x33FF1744)
                    else Color(0x33161A26)
                )
                .border(
                    width = 1.dp,
                    color = if (isUser) GoldPrimary else if (isErr) Color(0xFFFF1744) else Color(0x4400E5FF),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(12.dp)
        ) {
            // Bubble Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isUser) " You" else if (isErr) "⚠️ AI Provider Error" else "🤖 A23 PRO AI",
                    color = if (isUser) GoldPrimary else if (isErr) Color(0xFFFF8A80) else NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                if (!isUser && !isErr) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { onCopy(message.text) }, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = GoldPrimary, modifier = Modifier.size(14.dp))
                        }
                        IconButton(onClick = { onShare(message.text) }, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = NeonCyan, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Text Content
            Text(
                text = message.text,
                color = TextWhite,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            // Direct button to open settings if error
            if (isErr) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(GoldPrimary)
                        .clickable { onOpenSettings() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = TextDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "⚙️ API Key Check Karein (Open Settings)",
                            color = TextDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
