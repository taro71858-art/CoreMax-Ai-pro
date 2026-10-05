package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.ai.ChatMessage
import com.example.ai.GeminiAdvisor
import com.example.audio.SoundManager
import com.example.data.models.*
import com.example.ui.theme.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun AdvisorScreen(
    niche: Niche,
    currency: Currency,
    inputs: FinancialInputs,
    onSaveToHistory: (HistoryItem) -> Unit,
    onAdvisorAsked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var useMyData by remember { mutableStateOf(true) }
    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var activeJob by remember { mutableStateOf<Job?>(null) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                isUser = false,
                text = "Здравствуйте! Я — ИИ-советник CoreMaX AI. Отвечу на любые ваши вопросы о финансах, бизнесе в нише «${niche.name}», привлечении клиентов, технологиях и работе с программой.",
                confidence = "высокая"
            )
        )
    }

    val quickQuestions = remember(niche) {
        listOf(
            "Что такое маржа и чем она отличается от наценки?",
            "Разбери прибыль и маржу по моим данным",
            "Как поднять маржинальность в нише «${niche.name}»?",
            "Что такое нейросеть простыми словами?",
            "Напиши продающий пост для соцсетей"
        )
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || isGenerating) return
        val userMsg = ChatMessage(isUser = true, text = text)
        messages.add(userMsg)
        inputText = ""
        isGenerating = true
        SoundManager.playClick(context)
        onAdvisorAsked()

        activeJob = coroutineScope.launch {
            // Scroll to bottom
            listState.animateScrollToItem(messages.size - 1)

            val botMsgPlaceholder = ChatMessage(isUser = false, text = "")
            messages.add(botMsgPlaceholder)
            val botIdx = messages.size - 1

            val response = GeminiAdvisor.askAdvisor(
                prompt = text,
                history = messages.dropLast(2),
                useAppData = useMyData,
                niche = niche,
                currencyCode = currency.code,
                inputs = inputs,
                onChunk = { chunk ->
                    messages[botIdx] = messages[botIdx].copy(text = chunk)
                }
            )

            messages[botIdx] = response
            isGenerating = false
            SoundManager.playSuccess(context)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun stopGeneration() {
        activeJob?.cancel()
        isGenerating = false
        SoundManager.playSwitch(context)
    }

    fun copyAnswer(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("AI Advice", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Скопировано", Toast.LENGTH_SHORT).show()
        SoundManager.playSuccess(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Top Toolbar: New Chat & "Использовать мои данные" toggle
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Switch: Use My Data
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { useMyData = !useMyData }
                ) {
                    Switch(
                        checked = useMyData,
                        onCheckedChange = { useMyData = it },
                        modifier = Modifier.height(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Использовать мои данные",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // New Chat Button
                TextButton(
                    onClick = {
                        SoundManager.playClick(context)
                        messages.clear()
                        messages.add(
                            ChatMessage(
                                isUser = false,
                                text = "Новый диалог начат. Чем могу помочь вашему бизнесу сегодня?",
                                confidence = "высокая"
                            )
                        )
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Новый чат", fontSize = 12.sp)
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
        ) {
            items(messages) { msg ->
                MessageBubble(
                    message = msg,
                    onCopy = { copyAnswer(msg.text) },
                    onSave = {
                        val item = HistoryItem(
                            title = "Ответ ИИ: " + msg.text.take(30) + "...",
                            date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                            businessName = "Бизнес",
                            nicheName = niche.name,
                            currencyCode = currency.code,
                            currencySymbol = currency.symbol,
                            section = "Советник",
                            details = msg.text,
                            tags = listOf("советник", "ии")
                        )
                        onSaveToHistory(item)
                        Toast.makeText(context, "Ответ сохранён в историю!", Toast.LENGTH_SHORT).show()
                    },
                    onSimplify = {
                        sendMessage("Объясни проще предыдущий ответ: " + msg.text.take(80))
                    }
                )
            }

            if (isGenerating) {
                item {
                    Row(
                        modifier = Modifier.padding(start = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = CyanBrand,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ИИ-советник формулирует мысль...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Quick Suggestion Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickQuestions) { q ->
                Surface(
                    onClick = { sendMessage(q) },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanBrand.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = q,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        maxLines = 1
                    )
                }
            }
        }

        // Input Box + Send / Stop Button
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Задайте любой вопрос...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                if (isGenerating) {
                    FilledIconButton(
                        onClick = { stopGeneration() },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = ErrorRose),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Остановить", tint = Color.White)
                    }
                } else {
                    FilledIconButton(
                        onClick = { sendMessage(inputText) },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = CyanBrand),
                        modifier = Modifier.size(46.dp),
                        enabled = inputText.isNotBlank()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Отправить", tint = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    onCopy: () -> Unit,
    onSave: () -> Unit,
    onSimplify: () -> Unit
) {
    val isUser = message.isUser

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            color = if (isUser) CyanBrand else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (!isUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = CyanBrand,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CoreMaX AI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanBrand
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• Уверенность: ${message.confidence}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    color = if (isUser) Color.Black else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }
        }

        // Action Buttons under AI answers
        if (!isUser && message.text.isNotBlank()) {
            Row(
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TextButton(
                    onClick = onCopy,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Копировать", fontSize = 10.sp)
                }

                TextButton(
                    onClick = onSave,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("В историю", fontSize = 10.sp)
                }

                TextButton(
                    onClick = onSimplify,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Пояснить проще", fontSize = 10.sp)
                }
            }
        }
    }
}
