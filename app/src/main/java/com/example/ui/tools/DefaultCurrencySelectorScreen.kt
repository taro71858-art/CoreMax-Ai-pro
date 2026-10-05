package com.example.ui.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Currency
import com.example.data.models.CurrencyCatalog
import com.example.data.models.CurrencyRegion
import com.example.ui.theme.CyanBrand
import com.example.ui.theme.GoldCrown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultCurrencySelectorScreen(
    currentCurrency: Currency,
    onSelectCurrency: (Currency, Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf<CurrencyRegion?>(null) }
    var pendingCurrency by remember { mutableStateOf<Currency?>(null) }
    var showRecalculateDialog by remember { mutableStateOf(false) }

    val filteredCurrencies = remember(searchQuery, selectedRegion) {
        CurrencyCatalog.allCurrencies.filter { c ->
            (searchQuery.isBlank() || c.code.contains(searchQuery, ignoreCase = true) || c.name.contains(searchQuery, ignoreCase = true) || c.symbol.contains(searchQuery, ignoreCase = true)) &&
                    (selectedRegion == null || c.region == selectedRegion)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Валюта по умолчанию", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 16.dp)
        ) {
            // Active Currency Preview Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.5.dp, GoldCrown),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(currentCurrency.flag, fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${currentCurrency.code} — ${currentCurrency.name}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Пример суммы: 1 234,56 ${currentCurrency.symbol}",
                            fontSize = 13.sp,
                            color = CyanBrand,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoldCrown, modifier = Modifier.size(24.dp))
                }
            }

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Поиск из 256+ валют (код, название, флаг)...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(14.dp)
            )

            // Category filter chips
            ScrollableTabRow(
                selectedTabIndex = selectedRegion?.ordinal ?: 0,
                edgePadding = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedRegion == null,
                    onClick = { selectedRegion = null },
                    text = { Text("Все", fontSize = 12.sp) }
                )
                CurrencyRegion.entries.forEach { region ->
                    Tab(
                        selected = selectedRegion == region,
                        onClick = { selectedRegion = region },
                        text = { Text(region.displayName, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Currencies Virtualized List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredCurrencies) { c ->
                    val isSelected = c.code == currentCurrency.code

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) GoldCrown.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = if (isSelected) BorderStroke(1.dp, GoldCrown) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!isSelected) {
                                    pendingCurrency = c
                                    showRecalculateDialog = true
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(c.flag, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = c.code,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) GoldCrown else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(c.symbol, fontSize = 12.sp, color = CyanBrand, fontWeight = FontWeight.Bold)
                                    if (c.isOutdated) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("(устаревшая)", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                                Text(
                                    text = "${c.name} • 1 USD = ${String.format("%.2f", c.rateToUsd)} ${c.code}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoldCrown, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Recalculate prompt dialog
    if (showRecalculateDialog && pendingCurrency != null) {
        val target = pendingCurrency!!

        AlertDialog(
            onDismissRequest = { showRecalculateDialog = false },
            title = { Text("Смена валюты учёта на ${target.code}") },
            text = {
                Text("Пересчитать существующие финансовые данные (выручку и расходы) по текущему курсу обмена (${currentCurrency.code} → ${target.code})?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSelectCurrency(target, true)
                        showRecalculateDialog = false
                        onBack()
                    }
                ) {
                    Text("Пересчитать")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onSelectCurrency(target, false)
                        showRecalculateDialog = false
                        onBack()
                    }
                ) {
                    Text("Оставить значения")
                }
            }
        )
    }
}
