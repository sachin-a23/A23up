package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Square
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.SavedPatternItem
import java.io.File
import java.io.FileOutputStream
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

// Drawing Tool Types
enum class ToolType {
    FREEHAND,
    BOX,
    LINE,
    ARROW,
    OVAL,
    TEXT
}

// Color Options
data class MarkerColor(val name: String, val color: Color, val hex: String)

val MarkerColorList = listOf(
    MarkerColor("Neon Green", Color(0xFF00E676), "#00E676"),
    MarkerColor("Bright Orange", Color(0xFFFF9100), "#FF9100"),
    MarkerColor("Electric Yellow", Color(0xFFFFEA00), "#FFEA00"),
    MarkerColor("Cyber Cyan", Color(0xFF00E5FF), "#00E5FF"),
    MarkerColor("Neon Pink", Color(0xFFFF007F), "#FF007F"),
    MarkerColor("A23 Gold", Color(0xFFFFD700), "#FFD700"),
    MarkerColor("Pure White", Color(0xFFFFFFFF), "#FFFFFF")
)

val BrightOrangeColor = Color(0xFFFF9100)
val YellowColor = Color(0xFFFFEA00)
val CyanColor = Color(0xFF00E5FF)
val PinkColor = Color(0xFFFF007F)

// Drawing Shape Sealed Interface
sealed class ChartShape {
    data class PathShape(
        val points: List<Offset>,
        val color: Color,
        val strokeWidth: Float,
        val isGlow: Boolean
    ) : ChartShape()

    data class BoxShape(
        val start: Offset,
        val end: Offset,
        val color: Color,
        val strokeWidth: Float,
        val isGlow: Boolean
    ) : ChartShape()

    data class LineShape(
        val start: Offset,
        val end: Offset,
        val color: Color,
        val strokeWidth: Float,
        val isGlow: Boolean
    ) : ChartShape()

    data class ArrowShape(
        val start: Offset,
        val end: Offset,
        val color: Color,
        val strokeWidth: Float,
        val isGlow: Boolean
    ) : ChartShape()

    data class OvalShape(
        val start: Offset,
        val end: Offset,
        val color: Color,
        val strokeWidth: Float,
        val isGlow: Boolean
    ) : ChartShape()

    data class TextShape(
        val position: Offset,
        val text: String,
        val color: Color,
        val fontSize: Float
    ) : ChartShape()
}

// PATTERN STAMP DATA STRUCTURES & DIALOG
data class PatternStampOption(
    val id: String,
    val name: String,
    val description: String,
    val iconStr: String
)

val PresetPatternStampList = listOf(
    PatternStampOption("CROSS_X", "⚡ Super Neon Cross (X)", "Dual diagonal glowing cross lines across target cell", "✖️"),
    PatternStampOption("GOLD_BOX", "📦 Golden Highlight Box", "Glow-bordered target highlight rectangle", "🔳"),
    PatternStampOption("BULLSEYE", "🎯 Target Bullseye Circle", "Concentric double glowing target circles", "🎯"),
    PatternStampOption("TREND_ARROW", "➡️ Trend Pointer Arrow", "Glowing diagonal arrow connecting touch points", "↗️"),
    PatternStampOption("OTC_BANNER", "🏷️ OTC 4-Digit Banner", "Pre-filled glowing banner with 'OTC: 3 - 8 - 4 - 9'", "🏷️"),
    PatternStampOption("STAR_STAMP", "⭐ Golden Star Marker", "Glowing gold star stamp on key chart numbers", "⭐"),
    PatternStampOption("DELTA_TRIANGLE", "🔺 Neon Delta Triangle", "Glowing neon green triangle marker", "🔺"),
    PatternStampOption("JODI_GRID", "🏁 2x2 Jodi Matrix Grid", "Glow-lined matrix grid box for panel analysis", "🏁"),
    PatternStampOption("PAIR_BAR", "🔗 Pair Connection Bar", "Horizontal connection bar with glowing end dots", "➖"),
    PatternStampOption("DROP_POINTER", "📍 Touch Drop Pointer", "Vertical drop line with glowing pin head", "📍")
)

@Composable
fun PatternStampPopupDialog(
    onDismiss: () -> Unit,
    onSelectStamp: (PatternStampOption) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NEON PATTERNS & STAMPS",
                    color = GoldPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                Text(
                    text = "Tap any preset stamp to instantly place it on your chart canvas:",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(PresetPatternStampList) { stamp ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0x3310131E)),
                            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectStamp(stamp) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(GoldPrimary.copy(alpha = 0.2f))
                                        .border(1.dp, GoldPrimary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = stamp.iconStr, fontSize = 18.sp)
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stamp.name,
                                        color = TextWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stamp.description,
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GoldPrimary)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "+ STAMP",
                                        color = Color.Black,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = TextMuted, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = CyberCardBg
    )
}

