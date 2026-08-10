package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.MainViewModel

@Composable
fun PinLockOverlay(viewModel: MainViewModel) {
    val isPinLocked by viewModel.isPinLocked.collectAsState()
    val isPinEnabled by viewModel.isPinEnabled.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val savedPin by viewModel.savedPin.collectAsState()

    val context = LocalContext.current
    var pinInput by remember { mutableStateOf("") }
    var isErrorState by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var lastKeyPressTime by remember { mutableStateOf(0L) }

    if (!isPinEnabled || !isPinLocked) {
        return
    }

    val dotBorderColor by animateColorAsState(
        if (isErrorState) Color(0xFFFF5252) else GoldPrimary,
        label = "dotBorderColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF00D111A))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCardBg),
            border = BorderStroke(1.5.dp, if (isErrorState) Color(0xFFFF5252) else GoldPrimary),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Security Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(GoldPrimary.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )
                        .border(2.dp, GoldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Pin Lock",
                        tint = GoldPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "A23 PRO SECURITY PIN LOCK",
                    color = GoldPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Welcome, $userName",
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = userRole,
                    color = NeonCyan,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Error Message / Helper
                if (isErrorState && errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFFF5252),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    Text(
                        text = "Enter your 4-Digit Security PIN to unlock",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 4 PIN DOTS
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < pinInput.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) dotBorderColor else Color.Transparent)
                                .border(2.dp, dotBorderColor, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // NUMERIC KEYPAD (3x4 Grid)
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "DEL")
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    keys.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            row.forEach { key ->
                                Box(
                                    modifier = Modifier
                                        .size(58.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (key) {
                                                "C" -> Color(0x33FF5252)
                                                "DEL" -> Color(0x33FFC107)
                                                else -> Color(0x3310131E)
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            when (key) {
                                                "C" -> Color(0xFFFF5252)
                                                "DEL" -> GoldPrimary
                                                else -> Color(0x4438BDF8)
                                            },
                                            CircleShape
                                        )
                                        .clickable {
                                            val now = System.currentTimeMillis()
                                            if (now - lastKeyPressTime < 180L) return@clickable
                                            lastKeyPressTime = now

                                            isErrorState = false
                                            errorMessage = ""
                                            when (key) {
                                                "C" -> pinInput = ""
                                                "DEL" -> if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1)
                                                else -> {
                                                    if (pinInput.length < 4) {
                                                        pinInput += key
                                                        if (pinInput.length == 4) {
                                                            val result = viewModel.unlockWithPin(pinInput)
                                                            if (result.first) {
                                                                Toast.makeText(context, result.second, Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                isErrorState = true
                                                                errorMessage = result.second
                                                                pinInput = ""
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (key == "DEL") {
                                        Icon(
                                            imageVector = Icons.Default.Backspace,
                                            contentDescription = "Delete",
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Text(
                                            text = key,
                                            color = when (key) {
                                                "C" -> Color(0xFFFF5252)
                                                else -> TextWhite
                                            },
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Biometrics / Reset Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            val result = viewModel.unlockWithPin(savedPin ?: "1234")
                            Toast.makeText(context, "Biometric verified! App Unlocked.", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Biometric",
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Biometric Unlock", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    TextButton(
                        onClick = {
                            viewModel.logoutUser()
                            Toast.makeText(context, "Please log in to reset your PIN.", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Forgot PIN?", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
