package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.ui.theme.CyanBrand
import com.example.ui.theme.SuccessEmerald

@Composable
fun AmountText(
    amount: Double,
    currencySymbol: String,
    hideAmounts: Boolean,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = MaterialTheme.colorScheme.onSurface,
    showSign: Boolean = false
) {
    val context = LocalContext.current
    val displayText = if (hideAmounts) {
        "*** $currencySymbol"
    } else {
        val sign = if (showSign && amount > 0) "+" else ""
        "$sign${String.format("%,.0f", amount).replace(',', ' ')} $currencySymbol"
    }

    AnimatedContent(
        targetState = displayText,
        label = "AmountAnim",
        modifier = modifier
    ) { text ->
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = color,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActionButtonsBar(
    onSave: () -> Unit,
    copyText: String,
    onPdf: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    val context = LocalContext.current
    var showCopyMenu by remember { mutableStateOf(false) }

    fun copyToClipboard(text: String, label: String = "Скопировано") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("CoreMaX Report", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, label, Toast.LENGTH_SHORT).show()
        SoundManager.playSuccess(context)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Save Button
            Button(
                onClick = {
                    if (isEnabled) {
                        SoundManager.playCoins(context)
                        onSave()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanBrand,
                    contentColor = Color.Black
                ),
                enabled = isEnabled
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkAdd,
                    contentDescription = "Сохранить",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Сохранить", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            // Copy Button with long-click
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = {
                        if (isEnabled) {
                            copyToClipboard(copyText, "Скопировано в буфер")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .combinedClickable(
                            onClick = {
                                if (isEnabled) copyToClipboard(copyText, "Скопировано в буфер")
                            },
                            onLongClick = {
                                if (isEnabled) {
                                    SoundManager.playClick(context)
                                    showCopyMenu = true
                                }
                            }
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    enabled = isEnabled
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Копировать",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Копировать", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                DropdownMenu(
                    expanded = showCopyMenu,
                    onDismissRequest = { showCopyMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Копировать весь отчёт") },
                        onClick = {
                            showCopyMenu = false
                            copyToClipboard(copyText, "Весь отчёт скопирован")
                        },
                        leadingIcon = { Icon(Icons.Default.CopyAll, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Только цифры") },
                        onClick = {
                            showCopyMenu = false
                            val numbersOnly = copyText.lines()
                                .filter { it.contains(":") }
                                .joinToString("\n") { it.substringAfter(":").trim() }
                            copyToClipboard(numbersOnly, "Скопированы только цифры")
                        },
                        leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Как таблицу для Excel") },
                        onClick = {
                            showCopyMenu = false
                            val tableFormat = copyText.lines()
                                .joinToString("\n") { it.replace(":", "\t") }
                            copyToClipboard(tableFormat, "Скопировано для таблицы")
                        },
                        leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null) }
                    )
                }
            }

            // PDF Button
            FilledTonalButton(
                onClick = {
                    if (isEnabled) {
                        SoundManager.playClick(context)
                        onPdf()
                    }
                },
                modifier = Modifier
                    .weight(0.9f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                enabled = isEnabled
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "PDF",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("PDF", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
