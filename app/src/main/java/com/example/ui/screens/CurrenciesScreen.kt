package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.data.models.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CurrenciesScreen(
    defaultCurrency: Currency,
    onConverted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var fromCurrency by remember { mutableStateOf(CurrencyCatalog.getCurrency("USD")) }
    var toCurrency by remember { mutableStateOf(defaultCurrency) }
    var amountText by remember { mutableStateOf("100") }
    var useCustomRate by remember { mutableStateOf(false) }
    var customRateText by remember { mutableStateOf("") }

    val amount = amountText.toDoubleOrNull() ?: 100.0

    val effectiveRate = if (useCustomRate && (customRateText.toDoubleOrNull() ?: 0.0) > 0) {
        customRateText.toDoubleOrNull() ?: 1.0
    } else {
        CurrencyCatalog.convert(1.0, fromCurrency.code, toCurrency.code)
    }

    val convertedAmount = amount * effectiveRate

    val lastUpdated = remember {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
    }

    val quickAmounts = listOf(10, 50, 100, 500, 1000, 5000, 10000)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
    ) {
        item {
            Text(
                text = "Конвертер валют и курсы",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudDone, contentDescription = null, tint = SuccessEmerald, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Курсы обновлены: $lastUpdated (офлайн доступ)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Main Conversion Card
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // From Currency Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = {
                                amountText = it
                                onConverted()
                            },
                            label = { Text("Сумма к обмену") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // From Currency Selector
                        CurrencySelectorButton(
                            currency = fromCurrency,
                            onClick = {
                                val populars = CurrencyCatalog.allCurrencies.take(8)
                                val nextIdx = (populars.indexOf(fromCurrency) + 1) % populars.size
                                fromCurrency = populars[nextIdx]
                                SoundManager.playClick(context)
                                onConverted()
                            }
                        )
                    }

                    // Swap Button
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        FilledIconButton(
                            onClick = {
                                SoundManager.playSwitch(context)
                                val temp = fromCurrency
                                fromCurrency = toCurrency
                                toCurrency = temp
                                onConverted()
                            },
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = CyanBrand),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.Default.SwapVert, contentDescription = "Поменять местами", tint = Color.Black)
                        }
                    }

                    // To Currency Output Result
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Результат",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${String.format("%,.2f", convertedAmount)} ${toCurrency.symbol}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanBrand
                            )
                            Text(
                                text = "1 ${fromCurrency.code} = ${String.format("%.4f", effectiveRate)} ${toCurrency.code}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // To Currency Selector
                        CurrencySelectorButton(
                            currency = toCurrency,
                            onClick = {
                                val populars = CurrencyCatalog.allCurrencies.take(8)
                                val nextIdx = (populars.indexOf(toCurrency) + 1) % populars.size
                                toCurrency = populars[nextIdx]
                                SoundManager.playClick(context)
                                onConverted()
                            }
                        )
                    }

                    // Custom Rate Override Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = useCustomRate,
                            onCheckedChange = { useCustomRate = it }
                        )
                        Text(
                            text = "Свой курс обмена",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (useCustomRate) {
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = customRateText,
                                onValueChange = { customRateText = it },
                                placeholder = { Text(String.format("%.2f", effectiveRate), fontSize = 12.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Amounts
        item {
            Text(
                text = "Быстрые суммы (${fromCurrency.code}):",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(quickAmounts) { q ->
                    AssistChip(
                        onClick = {
                            SoundManager.playClick(context)
                            amountText = q.toString()
                            onConverted()
                        },
                        label = { Text("$q ${fromCurrency.symbol}", fontSize = 12.sp) }
                    )
                }
            }
        }

        // Popular Currencies Table
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Популярные курсы к 1 ${fromCurrency.code}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val sampleTargets = listOf("RUB", "USD", "EUR", "KZT", "UZS", "TJS", "TRY", "AED", "CNY", "BTC")
                    sampleTargets.filter { it != fromCurrency.code }.forEach { code ->
                        val target = CurrencyCatalog.getCurrency(code)
                        val rate = CurrencyCatalog.convert(1.0, fromCurrency.code, target.code)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    SoundManager.playClick(context)
                                    toCurrency = target
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(target.flag, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${target.code} (${target.name})",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${String.format("%.4f", rate)} ${target.symbol}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanBrand
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencySelectorButton(
    currency: Currency,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier.height(56.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(currency.flag, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(currency.code, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(currency.symbol, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = CyanBrand, modifier = Modifier.size(18.dp))
        }
    }
}
