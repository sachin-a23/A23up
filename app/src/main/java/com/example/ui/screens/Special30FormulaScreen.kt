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
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import com.example.formula.FormulaEngine
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
fun Special30FormulaScreen(viewModel: MainViewModel) {
    val selectedMarket by viewModel.selectedMarket.collectAsState()
    val availableMarkets by viewModel.availableMarkets.collectAsState()
    val currentRecordsAsc by viewModel.currentMarketRecordsAsc.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var manualJodi by remember { mutableStateOf("71") }
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("ALL") }

    var pdfExportFile by remember { mutableStateOf<File?>(null) }
    var showPdfDialog by remember { mutableStateOf(false) }

    // Calculate Special 30 Formula for live market data
    val validRecords = remember(currentRecordsAsc) {
        currentRecordsAsc.filter {
            !it.isHoliday && it.jodi.isNotBlank() && it.jodi != "**" && !it.jodi.contains("*")
        }
    }

    val lastRec = validRecords.lastOrNull()
    val lastJodi = lastRec?.jodi ?: "71"

    val liveResult = remember(lastJodi) {
        FormulaEngine.calculateSpecial30JodiFormula(lastJodi)
    }

    // Historical Backtest Evaluation
    val specialEvaluations = remember(currentRecordsAsc) {
        val list = mutableListOf<com.example.formula.BacktestEvaluation>()
        for (i in 1 until currentRecordsAsc.size) {
            val curr = currentRecordsAsc[i]
            val prev = currentRecordsAsc.getOrNull(i - 1)
            val eval = FormulaEngine.evaluateSpecial30Record(curr, prev)
            list.add(eval)
        }
        list.asReversed()
    }

    val filteredEvaluations = remember(specialEvaluations, searchQuery, filterStatus) {
        var list = specialEvaluations.toList()
        if (filterStatus == "PASS") {
            list = list.filter { it.isPass }
        } else if (filterStatus == "FAIL") {
            list = list.filter { !it.isPass }
        }

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter { eval ->
                eval.record.date.lowercase().contains(q) ||
                        eval.record.jodi.contains(q) ||
                        (eval.formulaResult?.otcFormatted ?: "").contains(q)
            }
        }
        list
    }

    val specialStats = remember(specialEvaluations) {
        FormulaEngine.calculateWeeklyStats(specialEvaluations)
    }

    fun downloadPdfReport() {
        val file = PdfExporter.generatePdfReport(context, "$selectedMarket (SPECIAL 30 FORMULA)", specialStats, specialEvaluations)
        if (file != null) {
            pdfExportFile = file
            showPdfDialog = true
            Toast.makeText(context, "Special 30 PDF Generated: ${file.name}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
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
                            Text(
                                text = "SPECIAL 30 FORMULA",
                                color = GoldPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x33FFC107))
                                    .border(1.dp, GoldPrimary, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "SPECIAL",
                                    color = GoldPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Formula: ( 30 × Last Jodi ÷ 2 ) = * * * *",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "OTC counts vary dynamically: 2, 3, or 4 digits based on result!",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = { viewModel.syncWithGitHub() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x3300E5FF))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // MARKET SELECTOR CHIPS
        item {
            Column {
                Text(
                    text = "SELECT MARKET FOR SPECIAL 30 FORMULA",
                    color = GoldPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableMarkets) { mkt ->
                        val isSel = selectedMarket.uppercase() == mkt.uppercase()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) GoldPrimary else Color(0xFF1E293B))
                                .border(
                                    width = 1.dp,
                                    color = if (isSel) GoldPrimary else Color(0x3338BDF8),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.selectMarket(mkt) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = mkt,
                                color = if (isSel) TextDark else TextWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // LIVE NEXT DAY SPECIAL OTC PREDICTION CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.5.dp, NeonGreen),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LIVE OTC PREDICTION",
                                    color = NeonGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "Market: $selectedMarket | Last Jodi: $lastJodi",
                                color = TextWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    val copyTxt = """
                                        👑 SPECIAL 30 OTC PREDICTION ($selectedMarket)
                                        Formula: ( 30 × $lastJodi ÷ 2 )
                                        Step 1: ${liveResult.step1Description}
                                        Step 2: ${liveResult.step2Description}
                                        -----------------------------
                                        🔥 OTC DIGITS (${liveResult.otcDigits.size} Digits): ${liveResult.otcFormatted}
                                        💎 SUPER JODIS: ${liveResult.superJodis}
                                    """.trimIndent()
                                    clipboardManager.setText(AnnotatedString(copyTxt))
                                    Toast.makeText(context, "Special 30 Prediction Copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x3300E676))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    val shareTxt = """
                                        👑 SPECIAL 30 OTC PREDICTION ($selectedMarket)
                                        Formula: ( 30 × $lastJodi ÷ 2 )
                                        OTC Digits (${liveResult.otcDigits.size} Digits): ${liveResult.otcFormatted}
                                        Super Jodis: ${liveResult.superJodis}
                                    """.trimIndent()
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareTxt)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Special Prediction"))
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x3338BDF8))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = { downloadPdfReport() },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33FFC107))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = "PDF",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Calculation Step Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF131A2A))
                            .border(1.dp, Color(0x44FFC107), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(text = "CALCULATION STEPS:", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = liveResult.step1Description, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = liveResult.step2Description, color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // OTC DIGITS DYNAMIC HEADER & BADGES
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "EXTRACTED OTC DIGITS", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x3322C55E))
                                .border(1.dp, NeonGreen, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "DYNAMIC ${liveResult.otcDigits.size} OTC DIGITS",
                                color = NeonGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dynamic OTC digit cards grid
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        liveResult.otcDigits.forEach { digit ->
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
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "Cut: ${(digit + 5) % 10}",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Super Jodis Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF152033))
                            .border(1.dp, Color(0x4400E5FF), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("SUPER JODIS:", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = liveResult.superJodis,
                                color = TextWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }

        // MANUAL CALCULATOR FOR TESTING CUSTOM JODI
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.dp, GoldPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Functions,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MANUAL JODI CALCULATOR TESTER",
                            color = GoldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = manualJodi,
                        onValueChange = { if (it.length <= 2) manualJodi = it },
                        label = { Text("Enter Last Jodi (e.g. 71, 05, 84)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = TextMuted,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val manualRes = remember(manualJodi) {
                        FormulaEngine.calculateSpecial30JodiFormula(manualJodi)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0x44FFC107), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(text = manualRes.step1Description, color = TextWhite, fontSize = 11.sp)
                            Text(text = manualRes.step2Description, color = NeonCyan, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Extracted OTC (${manualRes.otcDigits.size} Digits): [ ${manualRes.otcFormatted} ]",
                                color = NeonGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // STATS & ACCURACY
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.dp, Color(0x3338BDF8)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SPECIAL 30 BACKTEST STATS ($selectedMarket)",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Accuracy: %.1f%%".format(specialStats.accuracyPercentage),
                            color = if (specialStats.accuracyPercentage >= 70f) NeonGreen else GoldPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Evaluated: ${specialStats.totalEvaluated}", color = TextWhite, fontSize = 11.sp)
                        Text("Pass Days: ${specialStats.passDays} ✅", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Fail Days: ${specialStats.failDays} ❌", color = NeonPink, fontSize = 11.sp)
                    }
                }
            }
        }

        // SEARCH & FILTER CHIPS
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HISTORICAL LOGS (${filteredEvaluations.size})",
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("ALL", "PASS", "FAIL").forEach { st ->
                            val isSel = filterStatus == st
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) GoldPrimary else Color(0xFF1E293B))
                                    .clickable { filterStatus = st }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = st,
                                    color = if (isSel) TextDark else TextWhite,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search date or jodi...", fontSize = 11.sp, color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = TextMuted,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    )
                )
            }
        }

        // HISTORICAL LOG ITEMS
        items(filteredEvaluations) { eval ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(
                    1.dp,
                    if (eval.isPass) NeonGreen.copy(alpha = 0.5f) else NeonPink.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = eval.record.date,
                                color = TextWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = eval.dayOfWeekHindi,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        eval.formulaResult?.let { res ->
                            Text(
                                text = "Result: ${eval.record.openPanel.ifBlank { "***" }} - ${eval.record.jodi} - ${eval.record.closePanel.ifBlank { "***" }}",
                                color = GoldPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Special OTC (${res.otcDigits.size} Digits): [ ${res.otcFormatted} ]",
                                color = NeonCyan,
                                fontSize = 11.sp
                            )
                        } ?: Text(
                            text = "Holiday / No Result",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    if (eval.formulaResult != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (eval.isPass) Color(0x3300E676) else Color(0x33FF5252))
                                .border(
                                    1.dp,
                                    if (eval.isPass) NeonGreen else NeonPink,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (eval.isPass) "PASS ✅" else "FAIL ❌",
                                color = if (eval.isPass) NeonGreen else NeonPink,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // PDF DIALOG
    if (showPdfDialog && pdfExportFile != null) {
        AlertDialog(
            onDismissRequest = { showPdfDialog = false },
            containerColor = CyberCardBg,
            title = {
                Text(text = "Special 30 PDF Generated", color = GoldPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Report saved successfully:\n${pdfExportFile?.name}",
                    color = TextWhite,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val file = pdfExportFile ?: return@Button
                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "No PDF viewer app found", Toast.LENGTH_SHORT).show()
                        }
                        showPdfDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Open PDF", color = TextDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPdfDialog = false }) {
                    Text("Close", color = TextMuted)
                }
            }
        )
    }
}
