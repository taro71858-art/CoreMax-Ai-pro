package com.example.ui.tools

import android.widget.Toast
import androidx.compose.foundation.layout.*
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
import com.example.data.models.Currency
import com.example.data.models.HistoryItem
import com.example.ui.components.ActionButtonsBar
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscountPromoScreen(
    currency: Currency,
    onSaveToHistory: (HistoryItem) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var priceText by remember { mutableStateOf("1000") }
    var cogsText by remember { mutableStateOf("400") }
    var discountText by remember { mutableStateOf("15") }

    val price = priceText.toDoubleOrNull() ?: 1000.0
    val cogs = cogsText.toDoubleOrNull() ?: 400.0
    val discountPercent = discountText.toDoubleOrNull() ?: 15.0

    val newPrice = price * (1.0 - discountPercent / 100.0)
    val oldMargin = if (price > 0) ((price - cogs) / price) * 100.0 else 0.0
    val newMargin = if (newPrice > 0) ((newPrice - cogs) / newPrice) * 100.0 else 0.0

    val oldProfitPerUnit = price - cogs
    val newProfitPerUnit = newPrice - cogs

    val requiredSalesMultiplier = if (newProfitPerUnit > 0) oldProfitPerUnit / newProfitPerUnit else Double.POSITIVE_INFINITY
    val isDiscountUnprofitable = newProfitPerUnit <= 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Скидки и акции", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Базовая цена (${currency.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = cogsText,
                        onValueChange = { cogsText = it },
                        label = { Text("Себестоимость (${currency.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = discountText,
                        onValueChange = { discountText = it },
                        label = { Text("Планируемая скидка (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Calculation Output
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDiscountUnprofitable) ErrorRose.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isDiscountUnprofitable) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRose)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "СКИДКА НЕВЫГОДНА! Вы торгуете себе в убыток.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ErrorRose
                            )
                        }
                    } else {
                        Text("Новая цена со скидкой: ${String.format("%,.0f", newPrice)} ${currency.symbol}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CyanBrand)
                        Text("Базовая маржа: ${String.format("%.1f", oldMargin)}% ➔ Новая маржа: ${String.format("%.1f", newMargin)}%", fontSize = 13.sp)

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        Text(
                            text = "Чтобы не потерять общую прибыль, вам нужно увеличить продажи в ${String.format("%.2f", requiredSalesMultiplier)} раза (+${String.format("%.0f", (requiredSalesMultiplier - 1.0) * 100)}% к объёму)!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessEmerald
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            val summary = "Скидка ${discountPercent}%: Новая цена ${newPrice} ${currency.symbol}, Новая маржа ${String.format("%.1f", newMargin)}%, Нужен рост продаж в ${String.format("%.2f", requiredSalesMultiplier)}х"

            ActionButtonsBar(
                onSave = {
                    onSaveToHistory(
                        HistoryItem(
                            title = "Расчет скидки $discountPercent%",
                            date = "Сегодня",
                            businessName = "Бизнес",
                            nicheName = "Общая",
                            currencyCode = currency.code,
                            currencySymbol = currency.symbol,
                            section = "Скидки",
                            details = summary
                        )
                    )
                    Toast.makeText(context, "Сохранено", Toast.LENGTH_SHORT).show()
                },
                copyText = summary,
                onPdf = { Toast.makeText(context, "Экспорт PDF готов", Toast.LENGTH_SHORT).show() }
            )
        }
    }
}