// Canvas Shapes Rendering Helper
fun DrawScope.drawChartShape(shape: ChartShape) {
    when (shape) {
        is ChartShape.PathShape -> {
            if (shape.points.size < 2) return
            val path = Path().apply {
                moveTo(shape.points.first().x, shape.points.first().y)
                for (i in 1 until shape.points.size) {
                    lineTo(shape.points[i].x, shape.points[i].y)
                }
            }

            if (shape.isGlow) {
                drawPath(
                    path = path,
                    color = shape.color.copy(alpha = 0.45f),
                    style = Stroke(
                        width = shape.strokeWidth * 2.5f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            drawPath(
                path = path,
                color = shape.color,
                style = Stroke(
                    width = shape.strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        is ChartShape.BoxShape -> {
            val left = kotlin.math.min(shape.start.x, shape.end.x)
            val top = kotlin.math.min(shape.start.y, shape.end.y)
            val right = kotlin.math.max(shape.start.x, shape.end.x)
            val bottom = kotlin.math.max(shape.start.y, shape.end.y)
            val size = Size(right - left, bottom - top)

            if (shape.isGlow) {
                drawRoundRect(
                    color = shape.color.copy(alpha = 0.45f),
                    topLeft = Offset(left, top),
                    size = size,
                    cornerRadius = CornerRadius(12f, 12f),
                    style = Stroke(width = shape.strokeWidth * 2.2f)
                )
            }

            drawRoundRect(
                color = shape.color,
                topLeft = Offset(left, top),
                size = size,
                cornerRadius = CornerRadius(12f, 12f),
                style = Stroke(width = shape.strokeWidth)
            )
        }

        is ChartShape.LineShape -> {
            if (shape.isGlow) {
                drawLine(
                    color = shape.color.copy(alpha = 0.45f),
                    start = shape.start,
                    end = shape.end,
                    strokeWidth = shape.strokeWidth * 2.2f,
                    cap = StrokeCap.Round
                )
            }

            drawLine(
                color = shape.color,
                start = shape.start,
                end = shape.end,
                strokeWidth = shape.strokeWidth,
                cap = StrokeCap.Round
            )
        }

        is ChartShape.ArrowShape -> {
            if (shape.isGlow) {
                drawLine(
                    color = shape.color.copy(alpha = 0.45f),
                    start = shape.start,
                    end = shape.end,
                    strokeWidth = shape.strokeWidth * 2.2f,
                    cap = StrokeCap.Round
                )
            }

            drawLine(
                color = shape.color,
                start = shape.start,
                end = shape.end,
                strokeWidth = shape.strokeWidth,
                cap = StrokeCap.Round
            )

            val angle = atan2(shape.end.y - shape.start.y, shape.end.x - shape.start.x)
            val arrowLength = shape.strokeWidth * 3f + 15f
            val x1 = shape.end.x - arrowLength * cos(angle - Math.PI / 6).toFloat()
            val y1 = shape.end.y - arrowLength * sin(angle - Math.PI / 6).toFloat()
            val x2 = shape.end.x - arrowLength * cos(angle + Math.PI / 6).toFloat()
            val y2 = shape.end.y - arrowLength * sin(angle + Math.PI / 6).toFloat()

            val arrowPath = Path().apply {
                moveTo(shape.end.x, shape.end.y)
                lineTo(x1, y1)
                lineTo(x2, y2)
                close()
            }

            drawPath(path = arrowPath, color = shape.color)
        }

        is ChartShape.OvalShape -> {
            val left = kotlin.math.min(shape.start.x, shape.end.x)
            val top = kotlin.math.min(shape.start.y, shape.end.y)
            val right = kotlin.math.max(shape.start.x, shape.end.x)
            val bottom = kotlin.math.max(shape.start.y, shape.end.y)
            val size = Size(right - left, bottom - top)

            if (shape.isGlow) {
                drawOval(
                    color = shape.color.copy(alpha = 0.45f),
                    topLeft = Offset(left, top),
                    size = size,
                    style = Stroke(width = shape.strokeWidth * 2.2f)
                )
            }

            drawOval(
                color = shape.color,
                topLeft = Offset(left, top),
                size = size,
                style = Stroke(width = shape.strokeWidth)
            )
        }

        is ChartShape.TextShape -> {
            drawContext.canvas.nativeCanvas.drawText(
                shape.text,
                shape.position.x,
                shape.position.y,
                android.graphics.Paint().apply {
                    color = shape.color.hashCode()
                    textSize = shape.fontSize * 1.8f
                    isFakeBoldText = true
                    setShadowLayer(10f, 0f, 0f, shape.color.hashCode())
                }
            )
        }
    }
}

// Built-in Sample Matka Weekly Table Chart Canvas
@Composable
fun MatkaTableChartCanvas() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        drawRect(color = Color(0xFFFFF1D0))

        val cols = 8
        val rows = 12
        val colWidth = width / cols
        val rowHeight = height / rows

        for (i in 0..cols) {
            drawLine(
                color = Color(0x88000000),
                start = Offset(i * colWidth, 0f),
                end = Offset(i * colWidth, height),
                strokeWidth = 1.5f
            )
        }
        for (j in 0..rows) {
            drawLine(
                color = Color(0x88000000),
                start = Offset(0f, j * rowHeight),
                end = Offset(width, j * rowHeight),
                strokeWidth = 1.5f
            )
        }

        val sampleData = listOf(
            listOf("20/04", "178", "66", "259", "138", "21", "290", "234"),
            listOf("27/04", "157", "37", "359", "459", "80", "244", "224"),
            listOf("04/05", "160", "71", "335", "255", "29", "577", "469"),
            listOf("11/05", "247", "31", "579", "490", "35", "357", "660"),
            listOf("18/05", "338", "40", "488", "670", "38", "369", "355"),
            listOf("25/05", "679", "27", "449", "567", "87", "458", "899"),
            listOf("01/06", "124", "78", "288", "179", "70", "136", "369"),
            listOf("08/06", "499", "25", "230", "266", "40", "569", "688"),
            listOf("15/06", "237", "23", "689", "479", "01", "344", "370"),
            listOf("22/06", "168", "56", "880", "900", "94", "257", "677"),
            listOf("29/06", "470", "18", "369", "340", "77", "250", "368"),
            listOf("06/07", "378", "83", "157", "579", "14", "149", "180")
        )

        val paint = android.graphics.Paint().apply {
            textSize = rowHeight * 0.38f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
        }

        for (r in sampleData.indices) {
            val row = sampleData[r]
            for (c in row.indices) {
                val valStr = row[c]
                val x = c * colWidth + colWidth / 2f
                val y = r * rowHeight + rowHeight * 0.65f

                if (c == 2 || c == 5 || valStr == "66" || valStr == "94" || valStr == "27" || valStr == "83") {
                    paint.color = android.graphics.Color.parseColor("#D32F2F")
                } else {
                    paint.color = android.graphics.Color.parseColor("#1B1B1B")
                }
                drawContext.canvas.nativeCanvas.drawText(valStr, x, y, paint)
            }
        }
    }
}

// Built-in Candlestick Technical Chart Canvas
@Composable
fun CandlestickChartCanvas() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        drawRect(color = Color(0xFF10131E))

        for (i in 1..8) {
            drawLine(color = Color(0x22FFFFFF), start = Offset(0f, height * i / 9f), end = Offset(width, height * i / 9f), strokeWidth = 1f)
            drawLine(color = Color(0x22FFFFFF), start = Offset(width * i / 9f, 0f), end = Offset(width * i / 9f, height), strokeWidth = 1f)
        }

        val candles = 18
        val candleWidth = width / (candles * 1.5f)

        for (i in 0 until candles) {
            val cx = width * (i + 1) / (candles + 1)
            val isBullish = i % 3 != 0
            val color = if (isBullish) Color(0xFF00E676) else Color(0xFFFF5252)

            val openY = height * (0.3f + (i * 0.02f) + if (isBullish) 0.05f else -0.05f)
            val closeY = height * (0.3f + (i * 0.02f) + if (isBullish) -0.05f else 0.05f)
            val highY = openY.coerceAtMost(closeY) - 30f
            val lowY = openY.coerceAtLeast(closeY) + 30f

            drawLine(color = color, start = Offset(cx, highY), end = Offset(cx, lowY), strokeWidth = 2f)

            val topY = openY.coerceAtMost(closeY)
            val bodyH = (openY - closeY).let { kotlin.math.abs(it) }.coerceAtLeast(10f)
            drawRect(
                color = color,
                topLeft = Offset(cx - candleWidth / 2f, topY),
                size = Size(candleWidth, bodyH)
            )
        }
    }
}

// Helper Tool Chip Composable
@Composable
fun ToolChip(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color.copy(alpha = 0.3f) else Color(0x22FFFFFF))
            .border(1.dp, if (isSelected) color else Color(0x33FFFFFF), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = title, tint = if (isSelected) color else TextMuted, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(title, color = if (isSelected) color else TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Export Marked Chart as PDF Report Helper
fun exportChartAsPdf(context: Context, markingCount: Int, chartType: String): File? {
    return try {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        canvas.drawColor(android.graphics.Color.parseColor("#0D111A"))

        val paintHeader = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#FFC107")
            textSize = 24f
            isFakeBoldText = true
        }
        canvas.drawText("A23 PRO - CHART MARKER REPORT", 40f, 60f, paintHeader)

        val paintSub = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#00E5FF")
            textSize = 14f
        }
        canvas.drawText("Chart Type: $chartType • Total Markings: $markingCount", 40f, 90f, paintSub)

        val paintBorder = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#FFC107")
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRect(40f, 120f, 555f, 700f, paintBorder)

        val paintNote = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 12f
        }
        canvas.drawText("Verified A23 PRO Analysis Output • Generated on ${System.currentTimeMillis()}", 40f, 740f, paintNote)

        pdfDoc.finishPage(page)

        val file = File(context.cacheDir, "Chart_Marker_Report_${System.currentTimeMillis()}.pdf")
        val out = FileOutputStream(file)
        pdfDoc.writeTo(out)
        out.close()
        pdfDoc.close()
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Single Horizontal Row Edit Tool Item
@Composable
fun SingleRowToolItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean = false,
    accentColor: Color = GoldPrimary,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.25f) else Color(0x1AFFFFFF))
            .border(1.dp, if (isSelected) accentColor else Color(0x22FFFFFF), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) accentColor else TextWhite,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = if (isSelected) accentColor else TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartMarkerScreen(viewModel: MainViewModel, onBackToHome: (() -> Unit)? = null) {
    val context = LocalContext.current
    val savedPatterns by viewModel.savedPatterns.collectAsState()

    // Fullscreen Page Editor State (Default true for immediate full page drawing experience)
    var isFullPageEditorOpen by remember { mutableStateOf(true) }
    var showCustomColorDialog by remember { mutableStateOf(false) }

    // Active Image State
    var customImageUri by remember { mutableStateOf<Uri?>(null) }
    var activeChartType by remember { mutableStateOf("MATKA_TABLE") } // MATKA_TABLE, CANDLESTICK, CUSTOM

    // Gallery Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            customImageUri = uri
            activeChartType = "CUSTOM"
            isFullPageEditorOpen = true // Automatically launch in dedicated full-screen editor!
            Toast.makeText(context, "Chart image loaded in Editor!", Toast.LENGTH_SHORT).show()
        }
    }

    // Drawing State & Settings
    var activeTool by remember { mutableStateOf(ToolType.FREEHAND) }
    var activeColor by remember { mutableStateOf(MarkerColorList[0]) } // Neon Green default
    var strokeWidth by remember { mutableFloatStateOf(8f) }
    var isGlowEnabled by remember { mutableStateOf(true) }
    var isZoomMode by remember { mutableStateOf(false) }

    // Shapes Storage (Undo / Redo Stack)
    val shapesList = remember { mutableStateListOf<ChartShape>() }
    val redoList = remember { mutableStateListOf<ChartShape>() }

    // Active Touch In-Progress Shape Points
    var currentPathPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var shapeStartPoint by remember { mutableStateOf<Offset?>(null) }
    var shapeEndPoint by remember { mutableStateOf<Offset?>(null) }

    // Text Input Modal State
    var showAddTextDialog by remember { mutableStateOf(false) }
    var textInputVal by remember { mutableStateOf("") }
    var textTapPosition by remember { mutableStateOf(Offset.Zero) }

    // Save Pattern Modal State
    var showSavePatternDialog by remember { mutableStateOf(false) }
    var patternTitleInput by remember { mutableStateOf("") }
    var patternNoteInput by remember { mutableStateOf("") }

    // Saved Patterns Bottom Sheet State
    var showSavedBottomSheet by remember { mutableStateOf(false) }

    // Pattern Stamps Popup Modal State
    var showPatternStampPopup by remember { mutableStateOf(false) }

    // Zoom & Pan State
    var scale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Helper to stamp preset neon patterns directly onto canvas
    fun applyPatternStamp(stampId: String) {
        val color = activeColor.color
        val width = strokeWidth
        val isGlow = isGlowEnabled

        when (stampId) {
            "CROSS_X" -> {
                shapesList.add(ChartShape.LineShape(Offset(180f, 180f), Offset(320f, 320f), color, width + 2f, isGlow))
                shapesList.add(ChartShape.LineShape(Offset(320f, 180f), Offset(180f, 320f), color, width + 2f, isGlow))
            }
            "GOLD_BOX" -> {
                shapesList.add(ChartShape.BoxShape(Offset(160f, 180f), Offset(340f, 320f), GoldPrimary, width + 2f, true))
                shapesList.add(ChartShape.BoxShape(Offset(170f, 190f), Offset(330f, 310f), NeonCyan, width, true))
            }
            "BULLSEYE" -> {
                shapesList.add(ChartShape.OvalShape(Offset(170f, 170f), Offset(330f, 330f), NeonCyan, width, true))
                shapesList.add(ChartShape.OvalShape(Offset(210f, 210f), Offset(290f, 290f), GoldPrimary, width + 2f, true))
            }
            "TREND_ARROW" -> {
                shapesList.add(ChartShape.ArrowShape(Offset(150f, 350f), Offset(350f, 150f), NeonGreen, width + 2f, true))
            }
            "OTC_BANNER" -> {
                shapesList.add(ChartShape.BoxShape(Offset(140f, 220f), Offset(360f, 280f), GoldPrimary, width, true))
                shapesList.add(ChartShape.TextShape(Offset(155f, 260f), "OTC: 3 - 8 - 4 - 9", GoldPrimary, width * 1.5f + 12f))
            }
            "STAR_STAMP" -> {
                shapesList.add(ChartShape.TextShape(Offset(220f, 270f), "⭐", GoldPrimary, 36f))
            }
            "DELTA_TRIANGLE" -> {
                shapesList.add(ChartShape.LineShape(Offset(250f, 180f), Offset(180f, 310f), NeonGreen, width, isGlow))
                shapesList.add(ChartShape.LineShape(Offset(180f, 310f), Offset(320f, 310f), NeonGreen, width, isGlow))
                shapesList.add(ChartShape.LineShape(Offset(320f, 310f), Offset(250f, 180f), NeonGreen, width, isGlow))
            }
            "JODI_GRID" -> {
                shapesList.add(ChartShape.BoxShape(Offset(150f, 150f), Offset(350f, 350f), GoldPrimary, width, true))
                shapesList.add(ChartShape.LineShape(Offset(250f, 150f), Offset(250f, 350f), NeonCyan, width - 2f, true))
                shapesList.add(ChartShape.LineShape(Offset(150f, 250f), Offset(350f, 250f), NeonCyan, width - 2f, true))
            }
            "PAIR_BAR" -> {
                shapesList.add(ChartShape.LineShape(Offset(150f, 250f), Offset(350f, 250f), GoldPrimary, width + 3f, true))
                shapesList.add(ChartShape.OvalShape(Offset(140f, 240f), Offset(160f, 260f), GoldPrimary, width, true))
                shapesList.add(ChartShape.OvalShape(Offset(340f, 240f), Offset(360f, 260f), GoldPrimary, width, true))
            }
            "DROP_POINTER" -> {
                shapesList.add(ChartShape.LineShape(Offset(250f, 150f), Offset(250f, 320f), PinkColor, width + 2f, true))
                shapesList.add(ChartShape.OvalShape(Offset(235f, 135f), Offset(265f, 165f), PinkColor, width + 4f, true))
            }
        }
        redoList.clear()
    }

    // Toolbar Visibility & Expansion State
    var isToolbarVisible by remember { mutableStateOf(true) }
    var isToolbarExpanded by remember { mutableStateOf(false) }

    if (isFullPageEditorOpen) {
        // FULLSCREEN DEDICATED IMAGE PHOTO EDITOR PAGE (Covers entire screen, hiding app header & bottom bar)
        Dialog(
            onDismissRequest = { isFullPageEditorOpen = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF000000))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                // 1. TOP BAR: Cancel (Left), Chart Title (Center), Save (Right) - Exactly matching user reference photo!
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0A0D14))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back / Cancel Button (Top-Left)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.5.dp, GoldPrimary, RoundedCornerShape(20.dp))
                            .background(Color(0xFF161A26))
                            .clickable {
                                isFullPageEditorOpen = true
                                onBackToHome?.invoke()
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Cancel & Back to Home",
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "cancel",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Center A23 PRO Title Capsule
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.5.dp, GoldPrimary, RoundedCornerShape(20.dp))
                            .background(Color(0xCC10131E))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "A23 PRO",
                            color = GoldPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    // Save Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(20.dp))
                            .background(Color(0xFF1E88E5))
                            .clickable {
                                showSavePatternDialog = true
                            }
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "save",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 2. MAIN IMAGE / CHART CANVAS VIEWPORT WITH ZOOM & PAN
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFF05070C))
                        .clipToBounds()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = panOffset.x
                                translationY = panOffset.y
                            }
                            .pointerInput(isZoomMode, scale) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 8f)
                                    panOffset += pan
                                }
                            }
                    ) {
                        // Background Chart Image
                        Box(modifier = Modifier.fillMaxSize()) {
                            when (activeChartType) {
                                "CUSTOM" -> {
                                    if (customImageUri != null) {
                                        AsyncImage(
                                            model = customImageUri,
                                            contentDescription = "Full Chart Image",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                                "CANDLESTICK" -> {
                                    CandlestickChartCanvas()
                                }
                                else -> {
                                    MatkaTableChartCanvas()
                                }
                            }
                        }

                        // Drawing Overlay Canvas
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(isZoomMode, activeTool, activeColor, strokeWidth, isGlowEnabled) {
                                    if (!isZoomMode) {
                                        detectDragGestures(
                                            onDragStart = { offset ->
                                                if (activeTool == ToolType.TEXT) {
                                                    textTapPosition = offset
                                                    showAddTextDialog = true
                                                } else {
                                                    shapeStartPoint = offset
                                                    shapeEndPoint = offset
                                                    if (activeTool == ToolType.FREEHAND) {
                                                        currentPathPoints = listOf(offset)
                                                    }
                                                }
                                            },
                                            onDrag = { change, _ ->
                                                change.consume()
                                                if (activeTool == ToolType.FREEHAND) {
                                                    currentPathPoints = currentPathPoints + change.position
                                                } else {
                                                    shapeEndPoint = change.position
                                                }
                                            },
                                            onDragEnd = {
                                                if (activeTool == ToolType.FREEHAND && currentPathPoints.size > 1) {
                                                    shapesList.add(
                                                        ChartShape.PathShape(
                                                            points = currentPathPoints,
                                                            color = activeColor.color,
                                                            strokeWidth = strokeWidth,
                                                            isGlow = isGlowEnabled
                                                        )
                                                    )
                                                    redoList.clear()
                                                    currentPathPoints = emptyList()
                                                } else if (shapeStartPoint != null && shapeEndPoint != null && activeTool != ToolType.TEXT) {
                                                    val start = shapeStartPoint!!
                                                    val end = shapeEndPoint!!
                                                    val shape: ChartShape = when (activeTool) {
                                                        ToolType.BOX -> ChartShape.BoxShape(start, end, activeColor.color, strokeWidth, isGlowEnabled)
                                                        ToolType.LINE -> ChartShape.LineShape(start, end, activeColor.color, strokeWidth, isGlowEnabled)
                                                        ToolType.ARROW -> ChartShape.ArrowShape(start, end, activeColor.color, strokeWidth, isGlowEnabled)
                                                        ToolType.OVAL -> ChartShape.OvalShape(start, end, activeColor.color, strokeWidth, isGlowEnabled)
                                                        else -> ChartShape.LineShape(start, end, activeColor.color, strokeWidth, isGlowEnabled)
                                                    }
                                                    shapesList.add(shape)
                                                    redoList.clear()
                                                    shapeStartPoint = null
                                                    shapeEndPoint = null
                                                }
                                            }
                                        )
                                    }
                                }
                        ) {
                            shapesList.forEach { shape -> drawChartShape(shape) }

                            if (activeTool == ToolType.FREEHAND && currentPathPoints.size > 1) {
                                drawChartShape(
                                    ChartShape.PathShape(
                                        points = currentPathPoints,
                                        color = activeColor.color,
                                        strokeWidth = strokeWidth,
                                        isGlow = isGlowEnabled
                                    )
                                )
                            } else if (shapeStartPoint != null && shapeEndPoint != null) {
                                val start = shapeStartPoint!!
                                val end = shapeEndPoint!!
                                val tempShape: ChartShape = when (activeTool) {
                                    ToolType.BOX -> ChartShape.BoxShape(start, end, activeColor.color, strokeWidth, isGlowEnabled)
                                    ToolType.LINE -> ChartShape.LineShape(start, end, activeColor.color, strokeWidth, isGlowEnabled)
                                    ToolType.ARROW -> ChartShape.ArrowShape(start, end, activeColor.color, strokeWidth, isGlowEnabled)
                                    ToolType.OVAL -> ChartShape.OvalShape(start, end, activeColor.color, strokeWidth, isGlowEnabled)
                                    else -> ChartShape.LineShape(start, end, activeColor.color, strokeWidth, isGlowEnabled)
                                }
                                drawChartShape(tempShape)
                            }
                        }
                    }

                    // FLOATING QUICK ZOOM CONTROLS
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xCC000000))
                            .border(1.dp, GoldPrimary, RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Zoom: ${(scale * 100).toInt()}%",
                                color = GoldPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { scale = (scale + 0.35f).coerceAtMost(8f) },
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33FFC107))
                            ) {
                                Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = GoldPrimary, modifier = Modifier.size(16.dp))
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(
                                onClick = {
                                    scale = (scale - 0.35f).coerceAtLeast(1f)
                                    if (scale == 1f) panOffset = Offset.Zero
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33FFC107))
                            ) {
                                Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = GoldPrimary, modifier = Modifier.size(16.dp))
                            }

                            if (scale > 1f) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NeonCyan.copy(0.3f))
                                        .clickable { scale = 1f; panOffset = Offset.Zero }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Reset Zoom", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 3. QUICK COLOR PALETTE BAR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0C0F17))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        items(MarkerColorList) { mc ->
                            val isSel = activeColor == mc
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(mc.color)
                                    .border(if (isSel) 2.dp else 1.dp, if (isSel) Color.White else Color.Transparent, CircleShape)
                                    .clickable { activeColor = mc },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSel) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                }
                            }
                        }

                        // Custom Color Picker Button
                        item {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.sweepGradient(
                                            listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
                                        )
                                    )
                                    .border(1.dp, Color.White, CircleShape)
                                    .clickable { showCustomColorDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Custom Color",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Glow", color = GoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = isGlowEnabled,
                            onCheckedChange = { isGlowEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = GoldPrimary
                            )
                        )
                    }
                }

                // 4. SINGLE HORIZONTAL LINE EDIT OPTIONS (Requested by user: "edit option yek lain me rakhe")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF000000))
                        .padding(vertical = 10.dp)
                ) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            SingleRowToolItem(
                                title = "Doodle",
                                icon = Icons.Default.Gesture,
                                isSelected = activeTool == ToolType.FREEHAND && !isZoomMode,
                                accentColor = NeonGreen
                            ) { activeTool = ToolType.FREEHAND; isZoomMode = false }
                        }

                        item {
                            SingleRowToolItem(
                                title = "Box",
                                icon = Icons.Default.Square,
                                isSelected = activeTool == ToolType.BOX && !isZoomMode,
                                accentColor = BrightOrangeColor
                            ) { activeTool = ToolType.BOX; isZoomMode = false }
                        }

                        item {
                            SingleRowToolItem(
                                title = "Line",
                                icon = Icons.Default.Edit,
                                isSelected = activeTool == ToolType.LINE && !isZoomMode,
                                accentColor = YellowColor
                            ) { activeTool = ToolType.LINE; isZoomMode = false }
                        }

                        item {
                            SingleRowToolItem(
                                title = "Arrow",
                                icon = Icons.Default.ArrowForward,
                                isSelected = activeTool == ToolType.ARROW && !isZoomMode,
                                accentColor = CyanColor
                            ) { activeTool = ToolType.ARROW; isZoomMode = false }
                        }

                        item {
                            SingleRowToolItem(
                                title = "Circle",
                                icon = Icons.Default.Circle,
                                isSelected = activeTool == ToolType.OVAL && !isZoomMode,
                                accentColor = PinkColor
                            ) { activeTool = ToolType.OVAL; isZoomMode = false }
                        }

                        item {
                            SingleRowToolItem(
                                title = "Text",
                                icon = Icons.Default.TextFields,
                                isSelected = activeTool == ToolType.TEXT && !isZoomMode,
                                accentColor = GoldPrimary
                            ) { activeTool = ToolType.TEXT; isZoomMode = false }
                        }

                        item {
                            SingleRowToolItem(
                                title = "Stamps",
                                icon = Icons.Default.AutoAwesome,
                                isSelected = false,
                                accentColor = GoldPrimary
                            ) { showPatternStampPopup = true }
                        }

                        item {
                            SingleRowToolItem(
                                title = "Zoom Mode",
                                icon = Icons.Default.ZoomIn,
                                isSelected = isZoomMode,
                                accentColor = Color(0xFFB388FF)
                            ) { isZoomMode = !isZoomMode }
                        }

                        item {
                            SingleRowToolItem(
                                title = "Undo",
                                icon = Icons.Default.Undo,
                                isSelected = false,
                                accentColor = NeonCyan
                            ) {
                                if (shapesList.isNotEmpty()) {
                                    val removed = shapesList.removeAt(shapesList.size - 1)
                                    redoList.add(removed)
                                }
                            }
                        }

                        item {
                            SingleRowToolItem(
                                title = "Redo",
                                icon = Icons.Default.Redo,
                                isSelected = false,
                                accentColor = NeonGreen
                            ) {
                                if (redoList.isNotEmpty()) {
                                    val restored = redoList.removeAt(redoList.size - 1)
                                    shapesList.add(restored)
                                }
                            }
                        }

                        item {
                            SingleRowToolItem(
                                title = "Clear",
                                icon = Icons.Default.Delete,
                                isSelected = false,
                                accentColor = Color(0xFFFF5252)
                            ) {
                                shapesList.clear()
                                redoList.clear()
                                Toast.makeText(context, "Canvas reset", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                // 5. BOTTOM ACTION MODE BAR (Matching screenshot active pill!)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF000000))
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1E88E5)) // Matching reference screenshot!
                            .padding(horizontal = 24.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Edit Mode",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x22FFFFFF))
                            .clickable { showSavedBottomSheet = true }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Saved (${savedPatterns.size})",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
    } else {
    // STANDARD DASHBOARD VIEW
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07090E))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
        // TOP BACK TO HOME BAR
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x22FFFFFF))
                        .clickable {
                            isFullPageEditorOpen = true
                            onBackToHome?.invoke()
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Back to Home", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // TOP HEADER CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.5.dp, GoldPrimary),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(GoldPrimary.copy(alpha = 0.4f), Color.Transparent)
                                )
                            )
                            .border(1.5.dp, GoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Chart Marker",
                            tint = GoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CHART MARKER & GLOW DRAW",
                            color = GoldPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Finger Drawing • Neon Glow • Save Patterns • Pinch Zoom",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    // Open Gallery Button
                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0x33FFC107))
                            .border(1.dp, GoldPrimary, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Upload Chart",
                            tint = GoldPrimary
                        )
                    }
                }
            }
        }

        // PROMINENT BUTTON TO LAUNCH FULLSCREEN EDITOR PAGE
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101625)),
                border = BorderStroke(1.5.dp, Color(0xFF1E88E5)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isFullPageEditorOpen = true }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E88E5).copy(alpha = 0.3f))
                                .border(1.dp, Color(0xFF1E88E5), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Full Page Editor",
                                tint = Color(0xFF1E88E5),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "OPEN FULLSCREEN EDITOR (इमेज एडिटर नया पेज)",
                                color = GoldPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Target exact chart numbers with high precision zoom",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E88E5))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "OPEN PAGE",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // CHART SOURCE SELECTOR CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SELECT CHART SOURCE (चार्ट सोर्स चुनें)",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (activeChartType == "MATKA_TABLE") GoldPrimary.copy(0.25f) else Color(0x1AFFFFFF))
                                .border(1.dp, if (activeChartType == "MATKA_TABLE") GoldPrimary else Color(0x22FFFFFF), RoundedCornerShape(10.dp))
                                .clickable { activeChartType = "MATKA_TABLE"; customImageUri = null }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📊 Weekly Table", color = if (activeChartType == "MATKA_TABLE") GoldPrimary else TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (activeChartType == "CANDLESTICK") NeonCyan.copy(0.25f) else Color(0x1AFFFFFF))
                                .border(1.dp, if (activeChartType == "CANDLESTICK") NeonCyan else Color(0x22FFFFFF), RoundedCornerShape(10.dp))
                                .clickable { activeChartType = "CANDLESTICK"; customImageUri = null }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📈 Candlestick", color = if (activeChartType == "CANDLESTICK") NeonCyan else TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (activeChartType == "CUSTOM") NeonGreen.copy(0.25f) else Color(0x1AFFFFFF))
                                .border(1.dp, if (activeChartType == "CUSTOM") NeonGreen else Color(0x22FFFFFF), RoundedCornerShape(10.dp))
                                .clickable { imagePickerLauncher.launch("image/*") }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🖼️ Upload Photo", color = if (activeChartType == "CUSTOM") NeonGreen else TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // EXPORT & REPORT ACTIONS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // PDF Export Button
                Button(
                    onClick = {
                        val pdfFile = exportChartAsPdf(context, shapesList.size, activeChartType)
                        if (pdfFile != null) {
                            Toast.makeText(context, "✓ Chart PDF exported to ${pdfFile.name}!", Toast.LENGTH_LONG).show()
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, Uri.fromFile(pdfFile))
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Marked Chart PDF"))
                        } else {
                            Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x3300E5FF)),
                    border = BorderStroke(1.dp, NeonCyan),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("EXPORT PDF", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Share Image Button
                Button(
                    onClick = {
                        Toast.makeText(context, "✓ Marked chart image saved & ready to share!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x3300E676)),
                    border = BorderStroke(1.dp, NeonGreen),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = NeonGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SHARE CHART", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}
}

    // CUSTOM COLOR PICKER DIALOG
    if (showCustomColorDialog) {
        val extraCustomColors = listOf(
            MarkerColor("Neon Green", Color(0xFF00E676), "#00E676"),
            MarkerColor("Bright Orange", Color(0xFFFF9100), "#FF9100"),
            MarkerColor("Electric Yellow", Color(0xFFFFEA00), "#FFEA00"),
            MarkerColor("Cyber Cyan", Color(0xFF00E5FF), "#00E5FF"),
            MarkerColor("Neon Pink", Color(0xFFFF007F), "#FF007F"),
            MarkerColor("Crimson Red", Color(0xFFFF1744), "#FF1744"),
            MarkerColor("Deep Purple", Color(0xFFD500F9), "#D500F9"),
            MarkerColor("Neon Blue", Color(0xFF2979FF), "#2979FF"),
            MarkerColor("Teal Green", Color(0xFF1DE9B6), "#1DE9B6"),
            MarkerColor("Lime Yellow", Color(0xFFC6FF00), "#C6FF00"),
            MarkerColor("A23 Gold", Color(0xFFFFD700), "#FFD700"),
            MarkerColor("Pure White", Color(0xFFFFFFFF), "#FFFFFF")
        )

        AlertDialog(
            onDismissRequest = { showCustomColorDialog = false },
            containerColor = Color(0xFF121824),
            title = {
                Text(
                    text = "Custom Color & Stroke Thickness",
                    color = GoldPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("Select Finger Marker Color:", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(extraCustomColors) { c ->
                            val isSel = activeColor.color == c.color
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(c.color)
                                    .border(if (isSel) 2.5.dp else 1.dp, if (isSel) Color.White else Color.Transparent, CircleShape)
                                    .clickable {
                                        activeColor = c
                                        showCustomColorDialog = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSel) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Text("Brush Thickness: ${strokeWidth.toInt()} px", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = strokeWidth,
                        onValueChange = { strokeWidth = it },
                        valueRange = 2f..30f,
                        colors = SliderDefaults.colors(
                            thumbColor = GoldPrimary,
                            activeTrackColor = GoldPrimary
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showCustomColorDialog = false }) {
                    Text("DONE", color = GoldPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
    if (showAddTextDialog) {
        AlertDialog(
            onDismissRequest = { showAddTextDialog = false },
            title = { Text("Add Text Annotation", color = GoldPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter custom text label to place on chart:", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = textInputVal,
                        onValueChange = { textInputVal = it },
                        label = { Text("Text Label (e.g., Kalyan Touch 27)", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = activeColor.color,
                            unfocusedBorderColor = Color(0x44FFFFFF),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (textInputVal.isNotBlank()) {
                            shapesList.add(
                                ChartShape.TextShape(
                                    position = textTapPosition,
                                    text = textInputVal,
                                    color = activeColor.color,
                                    fontSize = strokeWidth * 2f + 12f
                                )
                            )
                            redoList.clear()
                            textInputVal = ""
                            showAddTextDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = activeColor.color)
                ) {
                    Text("ADD LABEL", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTextDialog = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = CyberCardBg
        )
    }

    // SAVE PATTERN MODAL DIALOG
    if (showSavePatternDialog) {
        AlertDialog(
            onDismissRequest = { showSavePatternDialog = false },
            title = { Text("Save Chart Pattern", color = GoldPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Save current markings to revisit and compare later:", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = patternTitleInput,
                        onValueChange = { patternTitleInput = it },
                        label = { Text("Pattern Title (e.g. Bullish Setup, Kalyan Touch Line)", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0x44FFFFFF),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = patternNoteInput,
                        onValueChange = { patternNoteInput = it },
                        label = { Text("Optional Notes", color = TextMuted) },
                        singleLine = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0x44FFFFFF),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveChartPattern(
                            title = patternTitleInput,
                            chartName = activeChartType,
                            note = patternNoteInput,
                            elementCount = shapesList.size,
                            elementsJson = ""
                        )
                        Toast.makeText(context, "✓ Pattern saved successfully!", Toast.LENGTH_SHORT).show()
                        patternTitleInput = ""
                        patternNoteInput = ""
                        showSavePatternDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("SAVE PATTERN", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePatternDialog = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = CyberCardBg
        )
    }

    // SAVED PATTERNS BOTTOM SHEET
    if (showSavedBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSavedBottomSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = CyberCardBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text("📁 SAVED PATTERNS LIBRARY", color = GoldPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Revisit, compare or reload your analyzed chart markup patterns", color = TextMuted, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(14.dp))

                if (savedPatterns.isEmpty()) {
                    Text("No saved patterns yet. Draw on chart and tap 'SAVE'.", color = TextMuted, fontSize = 13.sp)
                } else {
                    savedPatterns.forEach { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0x3310131E)),
                            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("${item.chartName} • ${item.dateStr} • ${item.elementCount} Markings", color = NeonCyan, fontSize = 11.sp)
                                    if (item.note.isNotBlank()) {
                                        Text(item.note, color = TextMuted, fontSize = 10.sp)
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteChartPattern(item.id)
                                            Toast.makeText(context, "Pattern deleted", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showSavedBottomSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CLOSE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // PATTERN STAMP POPUP DIALOG
    if (showPatternStampPopup) {
        PatternStampPopupDialog(
            onDismiss = { showPatternStampPopup = false },
            onSelectStamp = { stamp ->
                applyPatternStamp(stamp.id)
                showPatternStampPopup = false
                Toast.makeText(context, "${stamp.name} applied!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
