package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import java.io.File

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

@Composable
fun CyberMenuDrawerSheet(
    isOpen: Boolean,
    onClose: () -> Unit,
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    selectedMarket: String,
    availableMarkets: List<String>,
    onMarketSelect: (String) -> Unit,
    userName: String = "Sachin Solunke",
    userRole: String = "Admin / Verified Developer"
) {
    if (!isOpen) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable { onClose() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(310.dp)
                .align(Alignment.CenterStart)
                .background(Color(0xFF0F131E))
                .border(width = 1.dp, color = GoldPrimary, shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
                .clickable(enabled = false) { }
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Bar with Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // A23 PRO Brand Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.5.dp, GoldPrimary, RoundedCornerShape(16.dp))
                        .background(Color(0xFF161A26))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "A23 PRO MENU",
                        color = GoldPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Close Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0x66FFC107), CircleShape)
                        .background(Color(0x22161A26))
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Menu",
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Admin Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFF161A26)),
                border = BorderStroke(1.dp, Color(0x4400E5FF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, GoldPrimary, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_sachin_admin_1785594615532),
                            contentDescription = "Admin Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = userName,
                            color = GoldPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = userRole,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Market: $selectedMarket",
                                color = Color(0xFF00E676),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "NAVIGATION MODULES",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Items
            val navItems = listOf(
                NavTab.HOME to Pair("Home Dashboard", "Main Control Center & All Formulas"),
                NavTab.FORMULA to Pair("Formula Engine", "OTC & Formula Calculations"),
                NavTab.DATA_MARKET to Pair("Data & Market", "Weekly Charts & Records"),
                NavTab.AI_PREDICT to Pair("AI Prediction Studio", "Gemini, ChatGPT, Claude & OpenCode Zen"),
                NavTab.HISTORY to Pair("History Logs", "Saved Backtests"),
                NavTab.SETTINGS to Pair("Settings & AI Setup", "Multi-Provider AI, Wallpapers & PDF"),
                NavTab.PROFILE to Pair("Profile & Account", "GitHub Sync & Settings")
            )

            navItems.forEach { (tab, details) ->
                val (title, sub) = details
                val isSelected = currentTab == tab
                val bg = if (isSelected) GoldPrimary.copy(alpha = 0.18f) else Color(0x33161A26)
                val borderCol = if (isSelected) GoldPrimary else Color(0x22FFC107)
                val iconTint = if (isSelected) GoldPrimary else TextWhite

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bg)
                        .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                        .clickable {
                            onTabSelected(tab)
                            onClose()
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = title,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                color = if (isSelected) GoldPrimary else TextWhite,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = sub,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "SELECT ACTIVE MARKET",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Market selector chips inside menu
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(availableMarkets) { market ->
                    val isSel = market.equals(selectedMarket, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = 1.dp,
                                color = if (isSel) GoldPrimary else Color(0x44FFC107),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(if (isSel) GoldPrimary else Color(0x33161A26))
                            .clickable {
                                onMarketSelect(market)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = market,
                            color = if (isSel) Color.Black else TextWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Text(
                text = "A23 PRO CYBER SUITE v3.5\nDesigned for Kalyan & Indian Markets",
                color = TextMuted,
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun CyberBackgroundWrapper(
    wallpaperPath: String? = null,
    dimLevel: Float = 40f,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberDarkBg)
    ) {
        val customFile = wallpaperPath?.takeIf { !it.startsWith("res:") }?.let { File(it) }
        when {
            wallpaperPath != null && wallpaperPath.startsWith("res:") -> {
                val resName = wallpaperPath.removePrefix("res:")
                val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                if (resId != 0) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = "App Built-in Wallpaper",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.img_circuit_bg_1785562607299),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            customFile != null && customFile.exists() -> {
                AsyncImage(
                    model = customFile,
                    contentDescription = "Custom Background Wallpaper",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Image(
                    painter = painterResource(id = R.drawable.img_circuit_bg_1785562607299),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Soft Dark Dim Overlay (0% to 100% dimming)
        val dimAlpha = (dimLevel / 100f).coerceIn(0f, 0.95f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF141926).copy(alpha = dimAlpha))
        )

        content()
    }
}

@Composable
fun TopGsmHeader(
    selectedMarket: String,
    availableMarkets: List<String>,
    showMarketsBar: Boolean = true,
    showBackButton: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    onMarketSelect: (String) -> Unit,
    onMenuClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Top Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Drawer Icon Button or Back Button
            if (showBackButton) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.5.dp, GoldPrimary, RoundedCornerShape(20.dp))
                        .background(Color(0xCC10131E))
                        .clickable { onBackClick?.invoke() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Back",
                            color = GoldPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, GoldPrimary, CircleShape)
                        .background(Color(0x88161A26))
                        .clickable { onMenuClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = GoldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Center A23 PRO Logo Capsule
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, GoldPrimary, RoundedCornerShape(24.dp))
                    .background(Color(0xCC10131E))
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A23 PRO",
                    color = GoldPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Right Action Icons Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Profile Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color(0xFF00E5FF), CircleShape)
                        .background(Color(0x88161A26))
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        if (showMarketsBar) {
            Spacer(modifier = Modifier.height(10.dp))

            // Markets Header Subtitle
            Text(
                text = "Markets & Predictions",
                color = TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Market Selector Pill Bar
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                items(availableMarkets) { market ->
                    val isSelected = market.equals(selectedMarket, ignoreCase = true)
                    val bgColor by animateColorAsState(
                        if (isSelected) GoldPrimary else Color(0x99161A26),
                        label = "bgColor"
                    )
                    val textColor by animateColorAsState(
                        if (isSelected) Color.Black else TextWhite,
                        label = "textColor"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .border(
                                width = 1.2.dp,
                                color = if (isSelected) GoldPrimary else Color(0x66FFC107),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .background(bgColor)
                            .clickable { onMarketSelect(market) }
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = market,
                            color = textColor,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

enum class NavTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    FORMULA("Formula", Icons.Default.ShowChart),
    DATA_MARKET("Data", Icons.Default.TableChart),
    AI_PREDICT("AI", Icons.Default.AutoAwesome),
    HISTORY("History", Icons.Default.History),
    SETTINGS("Settings", Icons.Default.Settings),
    PROFILE("Profile", Icons.Default.Person)
}

@Composable
fun CyberBottomBar(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit
) {
    val bottomTabs = listOf(
        NavTab.HOME,
        NavTab.FORMULA,
        NavTab.DATA_MARKET,
        NavTab.AI_PREDICT,
        NavTab.HISTORY,
        NavTab.SETTINGS
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            .background(Color(0x990A0E18)) // Transparent Glassmorphism dark tint
            .border(
                width = 1.dp,
                color = Color(0x33FFC107), // Subtle gold accent border
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
            )
            .padding(top = 8.dp, bottom = 8.dp, start = 4.dp, end = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomTabs.forEach { tab ->
                val isSelected = currentTab == tab
                val color = if (isSelected) GoldPrimary else TextMuted

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) GoldPrimary.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = tab.title,
                        color = color,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
