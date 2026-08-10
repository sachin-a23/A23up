package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
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
fun ReportScreen(viewModel: MainViewModel) {
    val selectedMarket by viewModel.selectedMarket.collectAsState()
    val availableMarkets by viewModel.availableMarkets.collectAsState()
    val evaluations by viewModel.backtestEvaluations.collectAsState()
    val weeklyStats by viewModel.weeklyStats.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Market Option Choice Selector Bar
        item {
            Column {
                Text(
                    text = "CHOICE MARKET:",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableMarkets) { market ->
                        val isSelected = market.equals(selectedMarket, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .border(
                                    width = 1.2.dp,
                                    color = if (isSelected) GoldPrimary else Color(0x66FFC107),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .background(if (isSelected) GoldPrimary else Color(0x3310131E))
                                .clickable { viewModel.selectMarket(market) }
                                .padding(horizontal = 16.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = market,
                                color = if (isSelected) TextDark else TextWhite,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "HISTORICAL ANALYTICS & LOGS",
                            color = GoldPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "$selectedMarket • Strict Pattern Match Engine",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, GoldPrimary, RoundedCornerShape(16.dp))
                        .background(Color(0x66161A26))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${evaluations.size} RECS",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Weekly Accuracy Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FFC107)),
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
                                text = "WEEKLY ACCURACY (SOMVAAR-RAVIVAAR)",
                                color = GoldPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Accuracy % Box
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, NeonPink, RoundedCornerShape(12.dp))
                                .background(Color(0x33FF1744))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ACCURACY: ${String.format("%.1f", weeklyStats.accuracyPercentage)}%",
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
                                    text = ".PASS DAYS",
                                    color = NeonGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${weeklyStats.passDays} Days",
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
                                    text = ".FAIL DAYS",
                                    color = NeonPink,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${weeklyStats.failDays} Days",
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
                                    text = "${weeklyStats.totalEvaluated} Days",
                                    color = TextWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Bar Line
                    val passRatio = if (weeklyStats.totalEvaluated > 0) {
                        weeklyStats.passDays.toFloat() / weeklyStats.totalEvaluated
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
                        text = "Weekly Breakdown (Somvaar → Ravivaar):",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Days Scroll Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(weeklyStats.dailyBreakdown.toList()) { (dayLabel, counts) ->
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

        // Compact Direct PDF Export & Download Bar
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
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
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "EXPORT REPORT PDF",
                                color = NeonCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Saved in Downloads/GSM_PRO_Reports",
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
                                    stats = weeklyStats,
                                    evaluations = evaluations
                                )
                                if (pdf != null) {
                                    generatedPdfFile = pdf
                                    Toast.makeText(context, "✓ PDF Saved in Downloads/GSM_PRO_Reports:\n${pdf.name}", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.height(36.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(8.dp),
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
                                shape = RoundedCornerShape(8.dp),
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

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text(
                        text = "Search by date, result or day (e.g. Somvaar)...",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = GoldPrimary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
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

        // Filter Chips Row
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Filter:",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                listOf("ALL", "PASS", "FAIL").forEach { filter ->
                    val isSelected = statusFilter == filter
                    val bgColor = if (isSelected) GoldPrimary else Color(0x33161A26)
                    val textColor = if (isSelected) TextDark else TextWhite

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, GoldPrimary, RoundedCornerShape(16.dp))
                            .background(bgColor)
                            .clickable { viewModel.setStatusFilter(filter) }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter,
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Evaluation Cards List
        if (evaluations.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No records found matching query.",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(evaluations) { eval ->
                EvaluationHistoryCard(
                    eval = eval,
                    onCopy = {
                        val details = "A23 PRO $selectedMarket\nDate: ${eval.record.date}\nOTC: ${eval.formulaResult?.otcFormatted}\nSuper Jodi: ${eval.formulaResult?.superJodis}\nResult: ${eval.record.openPanel}-${eval.record.jodi}-${eval.record.closePanel}\nStatus: ${if (eval.isPass) "PASS ✅" else "FAIL ❌"}"
                        clipboardManager.setText(AnnotatedString(details))
                        Toast.makeText(context, "Copied Record to Clipboard!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun EvaluationHistoryCard(
    eval: BacktestEvaluation,
    onCopy: () -> Unit
) {
    val borderColor = if (eval.isPass) NeonGreen else NeonPink
    val statusBg = if (eval.isPass) Color(0x2200C853) else Color(0x22FF1744)

    Card(
        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Date & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${eval.dayOfWeekHindi} • ${eval.record.date}",
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                            .background(statusBg)
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (eval.isPass) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = borderColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (eval.isPass) "PASS" else "FAIL",
                                color = borderColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3 Sub-Boxes Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // OTC Digits Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0x44FFC107), RoundedCornerShape(8.dp))
                        .background(Color(0x4410131E))
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "OTC DIGITS",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = eval.formulaResult?.otcFormatted ?: "0, 0, 0",
                            color = GoldPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Super Jodi Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0x44FFC107), RoundedCornerShape(8.dp))
                        .background(Color(0x4410131E))
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SUPER JODI",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = eval.formulaResult?.superJodis ?: "00, 05",
                            color = NeonCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Declaration Result Box
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0x44FFC107), RoundedCornerShape(8.dp))
                        .background(Color(0x4410131E))
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "DECLARATION RESULT",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${eval.record.openPanel}-${eval.record.jodi}-${eval.record.closePanel}",
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
