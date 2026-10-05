package com.example.ui.tools

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.data.models.Currency
import com.example.data.models.Invoice
import com.example.data.models.InvoiceItem
import com.example.ui.theme.CyanBrand
import com.example.ui.theme.SuccessEmerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicesScreen(
    invoices: List<Invoice>,
    currency: Currency,
    onAddInvoice: (Invoice) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var clientName by remember { mutableStateOf("") }
    var itemName by remember { mutableStateOf("") }
    var itemQty by remember { mutableStateOf("1") }
    var itemPrice by remember { mutableStateOf("1000") }

    val currentItems = remember { mutableStateListOf<InvoiceItem>() }

    val subtotal = currentItems.sumOf { it.total }
    val vatAmount = subtotal * 0.20
    val totalWithVat = subtotal + vatAmount

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Счета и сметы", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Создать новый счёт", fontSize = 15.sp, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = clientName,
                            onValueChange = { clientName = it },
                            label = { Text("Клиент / Компания") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = itemName,
                                onValueChange = { itemName = it },
                                label = { Text("Услуга / Товар") },
                                singleLine = true,
                                modifier = Modifier.weight(1.5f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedTextField(
                                value = itemQty,
                                onValueChange = { itemQty = it },
                                label = { Text("Кол-во") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(0.8f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedTextField(
                                value = itemPrice,
                                onValueChange = { itemPrice = it },
                                label = { Text("Цена") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Button(
                            onClick = {
                                val qty = itemQty.toDoubleOrNull() ?: 1.0
                                val pr = itemPrice.toDoubleOrNull() ?: 0.0
                                if (itemName.isNotBlank() && pr > 0) {
                                    currentItems.add(InvoiceItem(name = itemName, quantity = qty, unitPrice = pr))
                                    itemName = ""
                                    SoundManager.playClick(context)
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Добавить позицию")
                        }
                    }
                }
            }

            // Items in active invoice
            if (currentItems.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Позиции счёта:", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                            currentItems.forEachIndexed { idx, itm ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${idx + 1}. ${itm.name} (${itm.quantity.toInt()} x ${itm.unitPrice.toInt()} ₽)", fontSize = 12.sp, modifier = Modifier.weight(1f))
                                    Text("${itm.total.toInt()} ₽", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyanBrand)
                                }
                            }

                            HorizontalDivider()

                            Text("Итого с НДС (20%): ${String.format("%,.0f", totalWithVat)} ${currency.symbol}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SuccessEmerald)

                            Button(
                                onClick = {
                                    if (clientName.isNotBlank()) {
                                        val inv = Invoice(
                                            clientName = clientName,
                                            date = "Сегодня",
                                            dueDate = "Через 10 дней",
                                            items = currentItems.toList()
                                        )
                                        onAddInvoice(inv)
                                        currentItems.clear()
                                        clientName = ""
                                        SoundManager.playSuccess(context)
                                        Toast.makeText(context, "Счёт выставлен и сохранён!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanBrand, contentColor = androidx.compose.ui.graphics.Color.Black)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Выставить счёт и скачать PDF")
                            }
                        }
                    }
                }
            }

            // History of invoices
            item {
                Text("Ранее выставленные счета:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            items(invoices) { inv ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${inv.id} • ${inv.clientName}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("${inv.date} • ${inv.items.size} поз.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Text("${String.format("%,.0f", inv.totalWithVat)} ${currency.symbol}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SuccessEmerald)
                    }
                }
            }
        }
    }
}
