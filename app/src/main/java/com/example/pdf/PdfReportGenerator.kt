package com.example.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.models.FinancialAnalysisResult
import com.example.data.models.Niche
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    fun generateAnalysisReport(
        context: Context,
        businessName: String,
        niche: Niche,
        currencySymbol: String,
        result: FinancialAnalysisResult,
        hideAmounts: Boolean,
        onComplete: (File?) -> Unit
    ) {
        try {
            val doc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (72 dpi)
            val page = doc.startPage(pageInfo)
            val canvas = page.canvas

            // Always light background for printing
            canvas.drawColor(Color.WHITE)

            val paint = Paint().apply { isAntiAlias = true }

            // 1. Header with CoreMaX AI branding
            paint.color = Color.rgb(15, 23, 42) // Dark Slate
            paint.textSize = 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("БИЗНЕС-АНАЛИТИК • CoreMaX AI", 40f, 50f, paint)

            paint.color = Color.rgb(6, 182, 212) // Cyan line
            paint.strokeWidth = 3f
            canvas.drawLine(40f, 62f, 555f, 62f, paint)

            // Business info
            paint.color = Color.rgb(51, 65, 85)
            paint.textSize = 12f
            paint.typeface = Typeface.DEFAULT_BOLD
            canvas.drawText("Предприятие: $businessName", 40f, 85f, paint)
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("Ниша: ${niche.name} (${niche.category})", 40f, 102f, paint)

            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            canvas.drawText("Дата отчёта: $dateStr", 380f, 85f, paint)
            canvas.drawText("Индекс здоровья: ${result.healthScore}/100 (${result.healthRating.title})", 380f, 102f, paint)

            // Divider
            paint.color = Color.rgb(226, 232, 240)
            paint.strokeWidth = 1f
            canvas.drawLine(40f, 120f, 555f, 120f, paint)

            // 2. Financial Metrics Table
            paint.color = Color.rgb(15, 23, 42)
            paint.textSize = 14f
            paint.typeface = Typeface.DEFAULT_BOLD
            canvas.drawText("КЛЮЧЕВЫЕ ФИНАНСОВЫЕ ПОКАЗАТЕЛИ", 40f, 145f, paint)

            fun mask(amount: Double): String {
                return if (hideAmounts) "*** $currencySymbol" else "${String.format("%,.0f", amount)} $currencySymbol"
            }

            val tableYStart = 170f
            val rowHeight = 26f
            val rows = listOf(
                "Выручка (Общий оборот)" to mask(result.revenue),
                "Себестоимость (Переменные затраты)" to mask(result.cogs),
                "Постоянные расходы (Аренда, оклады, софт)" to mask(result.opex),
                "Налоги (${result.taxRate}%)" to mask(result.taxes),
                "Валовая прибыль" to mask(result.grossProfit),
                "ЧИСТАЯ ПРИБЫЛЬ" to mask(result.netProfit),
                "Чистая маржа" to "${String.format("%.1f", result.netMargin)}% (норма ниши ${niche.marginNormLow}–${niche.marginNormHigh}%)",
                "Наценка к закупке" to "${String.format("%.1f", result.markup)}%",
                "Точка безубыточности" to "${mask(result.breakEvenPoint)} (на ${result.breakEvenDays}-й день)"
            )

            var curY = tableYStart
            rows.forEachIndexed { i, row ->
                if (i % 2 == 1) {
                    paint.color = Color.rgb(248, 250, 252) // light zebra row
                    canvas.drawRect(40f, curY - 18f, 555f, curY + 6f, paint)
                }

                paint.color = if (row.first.startsWith("ЧИСТАЯ")) Color.rgb(16, 185, 129) else Color.rgb(51, 65, 85)
                paint.textSize = 11f
                paint.typeface = if (row.first.startsWith("ЧИСТАЯ")) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                canvas.drawText(row.first, 50f, curY, paint)

                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(row.second, 545f, curY, paint)
                paint.textAlign = Paint.Align.LEFT

                curY += rowHeight
            }

            // Divider
            paint.color = Color.rgb(226, 232, 240)
            canvas.drawLine(40f, curY + 10f, 555f, curY + 10f, paint)

            // 3. Diagnosis & Loss Cause Section
            curY += 35f
            paint.color = Color.rgb(15, 23, 42)
            paint.textSize = 14f
            paint.typeface = Typeface.DEFAULT_BOLD
            canvas.drawText("ДИАГНОСТИКА И АНАЛИЗ УТЕЧЕК", 40f, curY, paint)

            curY += 22f
            for (cause in result.lossCauses.take(2)) {
                paint.color = Color.rgb(239, 68, 68) // Red tag
                paint.textSize = 11f
                paint.typeface = Typeface.DEFAULT_BOLD
                canvas.drawText("• ${cause.title} [Угроза: ${cause.severity}]", 45f, curY, paint)
                curY += 16f

                paint.color = Color.rgb(71, 85, 105)
                paint.typeface = Typeface.DEFAULT
                canvas.drawText(cause.explanation.take(85), 55f, curY, paint)
                curY += 15f
                paint.color = Color.rgb(16, 185, 129)
                canvas.drawText("Рекомендация: " + cause.recommendation.take(80), 55f, curY, paint)
                curY += 22f
            }

            // 4. Strategic AI Tip
            curY += 10f
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawRoundRect(40f, curY, 555f, curY + 65f, 8f, 8f, paint)

            paint.color = Color.rgb(99, 102, 241) // Indigo
            paint.textSize = 11f
            paint.typeface = Typeface.DEFAULT_BOLD
            canvas.drawText("СОВЕТ ИИ-СОВЕТНИКА COREMAX ДЛЯ «${niche.name.uppercase()}»:", 50f, curY + 20f, paint)

            paint.color = Color.rgb(51, 65, 85)
            paint.typeface = Typeface.DEFAULT
            canvas.drawText(niche.promptAdvice.take(90), 50f, curY + 40f, paint)

            // 5. Footer with Instagram branding
            paint.color = Color.rgb(148, 163, 184)
            paint.textSize = 9f
            canvas.drawText("Сформировано в мобильном приложении «Бизнес-аналитик» • CoreMaX AI", 40f, 805f, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Instagram: @tojprogs", 555f, 805f, paint)

            doc.finishPage(page)

            // Write to file
            val fileDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val fileName = "Бизнес-аналитик_Анализ_${fileDate}.pdf"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            doc.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            doc.close()

            onComplete(file)
        } catch (_: Exception) {
            onComplete(null)
        }
    }

    fun sharePdf(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Поделиться отчётом PDF"))
        } catch (_: Exception) {
            // Fallback
        }
    }
}
