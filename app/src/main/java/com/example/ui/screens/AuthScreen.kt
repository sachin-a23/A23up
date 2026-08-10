package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.GTranslate
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.MainViewModel

enum class AuthMode {
    EMAIL,
    PHONE_OTP,
    GOOGLE_ONE_TAP
}

@Composable
fun AuthScreen(viewModel: MainViewModel) {
    val context = LocalContext.current

    // State from ViewModel
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userPhone by viewModel.userPhone.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val isPinEnabled by viewModel.isPinEnabled.collectAsState()
    val savedPin by viewModel.savedPin.collectAsState()

    // Auth Screen Modes: Login vs Register
    var isLoginTab by remember { mutableStateOf(true) }
    var selectedAuthMode by remember { mutableStateOf(AuthMode.EMAIL) }

    // Dialog States
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showGoogleAccountPicker by remember { mutableStateOf(false) }

    // Email/Pass Inputs
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var showLoginPassword by remember { mutableStateOf(false) }

    // Phone / OTP Inputs
    var phoneInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableStateOf(60) }

    // Registration Inputs
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var showRegPassword by remember { mutableStateOf(false) }
    var regAccountType by remember { mutableStateOf("VIP Trader") }

    // Timer effect for OTP
    LaunchedEffect(isOtpSent, timerSeconds) {
        if (isOtpSent && timerSeconds > 0) {
            kotlinx.coroutines.delay(1000L)
            timerSeconds--
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP HEADER BANNER
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.5.dp, GoldPrimary),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(GoldPrimary.copy(alpha = 0.4f), Color.Transparent)
                                )
                            )
                            .border(2.dp, GoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLoggedIn) Icons.Default.Shield else Icons.Default.Lock,
                            contentDescription = "Security Auth",
                            tint = GoldPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isLoggedIn) "USER PROFILE & SECURITY" else "A23 PRO AUTHENTICATION",
                        color = GoldPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isLoggedIn) "Verified Active Session & PIN Lock Settings" else "Firebase Email, Phone OTP & Google One-Tap Sign-In",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (isLoggedIn) {
            // ==========================================
            // USER PROFILE SCREEN (WHEN LOGGED IN)
            // ==========================================
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                    border = BorderStroke(1.5.dp, NeonGreen),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // User Profile Avatar
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, NeonGreen, CircleShape)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_sachin_admin_1785594615532),
                                    contentDescription = "User Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = userName,
                                        color = TextWhite,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = NeonGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = userEmail,
                                    color = NeonCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Text(
                                    text = userPhone,
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(GoldPrimary.copy(alpha = 0.2f))
                                        .border(1.dp, GoldPrimary, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "👑 $userRole",
                                        color = GoldPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            IconButton(onClick = { showEditProfileDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = GoldPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Security Status Badge
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0x2200E676))
                                .border(1.dp, NeonGreen, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Security Status",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Firebase Auth: Verified & Encrypted",
                                        color = NeonGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Full prediction models, historical market data & live formula tools unlocked.",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4-DIGIT PIN SECURITY SETTINGS CARD
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                    border = BorderStroke(1.5.dp, GoldPrimary),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "PIN Lock",
                                tint = GoldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "4-Digit PIN Lock Security",
                                    color = GoldPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isPinEnabled) "App requires 4-digit PIN on launch" else "PIN lock disabled",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }

                            Switch(
                                checked = isPinEnabled,
                                onCheckedChange = { enabled ->
                                    viewModel.togglePinEnabled(enabled)
                                    Toast.makeText(
                                        context,
                                        if (enabled) "PIN lock enabled!" else "PIN lock disabled!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = GoldPrimary,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = Color(0x3310131E)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Change PIN Button
                            Button(
                                onClick = { showChangePinDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFC107)),
                                border = BorderStroke(1.dp, GoldPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Change PIN",
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "CHANGE PIN",
                                        color = GoldPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Lock App Now Button
                            Button(
                                onClick = {
                                    viewModel.lockApp()
                                    Toast.makeText(context, "App locked with PIN", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x3300E5FF)),
                                border = BorderStroke(1.dp, NeonCyan),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = "Lock Now",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "LOCK APP NOW",
                                        color = NeonCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // LOGOUT BUTTON
            item {
                Button(
                    onClick = {
                        viewModel.logoutUser()
                        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF5252)),
                    border = BorderStroke(1.5.dp, Color(0xFFFF5252)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LOGOUT ACCOUNT",
                            color = Color(0xFFFF5252),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // ==========================================
            // LOGIN & REGISTER SCREEN (3 SIGN-IN METHODS)
            // ==========================================

            // 1. LOGIN vs REGISTER TAB SWITCHER
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x99161A26))
                        .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(4.dp)
                ) {
                    val loginBg by animateColorAsState(if (isLoginTab) GoldPrimary else Color.Transparent, label = "loginBg")
                    val loginText by animateColorAsState(if (isLoginTab) Color.Black else TextWhite, label = "loginText")

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(loginBg)
                            .clickable { isLoginTab = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LockOpen, contentDescription = "Login", tint = loginText, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LOGIN", color = loginText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    val regBg by animateColorAsState(if (!isLoginTab) NeonCyan else Color.Transparent, label = "regBg")
                    val regText by animateColorAsState(if (!isLoginTab) Color.Black else TextWhite, label = "regText")

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(regBg)
                            .clickable { isLoginTab = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = "Register", tint = regText, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("REGISTER", color = regText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (isLoginTab) {
                // 2. SIGN-IN METHOD SELECTOR (Email | Phone OTP | Google One-Tap)
                item {
                    Text(
                        text = "SELECT SIGN-IN METHOD:",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Email Method Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedAuthMode == AuthMode.EMAIL) GoldPrimary.copy(alpha = 0.25f) else Color(0x2210131E))
                                .border(1.dp, if (selectedAuthMode == AuthMode.EMAIL) GoldPrimary else Color(0x44FFFFFF), RoundedCornerShape(12.dp))
                                .clickable { selectedAuthMode = AuthMode.EMAIL }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.Email, contentDescription = "Email Auth", tint = if (selectedAuthMode == AuthMode.EMAIL) GoldPrimary else TextMuted, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Email/Pass", color = if (selectedAuthMode == AuthMode.EMAIL) GoldPrimary else TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Phone OTP Method Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedAuthMode == AuthMode.PHONE_OTP) NeonCyan.copy(alpha = 0.25f) else Color(0x2210131E))
                                .border(1.dp, if (selectedAuthMode == AuthMode.PHONE_OTP) NeonCyan else Color(0x44FFFFFF), RoundedCornerShape(12.dp))
                                .clickable { selectedAuthMode = AuthMode.PHONE_OTP }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.Phone, contentDescription = "Phone OTP Auth", tint = if (selectedAuthMode == AuthMode.PHONE_OTP) NeonCyan else TextMuted, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Phone OTP", color = if (selectedAuthMode == AuthMode.PHONE_OTP) NeonCyan else TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Google One-Tap Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedAuthMode == AuthMode.GOOGLE_ONE_TAP) NeonGreen.copy(alpha = 0.25f) else Color(0x2210131E))
                                .border(1.dp, if (selectedAuthMode == AuthMode.GOOGLE_ONE_TAP) NeonGreen else Color(0x44FFFFFF), RoundedCornerShape(12.dp))
                                .clickable { selectedAuthMode = AuthMode.GOOGLE_ONE_TAP }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.GTranslate, contentDescription = "Google Auth", tint = if (selectedAuthMode == AuthMode.GOOGLE_ONE_TAP) NeonGreen else TextMuted, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Google", color = if (selectedAuthMode == AuthMode.GOOGLE_ONE_TAP) NeonGreen else TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 3. FORM FOR SELECTED AUTH METHOD
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                        border = BorderStroke(1.dp, when(selectedAuthMode) {
                            AuthMode.EMAIL -> GoldPrimary
                            AuthMode.PHONE_OTP -> NeonCyan
                            AuthMode.GOOGLE_ONE_TAP -> NeonGreen
                        }),
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            when (selectedAuthMode) {
                                // ------------------------------------
                                // METHOD A: EMAIL & PASSWORD (FIREBASE)
                                // ------------------------------------
                                AuthMode.EMAIL -> {
                                    Text("EMAIL & PASSWORD LOGIN", color = GoldPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    Text("Firebase Authentication with secure password hashing", color = TextMuted, fontSize = 11.sp)

                                    Spacer(modifier = Modifier.height(16.dp))

                                    OutlinedTextField(
                                        value = loginEmail,
                                        onValueChange = { loginEmail = it },
                                        label = { Text("Email Address", color = TextMuted) },
                                        leadingIcon = { Icon(Icons.Default.Email, "Email", tint = GoldPrimary) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GoldPrimary,
                                            unfocusedBorderColor = Color(0x66FFC107),
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = loginPassword,
                                        onValueChange = { loginPassword = it },
                                        label = { Text("Password", color = TextMuted) },
                                        leadingIcon = { Icon(Icons.Default.Lock, "Password", tint = GoldPrimary) },
                                        trailingIcon = {
                                            IconButton(onClick = { showLoginPassword = !showLoginPassword }) {
                                                Icon(if (showLoginPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff, "Toggle", tint = TextMuted)
                                            }
                                        },
                                        singleLine = true,
                                        visualTransformation = if (showLoginPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GoldPrimary,
                                            unfocusedBorderColor = Color(0x66FFC107),
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(18.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Brush.horizontalGradient(listOf(GoldPrimary, Color(0xFFFF9100))))
                                            .clickable {
                                                val res = viewModel.loginUser(loginEmail, loginPassword)
                                                Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("LOGIN WITH EMAIL", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }

                                // ------------------------------------
                                // METHOD B: PHONE NUMBER & OTP
                                // ------------------------------------
                                AuthMode.PHONE_OTP -> {
                                    Text("PHONE NUMBER & OTP SIGN-IN", color = NeonCyan, fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    Text("Firebase SMS verification token auth", color = TextMuted, fontSize = 11.sp)

                                    Spacer(modifier = Modifier.height(16.dp))

                                    OutlinedTextField(
                                        value = phoneInput,
                                        onValueChange = { phoneInput = it },
                                        label = { Text("10-Digit Mobile Number (+91)", color = TextMuted) },
                                        leadingIcon = { Icon(Icons.Default.Phone, "Phone", tint = NeonCyan) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NeonCyan,
                                            unfocusedBorderColor = Color(0x6600E5FF),
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    if (!isOtpSent) {
                                        Button(
                                            onClick = {
                                                if (phoneInput.length >= 10) {
                                                    isOtpSent = true
                                                    otpInput = "123456" // Auto fill sample OTP for convenience
                                                    timerSeconds = 60
                                                    Toast.makeText(context, "OTP sent to +91 $phoneInput via SMS (Demo: 123456)", Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(context, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.fillMaxWidth().height(48.dp)
                                        ) {
                                            Text("SEND OTP SMS", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                    } else {
                                        OutlinedTextField(
                                            value = otpInput,
                                            onValueChange = { if (it.length <= 6) otpInput = it },
                                            label = { Text("6-Digit OTP Code", color = TextMuted) },
                                            leadingIcon = { Icon(Icons.Default.Lock, "OTP", tint = NeonCyan) },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = NeonCyan,
                                                unfocusedBorderColor = Color(0x6600E5FF),
                                                focusedTextColor = TextWhite,
                                                unfocusedTextColor = TextWhite
                                            ),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (timerSeconds > 0) "Resend OTP in ${timerSeconds}s" else "Resend OTP Available",
                                                color = TextMuted,
                                                fontSize = 11.sp
                                            )

                                            if (timerSeconds == 0) {
                                                Text(
                                                    text = "Resend SMS",
                                                    color = NeonCyan,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.clickable {
                                                        timerSeconds = 60
                                                        Toast.makeText(context, "New OTP code sent!", Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Button(
                                            onClick = {
                                                val res = viewModel.loginWithPhoneOtp(phoneInput, otpInput)
                                                Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.fillMaxWidth().height(48.dp)
                                        ) {
                                            Text("VERIFY OTP & SIGN IN", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }
                                }

                                // ------------------------------------
                                // METHOD C: GOOGLE ONE-TAP SIGN IN
                                // ------------------------------------
                                AuthMode.GOOGLE_ONE_TAP -> {
                                    Text("GOOGLE ONE-TAP SIGN-IN", color = NeonGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    Text("Google Identity Services / Firebase OAuth Integration", color = TextMuted, fontSize = 11.sp)

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0x2200E676))
                                            .border(1.dp, NeonGreen, RoundedCornerShape(14.dp))
                                            .clickable { showGoogleAccountPicker = true }
                                            .padding(16.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.GTranslate, contentDescription = "Google", tint = NeonGreen, modifier = Modifier.size(28.dp))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text("Sign in with Google", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                                Text("One-Tap Google account verification", color = TextMuted, fontSize = 11.sp)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Google Account: woldcom87@gmail.com (Sachin Solunke)",
                                        color = GoldPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. QUICK DEMO ONE-TAP LOGIN SHORTCUTS
                item {
                    Text("⚡ QUICK DEMO LOGINS:", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x22FFC107))
                            .border(1.dp, GoldPrimary, RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.loginWithGoogle("woldcom87@gmail.com", "Sachin Solunke")
                                Toast.makeText(context, "Logged in as Admin: Sachin Solunke", Toast.LENGTH_SHORT).show()
                            }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👑", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Lead Admin Access (Sachin Solunke)", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("woldcom87@gmail.com • Instant Auth", color = TextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }
            } else {
                // ==========================================
                // REGISTER FORM
                // ==========================================
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                        border = BorderStroke(1.dp, NeonCyan),
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("CREATE A STYLISH ACCOUNT", color = NeonCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text("Join A23 PRO community for verified predictions", color = TextMuted, fontSize = 11.sp)

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = regName,
                                onValueChange = { regName = it },
                                label = { Text("Full Name", color = TextMuted) },
                                leadingIcon = { Icon(Icons.Default.Person, "Name", tint = NeonCyan) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = Color(0x6600E5FF), focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = { regEmail = it },
                                label = { Text("Email Address", color = TextMuted) },
                                leadingIcon = { Icon(Icons.Default.Email, "Email", tint = NeonCyan) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = Color(0x6600E5FF), focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = regPhone,
                                onValueChange = { regPhone = it },
                                label = { Text("Mobile Number", color = TextMuted) },
                                leadingIcon = { Icon(Icons.Default.Phone, "Phone", tint = NeonCyan) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = Color(0x6600E5FF), focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = { regPassword = it },
                                label = { Text("Create Password", color = TextMuted) },
                                leadingIcon = { Icon(Icons.Default.Lock, "Password", tint = NeonCyan) },
                                trailingIcon = {
                                    IconButton(onClick = { showRegPassword = !showRegPassword }) {
                                        Icon(if (showRegPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff, "Toggle", tint = TextMuted)
                                    }
                                },
                                singleLine = true,
                                visualTransformation = if (showRegPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = Color(0x6600E5FF), focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text("Select Membership Role:", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                listOf("VIP Trader", "Market Analyst", "Regular Member").forEach { role ->
                                    val isSelected = regAccountType == role
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) NeonCyan.copy(alpha = 0.3f) else Color(0x2210131E))
                                            .border(1.dp, if (isSelected) NeonCyan else Color(0x44FFFFFF), RoundedCornerShape(10.dp))
                                            .clickable { regAccountType = role }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(role, color = if (isSelected) NeonCyan else TextWhite, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Brush.horizontalGradient(listOf(NeonCyan, Color(0xFF00B0FF))))
                                    .clickable {
                                        val res = viewModel.registerUser(regName, regEmail, regPhone, regPassword, regAccountType)
                                        Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("REGISTER ACCOUNT", color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // EDIT PROFILE DIALOG
    // ==========================================
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(userName) }
        var editEmail by remember { mutableStateOf(userEmail) }
        var editPhone by remember { mutableStateOf(userPhone) }
        var editRole by remember { mutableStateOf(userRole) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            containerColor = CyberCardBg,
            title = {
                Text("Edit User Profile", color = GoldPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0x66FFC107), focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0x66FFC107), focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0x66FFC107), focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    OutlinedTextField(
                        value = editRole,
                        onValueChange = { editRole = it },
                        label = { Text("Membership Role", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0x66FFC107), focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val res = viewModel.updateUserProfile(editName, editEmail, editPhone, editRole, 0)
                        Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // ==========================================
    // CHANGE PIN DIALOG
    // ==========================================
    if (showChangePinDialog) {
        var oldPinInput by remember { mutableStateOf("") }
        var newPinInput by remember { mutableStateOf("") }
        var confirmPinInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            containerColor = CyberCardBg,
            title = {
                Text("Change 4-Digit Security PIN", color = GoldPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = oldPinInput,
                        onValueChange = { if (it.length <= 4) oldPinInput = it },
                        label = { Text("Current 4-Digit PIN", color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0x66FFC107), focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { if (it.length <= 4) newPinInput = it },
                        label = { Text("New 4-Digit PIN", color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0x66FFC107), focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    OutlinedTextField(
                        value = confirmPinInput,
                        onValueChange = { if (it.length <= 4) confirmPinInput = it },
                        label = { Text("Confirm New 4-Digit PIN", color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0x66FFC107), focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinInput != confirmPinInput) {
                            Toast.makeText(context, "New PIN and Confirm PIN do not match!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val res = viewModel.changeSecurityPin(oldPinInput, newPinInput)
                        Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                        if (res.first) showChangePinDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Update PIN", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // ==========================================
    // GOOGLE ACCOUNT PICKER DIALOG
    // ==========================================
    if (showGoogleAccountPicker) {
        AlertDialog(
            onDismissRequest = { showGoogleAccountPicker = false },
            containerColor = CyberCardBg,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.GTranslate, contentDescription = "Google", tint = NeonGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose Google Account", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Account 1
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x2210131E))
                            .border(1.dp, NeonGreen, RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.loginWithGoogle("woldcom87@gmail.com", "Sachin Solunke")
                                showGoogleAccountPicker = false
                                Toast.makeText(context, "Signed in as Sachin Solunke (woldcom87@gmail.com)", Toast.LENGTH_SHORT).show()
                            }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(NeonGreen.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("S", color = NeonGreen, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Sachin Solunke", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("woldcom87@gmail.com", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    }

                    // Account 2
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x2210131E))
                            .border(1.dp, GoldPrimary, RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.loginWithGoogle("sachin.pro@gmail.com", "Sachin Pro Trader")
                                showGoogleAccountPicker = false
                                Toast.makeText(context, "Signed in as Sachin Pro Trader", Toast.LENGTH_SHORT).show()
                            }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(GoldPrimary.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("P", color = GoldPrimary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Sachin Pro Trader", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("sachin.pro@gmail.com", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showGoogleAccountPicker = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}
