package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.MainViewModel

@Composable
fun DataMarketScreen(viewModel: MainViewModel) {
    var activeSubTab by remember { mutableStateOf(1) } // 1: Add/Edit Entry, 2: Add/Edit Market

    val selectedMarket by viewModel.selectedMarket.collectAsState()
    val availableMarkets by viewModel.availableMarkets.collectAsState()
    val recordsAsc by viewModel.currentMarketRecordsAsc.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()

    val entryDate by viewModel.entryDate.collectAsState()
    val entryResult by viewModel.entryResult.collectAsState()
    val entryIsHoliday by viewModel.entryIsHoliday.collectAsState()
    val editingId by viewModel.editingRecordId.collectAsState()

    val context = LocalContext.current

    val missingDates by viewModel.missingDatesList.collectAsState()
    val lastRecord by viewModel.lastEntryRecord.collectAsState()
    val liveDateStr = viewModel.liveDateStr

    // Display Toast on sync message
    syncMessage?.let { msg ->
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        viewModel.dismissSyncMessage()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Live Date & Last Record Quick Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📅 Live Date (Today): $liveDateStr",
                            color = GoldPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Last Record: ${lastRecord?.date ?: "None"}",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (missingDates.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⚠️ Missing Dates Detected: ${missingDates.size} dates (Tap chip to fill):",
                            color = Color(0xFFFF8A80),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            missingDates.take(3).forEach { mDate ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x33FFC107))
                                        .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp))
                                        .clickable { viewModel.autofillMissingDate(mDate) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = mDate,
                                        color = TextWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        // Sub-tabs Header (1. Add/Edit Entry | 2. Add/Edit Market)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tab 1
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (activeSubTab == 1) GoldPrimary else Color(0x33161A26))
                        .border(
                            1.dp,
                            if (activeSubTab == 1) GoldPrimary else Color(0x66FFC107),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { activeSubTab = 1 }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "1. Add/Edit Entry",
                        color = if (activeSubTab == 1) TextDark else TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Tab 2
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (activeSubTab == 2) GoldPrimary else Color(0x33161A26))
                        .border(
                            1.dp,
                            if (activeSubTab == 2) GoldPrimary else Color(0x66FFC107),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { activeSubTab = 2 }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "2. Add/Edit Market",
                        color = if (activeSubTab == 2) TextDark else TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (activeSubTab == 1) {
            // Form Header Title
            item {
                Text(
                    text = if (editingId == null) "Manual Data Entry" else "Edit Record (ID: $editingId)",
                    color = GoldPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Form Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FFC107)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Select Market:",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // Markets Pill Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            availableMarkets.take(4).forEach { market ->
                                val isSelected = market.equals(selectedMarket, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) GoldPrimary else Color(0x2210131E))
                                        .border(
                                            1.dp,
                                            if (isSelected) GoldPrimary else Color(0x44FFC107),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable { viewModel.selectMarket(market) }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = market,
                                        color = if (isSelected) TextDark else TextWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Date Input Field
                        OutlinedTextField(
                            value = entryDate,
                            onValueChange = { viewModel.entryDate.value = it },
                            label = { Text("Date (DD-MM-YYYY)", color = TextMuted, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = Color(0x66FFC107),
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            singleLine = true
                        )

                        // Holiday Checkbox
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = entryIsHoliday,
                                onCheckedChange = { viewModel.entryIsHoliday.value = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = GoldPrimary,
                                    uncheckedColor = TextMuted,
                                    checkmarkColor = TextDark
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Holiday / Chutti Day ( ***-**-*** )",
                                color = TextWhite,
                                fontSize = 12.sp
                            )
                        }

                        // Result Input Field
                        OutlinedTextField(
                            value = entryResult,
                            onValueChange = { viewModel.entryResult.value = it },
                            label = { Text("Result (e.g. 149-45-140 or 445-36-260)", color = TextMuted, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = Color(0x66FFC107),
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Golden Save Button
                        Button(
                            onClick = {
                                viewModel.saveEntry()
                                Toast.makeText(context, "Entry Saved!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (editingId == null) "Save / Update Entry" else "Update Entry",
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Sync with GitHub Button (Cyan stroke)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.5.dp, NeonCyan, RoundedCornerShape(12.dp))
                                .background(Color(0x2200E5FF))
                                .clickable { viewModel.syncGitHubData() },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isSyncing) "Syncing Server Data..." else "Sync All Data from GitHub Server",
                                    color = NeonCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Clear Data Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, NeonPink, RoundedCornerShape(10.dp))
                                    .background(Color(0x22FF007F))
                                    .clickable { viewModel.clearCurrentMarketData() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🗑️ Clear $selectedMarket",
                                    color = NeonPink,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(10.dp))
                                    .background(Color(0x33FF5252))
                                    .clickable { viewModel.clearAllSampleData() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "❌ Clear All Sample Data",
                                    color = Color(0xFFFF8A80),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Entries List Header
            item {
                Text(
                    text = "Entries for $selectedMarket (Edit/Delete)",
                    color = GoldPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Entries List
            val descRecords = recordsAsc.reversed()
            if (descRecords.isEmpty()) {
                item {
                    Text(
                        text = "No records added for $selectedMarket yet.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            } else {
                items(descRecords) { record ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFC107)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = record.date,
                                    color = GoldPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (record.isHoliday) "Result: ***-**-*** (Holiday)"
                                    else "Result: ${record.openPanel}-${record.jodi}-${record.closePanel}",
                                    color = TextWhite,
                                    fontSize = 13.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.editRecord(record) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.deleteRecord(record.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = NeonPink,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Tab 2: Add/Edit Market
            item {
                Text(
                    text = "Custom Market Manager",
                    color = GoldPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                var newMarketName by remember { mutableStateOf("") }

                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FFC107)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Add New Custom Market",
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = newMarketName,
                            onValueChange = { newMarketName = it.uppercase() },
                            label = { Text("Market Name (e.g. MAIN BAZAR, RAJDHANI)", color = TextMuted, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = Color(0x66FFC107),
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (newMarketName.isNotBlank()) {
                                    viewModel.selectMarket(newMarketName.trim())
                                    Toast.makeText(context, "Market '${newMarketName.trim()}' Selected!", Toast.LENGTH_SHORT).show()
                                    newMarketName = ""
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Create / Switch to Market",
                                color = Color.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
