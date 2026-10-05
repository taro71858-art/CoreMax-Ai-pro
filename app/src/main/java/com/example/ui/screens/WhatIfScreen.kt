package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.data.models.*
import com.example.pdf.PdfReportGenerator
import com.example.ui.components.ActionButtonsBar
import com.example.ui.components.AmountText
import com.example.ui.theme.*

@Composable
fun WhatIfScreen(
    baseInputs: FinancialInputs,
    niche: Niche,
    currency: Currency,
    isAmountsHidden: Boolean,
    onSaveToHistory: (HistoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Sliders states (-50..+50 etc.)
    var priceDeltaPercent by remember { mutableFloatStateOf(0f) }
    var volumeDeltaPercent by remember { mutableFloatStateOf(0f) }
    var cogsDeltaPercent by remember { mutableFloatStateOf(0f) }
    var opexDeltaPercent by remember { mutableFloatStateOf(0f) }

    // Preset buttons
    fun applyPreset(preset: String) {
        SoundManager.playSwitch(context)
        when (preset) {
            "pessimistic" -> {
                priceDeltaPercent = -15f
                volumeDeltaPercent = -20f
                cogsDeltaPercent = 10f
                opexDeltaPercent = 10f
            }
            "realistic" -> {
                priceDeltaPercent = 0f
                volumeDeltaPercent = 0f
                cogsDeltaPercent = 0f
                opexDeltaPercent = 0f
            }
            "optimistic" -> {
                priceDeltaPercent = 15f
                volumeDeltaPercent = 25f
                cogsDeltaPercent = -5f
                opexDeltaPercent = -5f
            }
        }
    }

    // Instant Recalculation
    val baseRev = baseInputs.revenue
    val baseCogs = baseInputs.cogs
    val baseOpex = baseInputs.opex
    val taxRate = baseInputs.taxRate

    val newRevenue = baseRev * (1f + priceDeltaPercent / 100f) * (1f + volumeDeltaPercent / 100f)
    val newCogs = baseCogs * (1f + volumeDeltaPercent / 100f) * (1f + cogsDeltaPercent / 100f)
    val newOpex = baseOpex * (1f + opexDeltaPercent / 100f)
    val newGrossProfit = newRevenue - newCogs
    val newTaxes = newRevenue * (taxRate / 100f)
    val newNetProfit = newGrossProfit - newOpex - newTaxes
    val newMargin = if (newRevenue > 0) (newNetProfit / newRevenue) * 100.0 else 0.0

    val baseNetProfit = (baseRev - baseCogs) - baseOpex - (baseRev * taxRate / 100f)
    val profitDeltaAmount = newNetProfit - baseNetProfit
    val profitDeltaPercent = if (baseNetProfit != 0.0) ((newNetProfit - baseNetProfit) / Math.abs(baseNetProfit)) * 100.0 else 0.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
    ) {
        item {
            Text(
                text = "Сценарное моделирование «Что если»",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Мгновенно оцените, как изменения цены, потока клиентов и затрат повлияют на вашу чистую прибыль.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Scenario Preset Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { applyPreset("pessimistic") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ErrorRose
                    )
                ) {
                    Text("Пессимист.", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { applyPreset("realistic") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanBrand,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Базовый", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { applyPreset("optimistic") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SuccessEmerald
                    )
                ) {
                    Text("Оптимист.", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Sliders Card
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Slider 1: Price
                    SensitivitySlider(
                        title = "Изменение цены продажи",
                        value = priceDeltaPercent,
                        range = -50f..50f,
                        color = CyanBrand,
                        onValueChange = { priceDeltaPercent = it }
                    )

                    // Slider 2: Sales Volume
                    SensitivitySlider(
                        title = "Объём продаж / число гостей",
                        value = volumeDeltaPercent,
                        range = -50f..100f,
                        color = IndigoTag,
                        onValueChange = { volumeDeltaPercent = it }
                    )

                    // Slider 3: COGS / Raw materials
                    SensitivitySlider(
                        title = "Себестоимость / закупка",
                        value = cogsDeltaPercent,
                        range = -40f..50f,
                        color = Color(0xFFF97316),
                        onValueChange = { cogsDeltaPercent = it }
                    )

                    // Slider 4: Fixed OPEX
                    SensitivitySlider(
                        title = "Постоянные расходы (Аренда, ФОТ)",
                        value = opexDeltaPercent,
                        range = -40f..50f,
                        color = WarningAmber,
                        onValueChange = { opexDeltaPercent = it }
                    )
                }
            }
        }

        // Instant Results Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Результат моделирования",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ПРОГНОЗНАЯ ПРИБЫЛЬ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            AmountText(
                                amount = newNetProfit,
                                currencySymbol = currency.symbol,
                                hideAmounts = isAmountsHidden,
                                fontSize = 26.sp,
                                color = if (newNetProfit >= 0) SuccessEmerald else ErrorRose
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (profitDeltaAmount >= 0) SuccessEmerald.copy(alpha = 0.15f) else ErrorRose.copy(alpha = 0.15f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (profitDeltaAmount >= 0) "+${String.format("%.0f", profitDeltaPercent)}%" else "${String.format("%.0f", profitDeltaPercent)}%",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (profitDeltaAmount >= 0) SuccessEmerald else ErrorRose
                                )
                                Text(
                                    text = "к факту",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Прогноз выручки", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            AmountText(
                                amount = newRevenue,
                                currencySymbol = currency.symbol,
                                hideAmounts = isAmountsHidden,
                                fontSize = 16.sp
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Новая маржинальность", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${String.format("%.1f", newMargin)}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (newMargin >= niche.marginNormLow) SuccessEmerald else WarningAmber
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons
        item {
            val summary = """
Сценарный отчёт «Что если» (CoreMaX AI)
Ниша: ${niche.name}
Цена: ${String.format("%+.0f", priceDeltaPercent)}%
Объем продаж: ${String.format("%+.0f", volumeDeltaPercent)}%
Закупка: ${String.format("%+.0f", cogsDeltaPercent)}%
OPEX: ${String.format("%+.0f", opexDeltaPercent)}%
Новая выручка: ${String.format("%,.0f", newRevenue)} ${currency.symbol}
Новая прибыль: ${String.format("%,.0f", newNetProfit)} ${currency.symbol} (Δ ${String.format("%+.0f", profitDeltaPercent)}%)
Новая маржа: ${String.format("%.1f", newMargin)}%
Instagram: @tojprogs
            """.trimIndent()

            ActionButtonsBar(
                onSave = {
                    val item = HistoryItem(
                        title = "Сценарий: ${String.format("%+.0f", priceDeltaPercent)}% цена, ${String.format("%+.0f", volumeDeltaPercent)}% объем",
                        date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                        businessName = "Бизнес",
                        nicheName = niche.name,
                        currencyCode = currency.code,
                        currencySymbol = currency.symbol,
                        section = "Что если",
                        revenue = newRevenue,
                        netProfit = newNetProfit,
                        margin = newMargin,
                        details = summary,
                        tags = listOf("что_если", "сценарий")
                    )
                    onSaveToHistory(item)
                    Toast.makeText(context, "Сценарий сохранён в историю!", Toast.LENGTH_SHORT).show()
                },
                copyText = summary,
                onPdf = {
                    val tempResult = FinancialCalculator.calculate(
                        FinancialInputs(newRevenue, newCogs, newOpex, taxRate),
                        niche
                    )
                    PdfReportGenerator.generateAnalysisReport(
                        context = context,
                        businessName = "Сценарий Что если",
                        niche = niche,
                        currencySymbol = currency.symbol,
                        result = tempResult,
                        hideAmounts = isAmountsHidden
                    ) { file ->
                        if (file != null) PdfReportGenerator.sharePdf(context, file)
                    }
                }
            )
        }
    }
}

@Composable
fun SensitivitySlider(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = "${if (value > 0) "+" else ""}${value.toInt()}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (value > 0) SuccessEmerald else if (value < 0) ErrorRose else MaterialTheme.colorScheme.onSurface
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}
