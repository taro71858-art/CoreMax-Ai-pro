package com.example.ui.tools

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.ui.theme.CyanBrand
import com.example.ui.theme.GoldCrown

@Composable
fun OnboardingTourDialog(
    onDismiss: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var userLevel by remember { mutableStateOf("Новичок") }
    var userGoal by remember { mutableStateOf("Прибыль") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Explore, contentDescription = null, tint = CyanBrand)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Добро пожаловать в CoreMaX AI!", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (step) {
                    0 -> {
                        Text("Шаг 1 из 3: Выберите ваш уровень в бизнесе:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        listOf("Новичок (только запускаюсь)", "Уверенный (есть продажи)", "Профи (масштабирование)").forEach { level ->
                            FilterChip(
                                selected = userLevel == level,
                                onClick = { userLevel = level },
                                label = { Text(level) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    1 -> {
                        Text("Шаг 2 из 3: Какая у вас главная цель?", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        listOf("Рост чистой прибыли и маржи", "Наведение порядка в учёте (ДДС)", "ИИ-автоматизация и тексты", "Поиск причин убытков").forEach { goal ->
                            FilterChip(
                                selected = userGoal == goal,
                                onClick = { userGoal = goal },
                                label = { Text(goal) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    2 -> {
                        Text("Шаг 3 из 3: Всё готово! 🎉", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GoldCrown)
                        Text(
                            text = "• Нижняя панель содержит ровно 5 основных разделов: Анализ, Что если, Советник, Валюты и История.\n• В левом меню «три полоски» доступны 24 инструмента.\n• Справа в шапке доступен выбор ниши и скрытие сумм.\n• Значок Instagram ведет на профиль компании @tojprogs.",
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step < 2) step++ else onDismiss()
                }
            ) {
                Text(if (step < 2) "Далее" else "Начать работу")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Пропустить")
            }
        }
    )
}
