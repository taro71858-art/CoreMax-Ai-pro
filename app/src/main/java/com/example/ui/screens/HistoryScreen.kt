package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.models.HistoryItem
import com.example.data.models.TransactionItem
import com.example.ui.components.AmountText
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    historyItems: List<HistoryItem>,
    transactions: List<TransactionItem>,
    isAmountsHidden: Boolean,
    onApplyItem: (HistoryItem) -> Unit,
    onDeleteItem: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onAddTransaction: (TransactionItem) -> Unit,
    onDeleteTransaction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Расчёты, 1: Операции
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyFavorites by remember { mutableStateOf(false) }

    // Side-by-side comparison modal state
    var compareItems by remember { mutableStateOf<List<HistoryItem>>(emptyList()) }
    var showCompareModal by remember { mutableStateOf(false) }

    // New transaction dialog state
    var showAddTxModal by remember { mutableStateOf(false) }
    var txAmount by remember { mutableStateOf("") }
    var txCategory by remember { mutableStateOf("Выручка") }
    var txNote by remember { mutableStateOf("") }
    var txIsIncome by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
    ) {
        item {
            Text(
                text = "История и операции",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Segmented Tabs: [Расчёты] | [Операции (ДДС)]
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        SoundManager.playClick(context)
                        selectedTab = 0
                    },
                    text = { Text("Расчёты (${historyItems.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        SoundManager.playClick(context)
                        selectedTab = 1
                    },
                    text = { Text("Операции (${transactions.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        // Tab 0: Saved Calculations
        if (selectedTab == 0) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Поиск по названию, тегам...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FilterChip(
                        selected = showOnlyFavorites,
                        onClick = {
                            SoundManager.playClick(context)
                            showOnlyFavorites = !showOnlyFavorites
                        },
                        label = { Text("★ Избранные", fontSize = 11.sp) }
                    )
                }
            }

            // Compare trigger info banner if items selected
            if (compareItems.size == 2) {
                item {
                    Button(
                        onClick = { showCompareModal = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanBrand, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.Compare, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Сравнить 2 выбранных расчёта")
                    }
                }
            }

            val filteredHistory = historyItems.filter { item ->
                (searchQuery.isBlank() || item.title.contains(searchQuery, ignoreCase = true) || item.tags.any { it.contains(searchQuery, ignoreCase = true) }) &&
                        (!showOnlyFavorites || item.isFavorite)
            }

            if (filteredHistory.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Сохранённых расчетов пока нет.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                }
            } else {
                items(filteredHistory) { item ->
                    val isSelectedForCompare = compareItems.any { it.id == item.id }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelectedForCompare) CyanBrand.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = IndigoTag.copy(alpha = 0.2f),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = item.section,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IndigoTag,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = item.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = { onToggleFavorite(item.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Избранное",
                                        tint = if (item.isFavorite) GoldCrown else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Прибыль", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    AmountText(
                                        amount = item.netProfit,
                                        currencySymbol = item.currencySymbol,
                                        hideAmounts = isAmountsHidden,
                                        fontSize = 16.sp,
                                        color = if (item.netProfit >= 0) SuccessEmerald else ErrorRose
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Маржа", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "${String.format("%.1f", item.margin)}%",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanBrand
                                    )
                                }
                            }

                            if (item.details.isNotBlank()) {
                                Text(
                                    text = item.details,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }

                            // Card Actions Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        if (isSelectedForCompare) {
                                            compareItems = compareItems.filter { it.id != item.id }
                                        } else if (compareItems.size < 2) {
                                            compareItems = compareItems + item
                                        }
                                    }
                                ) {
                                    Text(if (isSelectedForCompare) "✓ Для сравнения" else "+ Сравнить", fontSize = 11.sp)
                                }

                                TextButton(onClick = { onApplyItem(item) }) {
                                    Text("Открыть", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanBrand)
                                }

                                IconButton(
                                    onClick = { onDeleteItem(item.id) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Удалить", tint = ErrorRose, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tab 1: Transactions
        if (selectedTab == 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val totalIncome = transactions.filter { it.isIncome }.sumOf { it.amount }
                    val totalExpense = transactions.filter { !it.isIncome }.sumOf { it.amount }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = SuccessEmerald.copy(alpha = 0.12f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Доходы", fontSize = 10.sp, color = SuccessEmerald, fontWeight = FontWeight.Bold)
                            AmountText(amount = totalIncome, currencySymbol = "₽", hideAmounts = isAmountsHidden, fontSize = 15.sp, color = SuccessEmerald)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = ErrorRose.copy(alpha = 0.12f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Расходы", fontSize = 10.sp, color = ErrorRose, fontWeight = FontWeight.Bold)
                            AmountText(amount = totalExpense, currencySymbol = "₽", hideAmounts = isAmountsHidden, fontSize = 15.sp, color = ErrorRose)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { showAddTxModal = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanBrand, contentColor = Color.Black),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить")
                    }
                }
            }

            items(transactions) { tx ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (tx.isIncome) SuccessEmerald.copy(alpha = 0.2f) else ErrorRose.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (tx.isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = if (tx.isIncome) SuccessEmerald else ErrorRose,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(tx.category, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("${tx.date} • ${tx.note}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        AmountText(
                            amount = tx.amount,
                            currencySymbol = "₽",
                            hideAmounts = isAmountsHidden,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (tx.isIncome) SuccessEmerald else ErrorRose,
                            showSign = true
                        )

                        IconButton(
                            onClick = { onDeleteTransaction(tx.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }

    // Modal Add Transaction
    if (showAddTxModal) {
        AlertDialog(
            onDismissRequest = { showAddTxModal = false },
            title = { Text("Новая операция") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = txIsIncome,
                            onClick = { txIsIncome = true },
                            label = { Text("+ Доход") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = !txIsIncome,
                            onClick = { txIsIncome = false },
                            label = { Text("- Расход") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = txAmount,
                        onValueChange = { txAmount = it },
                        label = { Text("Сумма (₽)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = txCategory,
                        onValueChange = { txCategory = it },
                        label = { Text("Категория") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = txNote,
                        onValueChange = { txNote = it },
                        label = { Text("Заметка") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = txAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            onAddTransaction(
                                TransactionItem(
                                    date = "Сегодня",
                                    amount = amt,
                                    isIncome = txIsIncome,
                                    category = txCategory,
                                    note = txNote
                                )
                            )
                            showAddTxModal = false
                            txAmount = ""
                            txNote = ""
                        }
                    }
                ) { Text("Сохранить") }
            },
            dismissButton = {
                TextButton(onClick = { showAddTxModal = false }) { Text("Отмена") }
            }
        )
    }

    // Compare Side by Side Modal
    if (showCompareModal && compareItems.size == 2) {
        val a = compareItems[0]
        val b = compareItems[1]

        AlertDialog(
            onDismissRequest = { showCompareModal = false },
            title = { Text("Сравнение расчётов") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(a.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CyanBrand)
                            Text("Выручка: ${String.format("%,.0f", a.revenue)} ${a.currencySymbol}", fontSize = 11.sp)
                            Text("Прибыль: ${String.format("%,.0f", a.netProfit)} ${a.currencySymbol}", fontSize = 11.sp, color = SuccessEmerald)
                            Text("Маржа: ${String.format("%.1f", a.margin)}%", fontSize = 11.sp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(b.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = IndigoTag)
                            Text("Выручка: ${String.format("%,.0f", b.revenue)} ${b.currencySymbol}", fontSize = 11.sp)
                            Text("Прибыль: ${String.format("%,.0f", b.netProfit)} ${b.currencySymbol}", fontSize = 11.sp, color = SuccessEmerald)
                            Text("Маржа: ${String.format("%.1f", b.margin)}%", fontSize = 11.sp)
                        }
                    }

                    HorizontalDivider()

                    val deltaProfit = b.netProfit - a.netProfit
                    Text(
                        text = "Дельта прибыли: ${if (deltaProfit >= 0) "+" else ""}${String.format("%,.0f", deltaProfit)} ₽",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (deltaProfit >= 0) SuccessEmerald else ErrorRose
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showCompareModal = false }) { Text("Закрыть") }
            }
        )
    }
}
