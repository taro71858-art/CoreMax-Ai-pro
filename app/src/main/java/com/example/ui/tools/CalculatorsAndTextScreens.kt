package com.example.ui.tools

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.CyanBrand
import com.example.ui.theme.SuccessEmerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorsScreen(
    currency: Currency,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var cogsText by remember { mutableStateOf("500") }
    var priceText by remember { mutableStateOf("900") }
    var cashText by remember { mutableStateOf("1500000") }
    var dailyBurnText by remember { mutableStateOf("30000") }

    val cogs = cogsText.toDoubleOrNull() ?: 500.0
    val price = priceText.toDoubleOrNull() ?: 900.0
    val markup = if (cogs > 0) ((price - cogs) / cogs) * 100.0 else 0.0

    val cash = cashText.toDoubleOrNull() ?: 1500000.0
    val burn = dailyBurnText.toDoubleOrNull() ?: 30000.0
    val runwayDays = if (burn > 0) (cash / burn).toInt() else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Финансовые калькуляторы", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Назад") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("1. Калькулятор наценки", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(value = cogsText, onValueChange = { cogsText = it }, label = { Text("Закупка (${currency.symbol})") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Продажа (${currency.symbol})") }, modifier = Modifier.fillMaxWidth())
                        Text("Расчитанная наценка: ${String.format("%.1f", markup)}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CyanBrand)
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("2. Запас денег (Runway)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(value = cashText, onValueChange = { cashText = it }, label = { Text("Остаток денег (${currency.symbol})") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = dailyBurnText, onValueChange = { dailyBurnText = it }, label = { Text("Расходы в день (${currency.symbol})") }, modifier = Modifier.fillMaxWidth())
                        Text("Денег хватит на: $runwayDays дней (${runwayDays / 30} месяцев)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SuccessEmerald)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextGeneratorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var topicText by remember { mutableStateOf("") }
    var generatedText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Генератор текстов ИИ", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Назад") } }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = topicText,
                onValueChange = { topicText = it },
                label = { Text("Опишите тему акции или новости...") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    generatedText = "🔥 Спецпредложение недели!\n\nТолько до воскресенья получите скидку 15% на наши услуги. Ждём вас в гости!\n\n📍 Ждём вас по адресу компании."
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Сгенерировать пост")
            }

            if (generatedText.isNotBlank()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(generatedText, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: AppThemeMode,
    isSoundEnabled: Boolean,
    isVibrationEnabled: Boolean,
    onSetTheme: (AppThemeMode) -> Unit,
    onSetSound: (Boolean) -> Unit,
    onSetVibration: (Boolean) -> Unit,
    onExportBackup: () -> String,
    onResetData: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showResetConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Оформление и сброс", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Назад") } }
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
            Text("Тема оформления:", fontWeight = FontWeight.Bold, fontSize = 14.sp)

            AppThemeMode.entries.forEach { mode ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = themeMode == mode, onClick = { onSetTheme(mode) })
                    Text(mode.name, fontSize = 13.sp)
                }
            }

            HorizontalDivider()

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Звуковые эффекты", modifier = Modifier.weight(1f))
                Switch(checked = isSoundEnabled, onCheckedChange = onSetSound)
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Вибрация (отклик)", modifier = Modifier.weight(1f))
                Switch(checked = isVibrationEnabled, onCheckedChange = onSetVibration)
            }

            HorizontalDivider()

            Button(
                onClick = {
                    val json = onExportBackup()
                    Toast.makeText(context, "Резервная копия скопирована!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CloudDownload, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Экспорт бэкапа в JSON")
            }

            OutlinedButton(
                onClick = { showResetConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Сброс данных к начальным")
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Сброс данных") },
            text = { Text("Вы уверены? Все сохранённые расчеты и истории будут сброшены к начальным примерам.") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetData()
                        showResetConfirm = false
                        Toast.makeText(context, "Данные сброшены", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Сбросить всё") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Отмена") }
            }
        )
    }
}
