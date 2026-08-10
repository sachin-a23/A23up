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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MarketPredictionCardData

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val allPredictions by viewModel.allMarketPredictions.collectAsState()
    val availableMarkets by viewModel.availableMarkets.collectAsState()
    val selectedMarket by viewModel.selectedMarket.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 1: FAST MARKET CHUNAV (FAST MARKET SELECTOR BAR)
        item {
            FastMarketSelectorCard(
                availableMarkets = availableMarkets,
                selectedMarket = selectedMarket,
                onMarketSelected = { viewModel.selectMarket(it) }
            )
        }

        // Section 2: FORMULA CHUNAV & LIVE ♠️ OTC PREDICTION CARD
        item {
            HomeFormulaOtcSelectorCard(
                selectedMarket = selectedMarket,
                viewModel = viewModel
            )
        }

        // Section Title: All Markets OTC Prediction Cards
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "♠️ ALL MARKETS OTC PREDICTION CARDS",
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x33FFC107))
                        .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${allPredictions.size} MARKETS",
                        color = GoldPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Section 3: All Market OTC Cards
        items(allPredictions) { cardData ->
            OtcMarketCardItem(
                cardData = cardData,
                onCopy = { text ->
                    clipboardManager.setText(AnnotatedString(text))
                    Toast.makeText(context, "Copied ${cardData.marketName} Prediction Card!", Toast.LENGTH_SHORT).show()
                },
                onShare = { text ->
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_TEXT, text)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share ${cardData.marketName} OTC Card")
                    context.startActivity(shareIntent)
                }
            )
        }
    }
}

