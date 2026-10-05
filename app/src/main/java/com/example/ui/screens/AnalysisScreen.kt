package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.data.models.*
import com.example.pdf.PdfReportGenerator
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun AnalysisScreen(
    inputs: FinancialInputs,
    niche: Niche,
    currency: Currency,
    isAmountsHidden: Boolean,
    firstSteps: FirstStepsChecklist,
    onInputsChanged: (FinancialInputs) -> Unit,
    onSaveToHistory: (HistoryItem) -> Unit,
    onStepClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var revText by remember(inputs.revenue) { mutableStateOf(inputs.revenue.toLong().toString()) }
    var cogsText by remember(inputs.cogs) { mutableStateOf(inputs.cogs.toLong().toString()) }
    var opexText by remember(inputs.opex) { mutableStateOf(inputs.opex.toLong().toString()) }
    var taxText by remember(inputs.taxRate) { mutableStateOf(inputs.taxRate.toString()) }

    val analysisResult = remember(inputs, niche) {
        FinancialCalculator.calculate(inputs, niche)
    }

    fun syncInputs() {
        val r = revText.toDoubleOrNull() ?: inputs.revenue
        val c = cogsText.toDoubleOrNull() ?: inputs.cogs
        val o = opexText.toDoubleOrNull() ?: inputs.opex
        val t = taxText.toDoubleOrNull() ?: inputs.taxRate
        onInputsChanged(FinancialInputs(r, c, o, t))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
    ) {
        // Niche applied badge
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CyanBrand.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanBrand.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = CyanBrand, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ниша: ${niche.name}. Подставлены статьи расходов и ориентир маржи (${niche.marginNormLow}–${niche.marginNormHigh}%)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Checklist "First Steps" Widget
        item {
            FirstStepsWidget(
                checklist = firstSteps,
                onStepClick = onStepClick
            )
        }

        // Business Health Index Widget
        item {
            HealthIndexWidget(result = analysisResult)
        }

        // Financial Inputs Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Ввод финансовых данных",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 1. Revenue
                    OutlinedTextField(
                        value = revText,
                        onValueChange = {
                            revText = it
                            syncInputs()
                        },
                        label = { Text("Выручка за месяц (${currency.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            Text(currency.code, modifier = Modifier.padding(end = 12.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CyanBrand)
                        }
                    )

                    // 2. COGS (Direct costs)
                    OutlinedTextField(
                        value = cogsText,
                        onValueChange = {
                            cogsText = it
                            syncInputs()
                        },
                        label = { Text("Себестоимость / закупка (${currency.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            Text("${(niche.defaultCogsRatio * 100).toInt()}% в нише", modifier = Modifier.padding(end = 12.dp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    )

                    // 3. OPEX (Fixed expenses)
                    OutlinedTextField(
                        value = opexText,
                        onValueChange = {
                            opexText = it
                            syncInputs()
                        },
                        label = { Text("Постоянные расходы (Аренда, ФОТ) (${currency.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 4. Tax Rate %
                    OutlinedTextField(
                        value = taxText,
                        onValueChange = {
                            taxText = it
                            syncInputs()
                        },
                        label = { Text("Ставка налога (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            Text("%", modifier = Modifier.padding(end = 12.dp), fontWeight = FontWeight.Bold, color = CyanBrand)
                        }
                    )
                }
            }
        }

        // Revenue Structure Visual Bar
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Шкала структуры выручки",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress breakdown bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                    ) {
                        if (analysisResult.cogsShare > 0) {
                            Box(modifier = Modifier.weight(analysisResult.cogsShare.coerceAtLeast(0.01f)).fillMaxHeight().background(Color(0xFFF97316)))
                        }
                        if (analysisResult.opexShare > 0) {
                            Box(modifier = Modifier.weight(analysisResult.opexShare.coerceAtLeast(0.01f)).fillMaxHeight().background(Color(0xFF6366F1)))
                        }
                        if (analysisResult.taxShare > 0) {
                            Box(modifier = Modifier.weight(analysisResult.taxShare.coerceAtLeast(0.01f)).fillMaxHeight().background(Color(0xFFEAB308)))
                        }
                        if (analysisResult.profitShare > 0) {
                            Box(modifier = Modifier.weight(analysisResult.profitShare.coerceAtLeast(0.01f)).fillMaxHeight().background(SuccessEmerald))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LegendChip("Себест. ${(analysisResult.cogsShare * 100).toInt()}%", Color(0xFFF97316))
                        LegendChip("OPEX ${(analysisResult.opexShare * 100).toInt()}%", Color(0xFF6366F1))
                        LegendChip("Налог ${(analysisResult.taxShare * 100).toInt()}%", Color(0xFFEAB308))
                        LegendChip("Прибыль ${(analysisResult.profitShare * 100).toInt()}%", SuccessEmerald)
                    }
                }
            }
        }

        // Live Calculated Key Financial Metrics
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Net profit hero highlight
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ЧИСТАЯ ПРИБЫЛЬ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            AmountText(
                                amount = analysisResult.netProfit,
                                currencySymbol = currency.symbol,
                                hideAmounts = isAmountsHidden,
                                fontSize = 28.sp,
                                color = if (analysisResult.netProfit >= 0) SuccessEmerald else ErrorRose
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (analysisResult.netMargin >= niche.marginNormLow) SuccessEmerald.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "МАРЖА",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (analysisResult.netMargin >= niche.marginNormLow) SuccessEmerald else WarningAmber
                                )
                                Text(
                                    text = "${String.format("%.1f", analysisResult.netMargin)}%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (analysisResult.netMargin >= niche.marginNormLow) SuccessEmerald else WarningAmber
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // 4 Grid metrics
                    Row(modifier = Modifier.fillMaxWidth()) {
                        MetricSmallItem(
                            title = "Валовая прибыль",
                            amount = analysisResult.grossProfit,
                            currency = currency.symbol,
                            hideAmounts = isAmountsHidden,
                            modifier = Modifier.weight(1f)
                        )
                        MetricSmallItem(
                            title = "Наценка к базе",
                            valueString = "${String.format("%.1f", analysisResult.markup)}%",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        MetricSmallItem(
                            title = "Точка безубыточности",
                            amount = analysisResult.breakEvenPoint,
                            currency = currency.symbol,
                            hideAmounts = isAmountsHidden,
                            modifier = Modifier.weight(1f)
                        )
                        MetricSmallItem(
                            title = "Дней до окупаемости",
                            valueString = "${analysisResult.breakEvenDays} дн. в мес.",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Loss Cause Diagnosis in Plain Language
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FindInPage, contentDescription = null, tint = CyanBrand, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Причины убытков простым языком",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    analysisResult.lossCauses.forEach { cause ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = cause.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (cause.severity == "Высокая") ErrorRose else if (cause.severity == "Средняя") WarningAmber else SuccessEmerald,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = cause.severity,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cause.explanation,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "💡 " + cause.recommendation,
                                fontSize = 12.sp,
                                color = CyanBrand,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Mini 7-day profit chart
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "График прибыли по дням (пример)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val days = listOf("Пн" to 14000.0, "Вт" to 18000.0, "Ср" to 15500.0, "Чт" to 19200.0, "Пт" to 28500.0, "Сб" to 34000.0, "Вс" to 31000.0)
                    val maxVal = days.maxOf { it.second }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        days.forEach { (day, valAmount) ->
                            val heightRatio = (valAmount / maxVal).toFloat().coerceIn(0.1f, 1f)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .fillMaxHeight(heightRatio)
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(if (day == "Сб" || day == "Вс") CyanBrand else IndigoTag)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = day, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Tip of the Day from AI
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = GoldCrown, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Совет дня для «${niche.name}»:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldCrown
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = niche.promptAdvice,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Action Buttons: [Сохранить в историю], [Копировать], [PDF]
        item {
            val reportSummary = """
Отчёт Бизнес-аналитик (CoreMaX AI)
Ниша: ${niche.name}
Выручка: ${String.format("%,.0f", inputs.revenue)} ${currency.symbol}
Себестоимость: ${String.format("%,.0f", inputs.cogs)} ${currency.symbol}
OPEX: ${String.format("%,.0f", inputs.opex)} ${currency.symbol}
Чистая прибыль: ${String.format("%,.0f", analysisResult.netProfit)} ${currency.symbol}
Чистая маржа: ${String.format("%.1f", analysisResult.netMargin)}%
Точка безубыточности: ${String.format("%,.0f", analysisResult.breakEvenPoint)} ${currency.symbol}
Индекс здоровья: ${analysisResult.healthScore}/100
Instagram: @tojprogs
            """.trimIndent()

            ActionButtonsBar(
                onSave = {
                    val item = HistoryItem(
                        title = "Расчет: ${niche.name}",
                        date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                        businessName = "Бизнес",
                        nicheName = niche.name,
                        currencyCode = currency.code,
                        currencySymbol = currency.symbol,
                        section = "Анализ",
                        revenue = inputs.revenue,
                        netProfit = analysisResult.netProfit,
                        margin = analysisResult.netMargin,
                        details = "Выручка: ${inputs.revenue.toLong()}, Прибыль: ${analysisResult.netProfit.toLong()}, Маржа: ${String.format("%.1f", analysisResult.netMargin)}%",
                        tags = listOf("факт", niche.name.lowercase())
                    )
                    onSaveToHistory(item)
                    Toast.makeText(context, "Сохранено в историю!", Toast.LENGTH_SHORT).show()
                },
                copyText = reportSummary,
                onPdf = {
                    PdfReportGenerator.generateAnalysisReport(
                        context = context,
                        businessName = "Бизнес-аналитик",
                        niche = niche,
                        currencySymbol = currency.symbol,
                        result = analysisResult,
                        hideAmounts = isAmountsHidden
                    ) { file ->
                        if (file != null) {
                            PdfReportGenerator.sharePdf(context, file)
                        } else {
                            Toast.makeText(context, "Ошибка создания PDF", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun MetricSmallItem(
    title: String,
    modifier: Modifier = Modifier,
    amount: Double? = null,
    currency: String = "",
    valueString: String? = null,
    hideAmounts: Boolean = false
) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (amount != null) {
            AmountText(
                amount = amount,
                currencySymbol = currency,
                hideAmounts = hideAmounts,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        } else if (valueString != null) {
            Text(
                text = valueString,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun LegendChip(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
