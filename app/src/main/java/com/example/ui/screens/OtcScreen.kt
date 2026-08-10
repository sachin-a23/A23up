package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
fun OtcScreen(viewModel: MainViewModel) {
    val selectedMarket by viewModel.selectedMarket.collectAsState()
    val availableMarkets by viewModel.availableMarkets.collectAsState()
    val evaluations by viewModel.backtestEvaluations.collectAsState()
    val currentRecordsAsc by viewModel.currentMarketRecordsAsc.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val weeklyStats by viewModel.weeklyStats.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var searchQuery by remember { mutableStateOf("") }
    var showAddMarketDialog by remember { mutableStateOf(false) }
    var newMarketNameInput by remember { mutableStateOf("") }

    var pdfExportFile by remember { mutableStateOf<File?>(null) }
    var showPdfDialog by remember { mutableStateOf(false) }

    fun downloadPdfReport() {
        val file = PdfExporter.generatePdfReport(context, selectedMarket, weeklyStats, evaluations)
        if (file != null) {
            pdfExportFile = file
            showPdfDialog = true
            Toast.makeText(context, "PDF Report Generated: ${file.name}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
        }
    }

    // Calculate Market Specific Live Next Day OTC Prediction
    val nextDayPred = remember(currentRecordsAsc, selectedMarket) {
        val validRecords = currentRecordsAsc.filter { !it.isHoliday && it.jodi.isNotBlank() && it.jodi != "**" && !it.jodi.contains("*") }
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

        // Get market formula indices
        val indices = FormulaEngine.getMarketFormulaIndices(selectedMarket)
        val i1 = indices.first
        val i2 = indices.second

        val rec1 = validRecords.getOrNull(validRecords.size - i1)
        val rec2 = validRecords.getOrNull(validRecords.size - i2)

        val jodi1 = rec1?.jodi ?: "00"
        val jodi2 = rec2?.jodi ?: "00"

        val fResult = FormulaEngine.calculateMarketSpecificFormula(selectedMarket, jodi1, jodi2)

        val lastFullRecordStr = if (lastRec.isHoliday || lastRec.jodi == "**" || lastRec.jodi.contains("*")) {
            "${lastRec.date}   result - *** - ** - ***"
        } else {
            "${lastRec.date}   result - ${lastRec.openPanel.ifBlank { "345" }} - ${lastRec.jodi} - ${lastRec.closePanel.ifBlank { "359" }}"
        }

        object {
            val targetDate = nextDateStr
            val otcFormatted = fResult.otcFormatted
            val superJodis = fResult.superJodis
            val lastEntryStr = lastFullRecordStr
            val formulaDesc = fResult.step1Description
            val step2 = fResult.step2Description
        }
    }

    // Filter evaluations by search query
    val filteredEvaluations = remember(evaluations, searchQuery) {
        if (searchQuery.isBlank()) {
            evaluations
        } else {
            val q = searchQuery.trim().lowercase()
            evaluations.filter { eval ->
                eval.record.date.lowercase().contains(q) ||
                eval.record.jodi.lowercase().contains(q) ||
                (eval.formulaResult?.otcFormatted ?: "").lowercase().contains(q)
            }
        }
    }

    // Accuracy stats
    val totalCount = evaluations.size
    val passCount = evaluations.count { it.isPass }
    val failCount = evaluations.count { !it.isPass && !it.record.isHoliday && it.record.jodi != "**" }
    val holidayCount = evaluations.count { it.record.isHoliday || it.record.jodi == "**" || it.record.jodi.contains("*") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Top Control Row: Market Selection & Actions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "* Market chunav",
                    color = GoldPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                // Horizontal list of market choice chips
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
                                .padding(horizontal = 14.dp, vertical = 6.dp),
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

                // Action Buttons Row: ADD Market, Dada - sync & PDF Download
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // ADD Market Button
                    Button(
                        onClick = { showAddMarketDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x4438BDF8)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "ADD Market",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Dada - sync Button
                    Button(
                        onClick = { viewModel.syncWithGitHub() },
                        modifier = Modifier.weight(1.1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x44FFC107)),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isSyncing,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isSyncing) "Syncing..." else "Dada - sync",
                            color = GoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // PDF Download Button
                    Button(
                        onClick = { downloadPdfReport() },
                        modifier = Modifier.weight(1.1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x4422C55E)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "PDF Export",
                            color = NeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (!syncMessage.isNullOrBlank()) {
                    Text(
                        text = syncMessage!!,
                        color = NeonGreen,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        // Live Next Day OTC Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, GoldPrimary, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Next Day OTC   ${nextDayPred?.targetDate ?: "Pending"}",
                            color = GoldPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x3338BDF8))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LOCKED FORMULA",
                                color = NeonCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // OTC Digit & Result Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x3310131E))
                            .border(1.dp, Color(0x44FFC107), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "OTC dijit -",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            Text(
                                text = nextDayPred?.otcFormatted ?: "Pending",
                                color = GoldPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Result -",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "pending",
                                color = NeonCyan,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Last Entry Line
                    Text(
                        text = "Last entry ${nextDayPred?.lastEntryStr ?: "N/A"}",
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Copy & Share Actions Row
                    val shareText = """
                        👑 A23 PRO - $selectedMarket
                        Live Next Day OTC ${nextDayPred?.targetDate ?: ""}
                        OTC Digits: ${nextDayPred?.otcFormatted ?: ""}
                        Result: Pending
                        Last Entry: ${nextDayPred?.lastEntryStr ?: ""}
                        Name: Sachin Solunke
                    """.trimIndent()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // PDF Download Icon Button
                        IconButton(
                            onClick = { downloadPdfReport() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "PDF Download",
                                tint = NeonGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Copy Button
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(shareText))
                                Toast.makeText(context, "Copied Live OTC Card!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Share Button ("Sher")
                        IconButton(
                            onClick = {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share OTC Prediction")
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Sher",
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Search & Filter Box
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "🔍  Filter by date or result",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = Color(0x66FFC107),
                    focusedContainerColor = Color(0x3310131E),
                    unfocusedContainerColor = Color(0x2210131E),
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // ALL DAY'S ACCURACY REPORT Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x6638BDF8), RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "All DAY'S ACCURACY REPORT",
                            color = GoldPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = { downloadPdfReport() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "Download PDF",
                                tint = NeonGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PASS ✅ - $passCount / $totalCount",
                            color = NeonGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "FAIL ❌ - $failCount / $totalCount",
                            color = NeonPink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "HOLIDAY 🏖️ - $holidayCount",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section Title: ALL DAYS REPORT
        item {
            Text(
                text = "ALL DAYS REPORT",
                color = GoldPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Report Items List
        items(filteredEvaluations) { eval ->
            val isHoliday = eval.record.isHoliday || eval.record.jodi == "**" || eval.record.jodi.contains("*")
            val isPass = eval.isPass

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = when {
                            isHoliday -> Color(0x4438BDF8)
                            isPass -> Color(0x6622C55E)
                            else -> Color(0x66EC4899)
                        },
                        shape = RoundedCornerShape(10.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isHoliday -> Color(0x2210131E)
                        isPass -> Color(0x22052E16)
                        else -> Color(0x222A0813)
                    }
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Header: MARKET NAME ♠️  DATE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedMarket.uppercase()} ♠️ ${eval.record.date}",
                            color = GoldPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Status Icon
                        if (isHoliday) {
                            Text(
                                text = "🏖️ HOLIDAY",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (isPass) {
                            Text(
                                text = "✅ PASS",
                                color = NeonGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "❌ FAIL",
                                color = NeonPink,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Content Row: OTC & RESULT
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OTC - ${if (isHoliday) "N/A" else (eval.formulaResult?.otcFormatted ?: "N/A")}",
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        val displayResult = if (isHoliday) {
                            "*** - ** - ***"
                        } else {
                            val open = eval.record.openPanel.ifBlank { "***" }
                            val jodi = eval.record.jodi.ifBlank { "**" }
                            val close = eval.record.closePanel.ifBlank { "***" }
                            "$open - $jodi - $close"
                        }

                        Text(
                            text = "RESULT - $displayResult",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    // Add Market Dialog
    if (showAddMarketDialog) {
        AlertDialog(
            onDismissRequest = { showAddMarketDialog = false },
            title = {
                Text("ADD NEW MARKET", color = GoldPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter custom market name:", color = TextWhite, fontSize = 12.sp)
                    OutlinedTextField(
                        value = newMarketNameInput,
                        onValueChange = { newMarketNameInput = it },
                        placeholder = { Text("e.g. MAIN BAZAR", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0x66FFC107),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newMarketNameInput.trim().uppercase()
                        if (trimmed.isNotBlank()) {
                            viewModel.addCustomMarket(trimmed)
                            Toast.makeText(context, "Added $trimmed!", Toast.LENGTH_SHORT).show()
                            showAddMarketDialog = false
                            newMarketNameInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Add", color = TextDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMarketDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = CyberCardBg
        )
    }

    // PDF Export Ready Dialog
    if (showPdfDialog && pdfExportFile != null) {
        AlertDialog(
            onDismissRequest = { showPdfDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PDF REPORT READY 📄", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "OTC Market Analysis Report for $selectedMarket is generated and downloaded successfully!",
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "📁 Saved in: GSM_PRO_Reports/${pdfExportFile?.name}",
                        color = NeonCyan,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            pdfExportFile?.let { PdfExporter.openPdfFile(context, it) }
                            showPdfDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Text("Open PDF", color = TextDark, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            pdfExportFile?.let { PdfExporter.sharePdfFile(context, it) }
                            showPdfDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Text("Share", color = TextDark, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showPdfDialog = false }) {
                    Text("Close", color = TextMuted)
                }
            },
            containerColor = CyberCardBg
        )
    }
}