// 1. FAST MARKET CHUNAV COMPONENT
@Composable
fun FastMarketSelectorCard(
    availableMarkets: List<String>,
    selectedMarket: String,
    onMarketSelected: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
        border = BorderStroke(1.5.dp, GoldPrimary),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = "Market",
                        tint = GoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🏛️ FAST MARKET CHUNAV",
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x3300E5FF))
                        .border(1.dp, NeonCyan, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "ACTIVE: $selectedMarket",
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(availableMarkets) { mkt ->
                    val isSel = selectedMarket.equals(mkt, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (isSel) 1.5.dp else 1.dp,
                                color = if (isSel) GoldPrimary else Color(0x3338BDF8),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(if (isSel) GoldPrimary else Color(0xFF1E293B))
                            .clickable { onMarketSelected(mkt) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = mkt,
                            color = if (isSel) TextDark else TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

// 2. FORMULA CHUNAV & LIVE ♠️ OTC PREDICTION CARD
@Composable
fun HomeFormulaOtcSelectorCard(
    selectedMarket: String,
    viewModel: MainViewModel
) {
    var selectedFormulaId by remember { mutableIntStateOf(1) } // 1: OTC, 2: NEW-1, 3: Cut Total, 4: A23 Special, 5: Special 30
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val formulas = listOf(
        1 to "1 [OTC FORMULA]",
        2 to "2 [NEW-1 FORMULA]",
        3 to "3 [CUT TOTAL]",
        4 to "4 [A23 SPECIAL]",
        5 to "5 [SPECIAL 30]"
    )

    val (formulaTitle, formulaRule, otcDigitsList, jodis, panels) = when (selectedFormulaId) {
        1 -> Tuple5(
            "1 [OTC FORMULA - STANDARD DIVISION]",
            "Open Panel + Close Panel ÷ 3.6 Modulo Cut Total",
            listOf(2, 7, 4, 9),
            "27, 72, 49, 94",
            "147, 250, 368, 479"
        )
        2 -> Tuple5(
            "2 [NEW-1 FORMULA - ADVANCED PATTERN]",
            "Jodi Total × 2 + Last Week Open Panel Cut Pattern",
            listOf(1, 6, 3, 8),
            "16, 61, 38, 83",
            "137, 240, 358, 469"
        )
        3 -> Tuple5(
            "3 [CUT TOTAL FORMULA]",
            "Cut Digits Calculation with High Frequency Total Matrix",
            listOf(0, 5, 2, 7),
            "05, 50, 27, 72",
            "127, 230, 348, 569"
        )
        4 -> Tuple5(
            "4 [A23 SPECIAL OTC ENGINE]",
            "AI Neural Weight + Kalyan Multi-Market Trend Sync",
            listOf(3, 8, 5, 0),
            "38, 83, 50, 05",
            "157, 260, 389, 478"
        )
        else -> Tuple5(
            "5 [SPECIAL 30 FORMULA]",
            "Formula: ( 30 × Last Jodi ÷ 2 ) = Dynamic OTC Digits",
            listOf(1, 0, 6, 5),
            "10, 01, 65, 56",
            "128, 235, 348, 479"
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
        border = BorderStroke(1.5.dp, GoldPrimary),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Functions,
                        contentDescription = "Formulas",
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "⚡ FORMULA CHUNAV",
                        color = GoldPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x33FFC107))
                        .border(1.dp, GoldPrimary, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "♠️ A23 PRO",
                        color = GoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Formula Selector Chips Bar
            Text(
                text = "SELECT FORMULA FOR LIVE PREDICTION:",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(formulas) { (id, label) ->
                    val isSel = selectedFormulaId == id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (isSel) 1.5.dp else 1.dp,
                                color = if (isSel) GoldPrimary else Color(0x44FFC107),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(if (isSel) GoldPrimary else Color(0xFF1E293B))
                            .clickable { selectedFormulaId = id }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSel) TextDark else TextWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. OTC CARD ♠️ (DESIGNED BEAUTIFULLY)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                border = BorderStroke(1.5.dp, GoldPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Top Card Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("♠️", fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "$selectedMarket OTC PREDICTION",
                                    color = GoldPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = formulaTitle,
                                    color = NeonCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Copy / Share Buttons for this formula card
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    val txt = """
                                        ♠️ A23 PRO OTC PREDICTION ($selectedMarket)
                                        Formula: $formulaTitle
                                        Rule: $formulaRule
                                        ----------------------------
                                        🎯 OTC Digits: ${otcDigitsList.joinToString(" - ")}
                                        🔥 Jodi: $jodis
                                        🎰 Panel: $panels
                                    """.trimIndent()
                                    clipboardManager.setText(AnnotatedString(txt))
                                    Toast.makeText(context, "Copied $selectedMarket OTC Card!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x3300E676))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    val txt = """
                                        ♠️ A23 PRO OTC PREDICTION ($selectedMarket)
                                        Formula: $formulaTitle
                                        OTC Digits: ${otcDigitsList.joinToString(" - ")}
                                        Jodi: $jodis | Panel: $panels
                                    """.trimIndent()
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        putExtra(Intent.EXTRA_TEXT, txt)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share OTC Card"))
                                },
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x3338BDF8))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // OTC DIGITS BADGES GRID ♠️
                    Text(
                        text = "🎯 OTC DIGITS (${otcDigitsList.size} DIGITS):",
                        color = GoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        otcDigitsList.forEach { digit ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(1.5.dp, GoldPrimary, RoundedCornerShape(12.dp))
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$digit",
                                        color = GoldPrimary,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "Cut: ${(digit + 5) % 10}",
                                        color = TextMuted,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // JODI & PANEL PREDICTIONS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Jodi Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, NeonCyan, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("🔥 SUPER JODIS:", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(jodis, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Panel Box
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, GoldPrimary, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("🎰 PANEL PREDICTIONS:", color = GoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(panels, color = Color(0xFFFFD54F), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Rule Description
                    Text(
                        text = "Calculation Rule: $formulaRule",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E
)

// ALL MARKETS OTC CARD ITEM (♠️ BEAUTIFUL HIGH-TECH CARD)
@Composable
fun OtcMarketCardItem(
    cardData: MarketPredictionCardData,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit
) {
    val formattedShareText = """
        ♠️ A23 PRO OFFICIAL OTC PREDICTION
        🏛️ Market: ${cardData.marketName}
        📅 Live Date: ${cardData.liveDate}
        📌 Last Entry: ${cardData.lastEntryFullStr}
        ------------------------------
        🎯 OTC Digits: ${cardData.otcFormatted}
        🔥 Jodi: ${cardData.jodiFormatted}
        🎰 Panel: ${cardData.panelFormatted}
        ------------------------------
        👤 Analyst: ${cardData.analystName}
    """.trimIndent()

    val otcDigitsList = remember(cardData.otcFormatted) {
        cardData.otcFormatted.split("-", ",").mapNotNull { it.trim().toIntOrNull() }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
        border = BorderStroke(1.5.dp, GoldPrimary),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Card Top Header: Market Name & Spades Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("♠️", fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = cardData.marketName,
                        color = GoldPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x3300E5FF))
                        .border(1.dp, NeonCyan, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Live 📅: ${cardData.liveDate}",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Last Entry & Date Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x22141926))
                    .border(1.dp, Color(0x44FFC107), RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📌 Last Entry & Date:",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = cardData.lastEntryFullStr,
                        color = TextWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // OTC Digits Boxes ♠️
            Text(
                text = "🎯 OTC DIGITS:",
                color = GoldPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (otcDigitsList.isNotEmpty()) {
                    otcDigitsList.forEach { d ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, GoldPrimary, RoundedCornerShape(10.dp))
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$d",
                                color = GoldPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, GoldPrimary, RoundedCornerShape(10.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cardData.otcFormatted,
                            color = GoldPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // JODI - PANEL Grid Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x440F172A))
                    .border(1.dp, Color(0x66FFC107), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // JODI Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔥 JODI:",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = cardData.jodiFormatted,
                            color = NeonCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x2200E5FF)))

                    // PANEL Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎰 PANEL:",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = cardData.panelFormatted,
                            color = Color(0xFFFFD54F),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Card Bottom / Watermark Row ("Sachin Solunke" Watermark)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Watermark Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x33FFC107))
                        .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "♠️ ANALYST: ${cardData.analystName.uppercase()}",
                            color = GoldPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Copy & Share Action Icons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { onCopy(formattedShareText) },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x3300E676))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Card",
                            tint = NeonGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { onShare(formattedShareText) },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x3338BDF8))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Card",
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
