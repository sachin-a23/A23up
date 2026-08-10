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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.formula.BacktestEvaluation
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.MainViewModel
import com.example.util.PdfExporter
import java.io.File

@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val selectedMarket by viewModel.selectedMarket.collectAsState()
    val availableMarkets by viewModel.availableMarkets.collectAsState()

    val otcEvaluations by viewModel.backtestEvaluations.collectAsState()
    val weeklyStats by viewModel.weeklyStats.collectAsState()

    val new1Evaluations by viewModel.lockedBacktestEvaluations.collectAsState()
    val lockedWeeklyStats by viewModel.lockedWeeklyStats.collectAsState()

    val special30Evaluations by viewModel.special30BacktestEvaluations.collectAsState()
    val special30WeeklyStats by viewModel.special30WeeklyStats.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var showPdfDialog by remember { mutableStateOf(false) }

    // Formula Selection Tab: 0 -> 1 [OTC FORMULA], 1 -> 2 [NEW-1 FORMULA], 2 -> 3 [SPECIAL 30 FORMULA]
    var historyTab by remember { mutableIntStateOf(0) }

    val activeEvaluations = remember(historyTab, otcEvaluations, new1Evaluations, special30Evaluations) {
        when (historyTab) {
            1 -> new1Evaluations
            2 -> special30Evaluations
            else -> otcEvaluations
        }
    }

    val activeStats = remember(historyTab, weeklyStats, lockedWeeklyStats, special30WeeklyStats) {
        when (historyTab) {
            1 -> lockedWeeklyStats
            2 -> special30WeeklyStats
            else -> weeklyStats
        }
    }

    val activeFormulaName = remember(historyTab) {
        when (historyTab) {
            1 -> "2 [NEW-1 FORMULA]"
            2 -> "3 [SPECIAL 30 FORMULA]"
            else -> "1 [OTC FORMULA]"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // TOP HEADER CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.5.dp, GoldPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "HISTORY & BACKTEST MANAGER",
                                color = GoldPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$selectedMarket • $activeFormulaName • ${activeEvaluations.size} Records Evaluated",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, GoldPrimary, RoundedCornerShape(12.dp))
                            .background(Color(0x33FFC107))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${activeEvaluations.size} DAYS",
                            color = GoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 1. MARKET CHUNAV (SELECT MARKET)
        item {
            Column {
                Text(
                    text = "1. MARKET CHUNAV (SELECT MARKET)",
                    color = GoldPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableMarkets) { market ->
                        val isSelected = market.equals(selectedMarket, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    width = 1.2.dp,
                                    color = if (isSelected) GoldPrimary else Color(0x4438BDF8),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .background(if (isSelected) GoldPrimary else Color(0xFF1E293B))
                                .clickable { viewModel.selectMarket(market) }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = market,
                                color = if (isSelected) TextDark else TextWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. FORMULA CHUNAV (SELECT FORMULA)
        item {
            Column {
                Text(
                    text = "2. FORMULA CHUNAV (SELECT FORMULA)",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF10131E))
                        .border(1.dp, NeonCyan, RoundedCornerShape(14.dp))
                        .padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            0 to "1 [OTC]",
                            1 to "2 [NEW-1]",
                            2 to "3 [SPECIAL 30]"
                        ).forEach { (idx, label) ->
                            val isSel = historyTab == idx
                            val tabBg = when (idx) {
                                1 -> if (isSel) NeonCyan else Color(0x2210131E)
                                2 -> if (isSel) NeonGreen else Color(0x2210131E)
                                else -> if (isSel) GoldPrimary else Color(0x2210131E)
                            }
                            val tabBorder = when (idx) {
                                1 -> NeonCyan
                                2 -> NeonGreen
                                else -> GoldPrimary
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, if (isSel) tabBorder else Color(0x33FFFFFF), RoundedCornerShape(10.dp))
                                    .background(tabBg)
                                    .clickable { historyTab = idx }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
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
                }
            }
        }

        // 3. COLORFUL PDF REPORT BAR
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.dp, NeonCyan),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "COLORFUL PDF REPORT",
                                color = NeonCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Market: $selectedMarket ($activeFormulaName)",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                val pdf = PdfExporter.generatePdfReport(
                                    context = context,
                                    marketName = selectedMarket,
                                    stats = activeStats,
                                    evaluations = activeEvaluations,
                                    formulaName = activeFormulaName
                                )
                                if (pdf != null) {
                                    generatedPdfFile = pdf
                                    showPdfDialog = true
                                    Toast.makeText(context, "✓ PDF Saved: ${pdf.name}", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.height(36.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "Download PDF",
                                color = TextDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (generatedPdfFile != null) {
                            Button(
                                onClick = {
                                    generatedPdfFile?.let { file ->
                                        PdfExporter.sharePdfFile(context, file)
                                    }
                                },
                                modifier = Modifier.height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = TextDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. PASS / FAIL DAYS REPORT STATS CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.dp, Color(0x66FFC107)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header Row inside Card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PASS / FAIL DAYS REPORT",
                                color = GoldPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Accuracy % Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, NeonPink, RoundedCornerShape(12.dp))
                                .background(Color(0x33FF1744))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ACCURACY: ${String.format("%.1f", activeStats.accuracyPercentage)}%",
                                color = NeonPink,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3 Metric Stat Boxes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Pass Days Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, NeonGreen, RoundedCornerShape(10.dp))
                                .background(Color(0x2200C853))
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "PASS DAYS",
                                    color = NeonGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${activeStats.passDays} Days",
                                    color = TextWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Fail Days Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, NeonPink, RoundedCornerShape(10.dp))
                                .background(Color(0x22FF1744))
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "FAIL DAYS",
                                    color = NeonPink,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${activeStats.failDays} Days",
                                    color = TextWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Total Eval Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0x66FFC107), RoundedCornerShape(10.dp))
                                .background(Color(0x3310131E))
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "TOTAL EVAL",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${activeStats.totalEvaluated} Days",
                                    color = TextWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Bar Line
                    val passRatio = if (activeStats.totalEvaluated > 0) {
                        activeStats.passDays.toFloat() / activeStats.totalEvaluated
                    } else 0.5f

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(NeonPink)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(passRatio)
                                .height(6.dp)
                                .background(NeonGreen)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Weekly Breakdown Header
                    Text(
                        text = "Weekly Day-by-Day Report (Somvaar → Ravivaar):",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Days Scroll Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(activeStats.dailyBreakdown.toList()) { (dayLabel, counts) ->
                            val passes = counts.first
                            val total = counts.second
                            val isPassDay = passes > 0
                            val borderColor = if (isPassDay) NeonGreen else NeonPink
                            val statusText = if (isPassDay) "PASS ($passes/$total)" else "FAIL ($passes/$total)"

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                                    .background(Color(0x2210131E))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayLabel,
                                        color = TextWhite,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = statusText,
                                        color = borderColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. SHOW ALL DAYS REPORT (SEARCH & FILTER CHIPS)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "5. SHOW ALL DAYS REPORT (${activeEvaluations.size})",
                        color = TextWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    // Filter Chips Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("ALL", "PASS", "FAIL").forEach { filter ->
                            val isSelected = statusFilter == filter
                            val bgColor = if (isSelected) GoldPrimary else Color(0xFF1E293B)
                            val textColor = if (isSelected) TextDark else TextWhite

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, if (isSelected) GoldPrimary else Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                                    .background(bgColor)
                                    .clickable { viewModel.setStatusFilter(filter) }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = filter,
                                    color = textColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = "Search date, jodi, panel or day...",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = Color(0x66FFC107),
                        focusedContainerColor = Color(0x33161A26),
                        unfocusedContainerColor = Color(0x33161A26),
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    singleLine = true
                )
            }
        }

        // EVALUATION CARDS LIST
        if (activeEvaluations.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No history records found for $selectedMarket ($activeFormulaName).",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(activeEvaluations) { eval ->
                EvaluationHistoryCard(
                    eval = eval,
                    onCopy = {
                        val details = """
                            👑 A23 PRO - $selectedMarket
                            Formula: $activeFormulaName
                            Date: ${eval.record.date} (${eval.dayOfWeekHindi})
                            Declaration: ${eval.record.openPanel.ifBlank { "***" }} - ${eval.record.jodi} - ${eval.record.closePanel.ifBlank { "***" }}
                            OTC Digits: ${eval.formulaResult?.otcFormatted ?: "N/A"}
                            Super Jodi: ${eval.formulaResult?.superJodis ?: "N/A"}
                            Status: ${if (eval.isPass) "PASS ✅" else "FAIL ❌"}
                        """.trimIndent()
                        clipboardManager.setText(AnnotatedString(details))
                        Toast.makeText(context, "Copied Record to Clipboard!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    // PDF OPEN/SHARE DIALOG
    if (showPdfDialog && generatedPdfFile != null) {
        AlertDialog(
            onDismissRequest = { showPdfDialog = false },
            containerColor = CyberCardBg,
            title = {
                Text(
                    text = "PDF Generated Successfully",
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "File: ${generatedPdfFile?.name}",
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Saved in Downloads/A23_PRO_Reports directory.",
                        color = NeonCyan,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        generatedPdfFile?.let { file ->
                            PdfExporter.openPdfFile(context, file)
                        }
                        showPdfDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Open PDF", color = TextDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        generatedPdfFile?.let { file ->
                            PdfExporter.sharePdfFile(context, file)
                        }
                        showPdfDialog = false
                    }
                ) {
                    Text("Share PDF", color = NeonCyan)
                }
            }
        )
    }
}

