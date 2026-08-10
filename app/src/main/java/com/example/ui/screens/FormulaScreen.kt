package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.MainViewModel

@Composable
fun FormulaScreen(viewModel: MainViewModel) {
    var selectedFormulaTab by remember { mutableIntStateOf(1) } // 1 for OTC, 2 for NEW-1, 3 for Special 30

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Formula Switcher Bar (1 [OTC], 2 [NEW-1], 3 [SPECIAL 30])
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF10131E))
                .border(1.dp, GoldPrimary, RoundedCornerShape(14.dp))
                .padding(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Option 1: [OTC] Formula
                val isTab1 = selectedFormulaTab == 1
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isTab1) GoldPrimary else Color(0x3310131E))
                        .border(1.dp, if (isTab1) GoldPrimary else Color(0x44FFC107), RoundedCornerShape(10.dp))
                        .clickable { selectedFormulaTab = 1 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "1 [OTC]",
                        color = if (isTab1) TextDark else TextWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Option 2: [NEW-1] Formula
                val isTab2 = selectedFormulaTab == 2
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isTab2) NeonCyan else Color(0x3310131E))
                        .border(1.dp, if (isTab2) NeonCyan else Color(0x4400E5FF), RoundedCornerShape(10.dp))
                        .clickable { selectedFormulaTab = 2 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "2 [NEW-1]",
                        color = if (isTab2) TextDark else TextWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Option 3: [SPECIAL 30] Formula
                val isTab3 = selectedFormulaTab == 3
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isTab3) NeonGreen else Color(0x3310131E))
                        .border(1.dp, if (isTab3) NeonGreen else Color(0x4422C55E), RoundedCornerShape(10.dp))
                        .clickable { selectedFormulaTab = 3 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "3 [SPECIAL 30]",
                        color = if (isTab3) TextDark else TextWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Content View based on selected tab
        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            when (selectedFormulaTab) {
                1 -> OtcScreen(viewModel = viewModel)
                2 -> New1FormulaScreen(viewModel = viewModel)
                else -> Special30FormulaScreen(viewModel = viewModel)
            }
        }
    }
}
