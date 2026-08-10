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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
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
fun New1FormulaScreen(viewModel: MainViewModel) {
    val selectedMarket by viewModel.selectedMarket.collectAsState()
    val availableMarkets by viewModel.availableMarkets.collectAsState()
    val lockedEvaluations by viewModel.lockedBacktestEvaluations.collectAsState()
    val currentRecordsAsc by viewModel.currentMarketRecordsAsc.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val lockedStats by viewModel.lockedWeeklyStats.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("ALL") } // ALL, PASS, FAIL

    var manualJodi1 by remember { mutableStateOf("45") }
    var manualJodi2 by remember { mutableStateOf("89") }

    var pdfExportFile by remember { mutableStateOf<File?>(null) }
    var showPdfDialog by remember { mutableStateOf(false) }

    val lockedRule = remember(selectedMarket) {
        FormulaEngine.getLockedMarketRule(selectedMarket)
    }

    // Calculate Live Next Day OTC Prediction for NEW-1
    val nextDayPred = remember(currentRecordsAsc, selectedMarket) {
        val validRecords = currentRecordsAsc.filter {
            !it.isHoliday && it.jodi.isNotBlank() && it.jodi != "**" && !it.jodi.contains("*")
        }
        if (validRecords.isEmpty()) return@remember null

        val lastRec = currentRecordsAsc.lastOrNull() ?: return@remember null

        // Format Next Date
        val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US)
        val nextDateStr = try {
            val dateObj = sdf.parse(lastRec.date)
            if (dateObj != null) {
                val cal = java.util.Calendar.getInstance()
                cal.time = dateObj
                cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
                sdf.format(cal.time)
            } else "Next Day"
        } catch (e: Exception) {
            "Next Day"
        }

        val rule = FormulaEngine.getLockedMarketRule(selectedMarket)
        val rec1 = validRecords.getOrNull(validRecords.size - rule.index1)
        val rec2 = validRecords.getOrNull(validRecords.size - rule.index2)

        val jodi1 = rec1?.jodi ?: "00"
        val jodi2 = rec2?.jodi ?: "00"

        val fResult = FormulaEngine.calculateLockedMarketFormula(selectedMarket, jodi1, jodi2)

        val lastFullRecordStr = if (lastRec.isHoliday || lastRec.jodi == "**" || lastRec.jodi.contains("*")) {
            "${lastRec.date}   result - *** - ** - ***"
        } else {
            "${lastRec.date}   result - ${lastRec.openPanel.ifBlank { "345" }} - ${lastRec.jodi} - ${lastRec.closePanel.ifBlank { "359" }}"
        }

        object {
            val targetDate = nextDateStr
            val rec1Date = rec1?.date ?: "N/A"
            val rec1Jodi = jodi1
            val rec2Date = rec2?.date ?: "N/A"
            val rec2Jodi = jodi2
            val otcDigits = fResult.otcDigits
            val otcFormatted = fResult.otcFormatted
            val superJodis = fResult.superJodis
            val lastEntryStr = lastFullRecordStr
            val formulaDesc = fResult.step1Description
            val step2 = fResult.step2Description
        }
    }

    // Filter evaluations by search & filter status
    val filteredEvaluations = remember(lockedEvaluations, searchQuery, filterStatus) {
        var list = lockedEvaluations
        if (filterStatus == "PASS") {
            list = list.filter { it.isPass }
        } else if (filterStatus == "FAIL") {
            list = list.filter { !it.isPass }
        }

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter { eval ->
                eval.record.date.lowercase().contains(q) ||
                        eval.dayOfWeekHindi.lowercase().contains(q) ||
                        eval.record.jodi.contains(q) ||
                        eval.record.openPanel.contains(q) ||
                        eval.record.closePanel.contains(q)
            }
        }
        list
    }

    fun downloadPdfReport() {
        val file = PdfExporter.generatePdfReport(context, "$selectedMarket (NEW-1 LOCKED)", lockedStats, lockedEvaluations)
        if (file != null) {
            pdfExportFile = file
            showPdfDialog = true
            Toast.makeText(context, "NEW-1 PDF Report Generated: ${file.name}", Toast.LENGTH_SHORT).show()
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
        // TOP HEADER BADGE
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.5.dp, NeonCyan),
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
                                text = "NEW-1 FORMULA ENGINE",
                                color = GoldPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x33FFD700))
                                    .border(1.dp, GoldPrimary, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "LOCKED",
                                    color = GoldPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Market-Specific Mathematical OTC Pattern Extraction",
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
                    text = "SELECT MARKET FOR NEW-1 FORMULA",
                    color = NeonCyan,
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

        // LOCKED FORMULA RULE BANNER CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131A2A)),
                border = BorderStroke(1.5.dp, GoldPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked Rule",
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LOCKED FORMULA RULE: ${lockedRule.marketKey}",
                                color = GoldPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x3300E676))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = NeonGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0x44FFC107), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = lockedRule.ruleDescription,
                                color = NeonGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when (lockedRule.marketKey) {
                                    "KALYAN" -> "Uses Jodi(i-2) and Jodi(i-3). Adds them, multiplies by Jodi(i-2), then divides by 9."
                                    "TIME-BAZAR" -> "Uses Jodi(i-2) and Jodi(i-3). Adds them, multiplies by Jodi(i-2), then divides by 2."
                                    "MILAN" -> "Uses Jodi(i-1) and Jodi(i-3). Multiplies them together, then divides by 8."
                                    "SRIDEVI" -> "Uses Jodi(i-1) and Jodi(i-4). Adds them, multiplies by Jodi(i-1), then divides by 9."
                                    else -> "Uses Jodi(i-1) and Jodi(i-2) standard market fallback formula."
                                },
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // LIVE NEXT DAY OTC PREDICTION CARD
        item {
            nextDayPred?.let { pred ->
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
                                Text(
                                    text = "LIVE OTC PREDICTION (NEW-1)",
                                    color = NeonGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Target Date: ${pred.targetDate}",
                                    color = TextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        val copyTxt = """
                                            🎯 NEW-1 OTC PREDICTION ($selectedMarket)
                                            Target Date: ${pred.targetDate}
                                            Formula: ${lockedRule.ruleDescription}
                                            Jodi Inputs: [J1: ${pred.rec1Jodi} (${pred.rec1Date})] × [J2: ${pred.rec2Jodi} (${pred.rec2Date})]
                                            Step 1: ${pred.formulaDesc}
                                            Step 2: ${pred.step2}
                                            -----------------------------
                                            🔥 OTC DIGITS: ${pred.otcFormatted}
                                            💎 SUPER JODIS: ${pred.superJodis}
                                        """.trimIndent()
                                        clipboardManager.setText(AnnotatedString(copyTxt))
                                        Toast.makeText(context, "NEW-1 Prediction Copied!", Toast.LENGTH_SHORT).show()
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
                                            🎯 NEW-1 OTC PREDICTION ($selectedMarket)
                                            Target Date: ${pred.targetDate}
                                            OTC Digits: ${pred.otcFormatted}
                                            Super Jodis: ${pred.superJodis}
                                        """.trimIndent()
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, shareTxt)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Share Prediction"))
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

                        // Selected Jodis Information Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1A233A))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "SELECTED JODIS FROM HISTORY:",
                                    color = GoldPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Index-1 (${pred.rec1Date}): Jodi [ ${pred.rec1Jodi} ]",
                                        color = TextWhite,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "Index-2 (${pred.rec2Date}): Jodi [ ${pred.rec2Jodi} ]",
                                        color = TextWhite,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Math steps
                        Text(text = "FORMULA STEPS:", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = pred.formulaDesc, color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = pred.step2, color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                        Spacer(modifier = Modifier.height(12.dp))

                        // OTC DIGITS CARDS
                        Text(text = "EXTRACTED OTC DIGITS", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            pred.otcDigits.forEach { digit ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1E293B))
                                        .border(1.5.dp, NeonGreen, RoundedCornerShape(12.dp))
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
                                    text = pred.superJodis,
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
        }

        // MANUAL CALCULATOR FOR TESTING CUSTOM JODI PAIRS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.dp, Color(0x33FFC107)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "MANUAL FORMULA CALCULATOR ($selectedMarket)",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = manualJodi1,
                            onValueChange = { if (it.length <= 2) manualJodi1 = it },
                            label = { Text("Jodi 1", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = TextMuted,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            )
                        )

                        OutlinedTextField(
                            value = manualJodi2,
                            onValueChange = { if (it.length <= 2) manualJodi2 = it },
                            label = { Text("Jodi 2", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = TextMuted,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val manualResult = remember(manualJodi1, manualJodi2, selectedMarket) {
                        FormulaEngine.calculateLockedMarketFormula(selectedMarket, manualJodi1, manualJodi2)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(text = manualResult.step1Description, color = TextWhite, fontSize = 11.sp)
                            Text(text = manualResult.step2Description, color = NeonCyan, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Extracted OTC Digits: [ ${manualResult.otcFormatted} ]",
                                color = NeonGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // WEEKLY STATS & ACCURACY CARD
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
                            text = "NEW-1 BACKTEST STATS ($selectedMarket)",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Accuracy: %.1f%%".format(lockedStats.accuracyPercentage),
                            color = if (lockedStats.accuracyPercentage >= 70f) NeonGreen else GoldPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Days: ${lockedStats.totalEvaluated}", color = TextWhite, fontSize = 11.sp)
                        Text("Pass Days: ${lockedStats.passDays} ✅", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Fail Days: ${lockedStats.failDays} ❌", color = Color(0xFFFF5252), fontSize = 11.sp)
                    }
                }
            }
        }

        // BACKTEST LOG SEARCH & FILTER CHIPS
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
                    placeholder = { Text("Search date, jodi, etc...", fontSize = 11.sp, color = TextMuted) },
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

        // HISTORICAL BACKTEST LOG ITEMS
        items(filteredEvaluations) { eval ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(
                    1.dp,
                    if (eval.isPass) NeonGreen.copy(alpha = 0.5f) else Color(0xFFFF5252).copy(alpha = 0.3f)
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
                                text = "NEW-1 OTC: [ ${res.otcFormatted} ]",
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
                                    if (eval.isPass) NeonGreen else Color(0xFFFF5252),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (eval.isPass) "PASS ✅" else "FAIL ❌",
                                color = if (eval.isPass) NeonGreen else Color(0xFFFF5252),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // PDF EXPORT DIALOG
    if (showPdfDialog && pdfExportFile != null) {
        AlertDialog(
            onDismissRequest = { showPdfDialog = false },
            containerColor = CyberCardBg,
            title = {
                Text(text = "NEW-1 PDF Generated", color = GoldPrimary, fontWeight = FontWeight.Bold)
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
