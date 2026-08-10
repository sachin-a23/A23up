package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.formula.BacktestEvaluation
import com.example.formula.WeeklyStats
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    fun generatePdfReport(
        context: Context,
        marketName: String,
        stats: WeeklyStats,
        evaluations: List<BacktestEvaluation>,
        formulaName: String = "OTC FORMULA"
    ): File? {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 width in pt
        val pageHeight = 842 // A4 height in pt

        val paint = Paint().apply {
            isAntiAlias = true
        }

        val rowsPerPage = 20
        val totalRecords = evaluations.size
        val totalPages = if (totalRecords == 0) 1 else ((totalRecords + rowsPerPage - 1) / rowsPerPage)

        var recordIndex = 0

        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        val currentDateStr = dateFormat.format(Date())

        for (pageNumber in 1..totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            // Crisp Clean High-Contrast Background
            canvas.drawColor(Color.parseColor("#0F172A")) // Modern Navy Dark canvas

            var currentY = 30f

            // Top Header Banner Box
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#1E293B")
            val headerRect = RectF(20f, currentY, (pageWidth - 20).toFloat(), currentY + 55f)
            canvas.drawRoundRect(headerRect, 8f, 8f, paint)

            paint.style = Paint.Style.STROKE
            paint.color = Color.parseColor("#FFC107") // Gold border
            paint.strokeWidth = 1.5f
            canvas.drawRoundRect(headerRect, 8f, 8f, paint)

            // Header Title
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#FFC107")
            paint.textSize = 15f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("A23 PRO - $formulaName REPORT", 32f, currentY + 24f, paint)

            // Header Sub-text
            paint.color = Color.parseColor("#38BDF8") // Light Blue
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Market: $marketName   |   Generated: $currentDateStr   |   Page $pageNumber of $totalPages", 32f, currentY + 44f, paint)

            currentY += 70f

            // Summary Stats Box (Only on Page 1)
            if (pageNumber == 1) {
                val summaryRect = RectF(20f, currentY, (pageWidth - 20).toFloat(), currentY + 65f)
                
                // Box Background
                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#1E293B")
                canvas.drawRoundRect(summaryRect, 10f, 10f, paint)

                // Box Border
                paint.style = Paint.Style.STROKE
                paint.color = Color.parseColor("#38BDF8")
                paint.strokeWidth = 1f
                canvas.drawRoundRect(summaryRect, 10f, 10f, paint)

                // Summary 4 Metric Blocks
                val boxWidth = (pageWidth - 60) / 4f

                // Block 1: Total Days
                drawSummaryPill(canvas, paint, 25f, currentY + 8f, boxWidth, 48f, "TOTAL EVAL", "${stats.totalEvaluated} Days", "#38BDF8", "#0F172A")
                // Block 2: Pass Days
                drawSummaryPill(canvas, paint, 25f + boxWidth + 8f, currentY + 8f, boxWidth, 48f, "PASS DAYS", "${stats.passDays} Days", "#22C55E", "#14532D")
                // Block 3: Fail Days
                drawSummaryPill(canvas, paint, 25f + (boxWidth + 8f) * 2, currentY + 8f, boxWidth, 48f, "FAIL DAYS", "${stats.failDays} Days", "#EF4444", "#7F1D1D")
                // Block 4: Accuracy %
                drawSummaryPill(canvas, paint, 25f + (boxWidth + 8f) * 3, currentY + 8f, boxWidth, 48f, "ACCURACY", "${String.format("%.1f", stats.accuracyPercentage)}%", "#FFC107", "#78350F")

                currentY += 78f
            }

            // Table Box Columns X Coordinates
            val colX0 = 20f
            val colX1 = 140f
            val colX2 = 230f
            val colX3 = 340f
            val colX4 = 470f
            val colX5 = (pageWidth - 20).toFloat()

            // Draw Table Header Row Box
            val tableHeaderRect = RectF(colX0, currentY, colX5, currentY + 28f)
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#334155")
            canvas.drawRoundRect(tableHeaderRect, 6f, 6f, paint)

            paint.style = Paint.Style.STROKE
            paint.color = Color.parseColor("#FFC107")
            paint.strokeWidth = 1f
            canvas.drawRoundRect(tableHeaderRect, 6f, 6f, paint)

            // Header Labels
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#FFC107")
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            canvas.drawText("DATE & DAY", colX0 + 8f, currentY + 18f, paint)
            canvas.drawText("OTC DIGITS", colX1 + 8f, currentY + 18f, paint)
            canvas.drawText("SUPER JODI", colX2 + 8f, currentY + 18f, paint)
            canvas.drawText("DECLARATION", colX3 + 8f, currentY + 18f, paint)
            canvas.drawText("STATUS", colX4 + 8f, currentY + 18f, paint)

            currentY += 32f

            // Record Rows (Boxed Cells)
            val rowsToDrawOnThisPage = minOf(rowsPerPage, totalRecords - recordIndex)

            for (i in 0 until rowsToDrawOnThisPage) {
                if (recordIndex >= evaluations.size) break
                val eval = evaluations[recordIndex]
                recordIndex++

                val rowTop = currentY
                val rowBottom = currentY + 26f
                val rowRect = RectF(colX0, rowTop, colX5, rowBottom)

                // Row Box Background (Alternating Fill)
                paint.style = Paint.Style.FILL
                paint.color = if (i % 2 == 0) Color.parseColor("#1E293B") else Color.parseColor("#0F172A")
                canvas.drawRect(rowRect, paint)

                // Outer Row Box Border
                paint.style = Paint.Style.STROKE
                paint.color = Color.parseColor("#334155")
                paint.strokeWidth = 0.8f
                canvas.drawRect(rowRect, paint)

                // Vertical Column Dividers inside Row Box
                canvas.drawLine(colX1, rowTop, colX1, rowBottom, paint)
                canvas.drawLine(colX2, rowTop, colX2, rowBottom, paint)
                canvas.drawLine(colX3, rowTop, colX3, rowBottom, paint)
                canvas.drawLine(colX4, rowTop, colX4, rowBottom, paint)

                // Row Values
                paint.style = Paint.Style.FILL
                paint.textSize = 9.5f
                val textY = rowTop + 17f

                // Date & Day
                paint.color = Color.WHITE
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                val dayShort = eval.record.dayOfWeek.take(3)
                canvas.drawText("${eval.record.date} ($dayShort)", colX0 + 6f, textY, paint)

                // OTC Digits Box Text
                paint.color = Color.parseColor("#FDE047") // Vibrant Yellow/Gold
                val otcText = eval.formulaResult?.otcFormatted ?: "N/A"
                canvas.drawText(otcText, colX1 + 6f, textY, paint)

                // Super Jodi
                paint.color = Color.WHITE
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                val superJodiText = eval.formulaResult?.superJodis ?: "N/A"
                canvas.drawText(superJodiText, colX2 + 6f, textY, paint)

                // Declaration Result (OPEN-JODI-CLOSE)
                paint.color = Color.parseColor("#38BDF8")
                val resText = "${eval.record.openPanel}-${eval.record.jodi}-${eval.record.closePanel}"
                canvas.drawText(resText, colX3 + 6f, textY, paint)

                // Status Badge Box
                val badgeRect = RectF(colX4 + 6f, rowTop + 3f, colX5 - 6f, rowBottom - 3f)
                if (eval.isPass) {
                    paint.style = Paint.Style.FILL
                    paint.color = Color.parseColor("#166534") // Dark Green Badge
                    canvas.drawRoundRect(badgeRect, 4f, 4f, paint)

                    paint.style = Paint.Style.STROKE
                    paint.color = Color.parseColor("#22C55E")
                    paint.strokeWidth = 1f
                    canvas.drawRoundRect(badgeRect, 4f, 4f, paint)

                    paint.style = Paint.Style.FILL
                    paint.color = Color.parseColor("#4ADE80")
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText("PASS  ✅", colX4 + 14f, textY, paint)
                } else {
                    paint.style = Paint.Style.FILL
                    paint.color = Color.parseColor("#991B1B") // Dark Red Badge
                    canvas.drawRoundRect(badgeRect, 4f, 4f, paint)

                    paint.style = Paint.Style.STROKE
                    paint.color = Color.parseColor("#EF4444")
                    paint.strokeWidth = 1f
                    canvas.drawRoundRect(badgeRect, 4f, 4f, paint)

                    paint.style = Paint.Style.FILL
                    paint.color = Color.parseColor("#FCA5A5")
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText("FAIL  ❌", colX4 + 14f, textY, paint)
                }

                currentY += 26f
            }

            // Footer
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#64748B")
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            canvas.drawText("Generated by A23 PRO Analysis Engine  •  Saved to Downloads Folder", 20f, 825f, paint)

            pdfDocument.finishPage(page)
        }

        // Save PDF directly to Device Public Downloads Folder
        val fileName = "A23_PRO_${marketName}_Report_${System.currentTimeMillis()}.pdf"
        val downloadsPublicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val gsmFolder = File(downloadsPublicDir, "A23_PRO_Reports")
        if (!gsmFolder.exists()) {
            gsmFolder.mkdirs()
        }

        var targetFile = File(gsmFolder, fileName)

        return try {
            val fos = FileOutputStream(targetFile)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()
            targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                // Fallback to internal app documents directory if public folder write blocked
                val fallbackFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
                val fos = FileOutputStream(fallbackFile)
                pdfDocument.writeTo(fos)
                pdfDocument.close()
                fos.close()
                fallbackFile
            } catch (ex: Exception) {
                ex.printStackTrace()
                pdfDocument.close()
                null
            }
        }
    }

    private fun drawSummaryPill(
        canvas: Canvas,
        paint: Paint,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        title: String,
        value: String,
        textColorHex: String,
        bgFillHex: String
    ) {
        val pillRect = RectF(x, y, x + width, y + height)

        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor(bgFillHex)
        canvas.drawRoundRect(pillRect, 6f, 6f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor(textColorHex)
        paint.strokeWidth = 1f
        canvas.drawRoundRect(pillRect, 6f, 6f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor(textColorHex)
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(title, x + 6f, y + 14f, paint)

        paint.textSize = 12f
        canvas.drawText(value, x + 6f, y + 36f, paint)
    }

    fun openPdfFile(context: Context, file: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(viewIntent, "Open OTC Report PDF"))
        } catch (e: Exception) {
            e.printStackTrace()
            sharePdfFile(context, file)
        }
    }

    fun sharePdfFile(context: Context, file: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share A23 PRO Report PDF"))
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val uri = Uri.fromFile(file)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share A23 PRO Report PDF"))
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}
