package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.data.models.FinancialAnalysisResult
import com.example.data.models.FirstStepsChecklist
import com.example.ui.theme.*

@Composable
fun HealthIndexWidget(
    result: FinancialAnalysisResult,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val score = result.healthScore
    val ratingColor = when {
        score >= 80 -> SuccessEmerald
        score >= 60 -> CyanBrand
        score >= 40 -> WarningAmber
        else -> ErrorRose
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Gauge
                Box(
                    modifier = Modifier.size(68.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = Color.DarkGray.copy(alpha = 0.3f),
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = ratingColor,
                            startAngle = 135f,
                            sweepAngle = (score / 100f) * 270f,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$score",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "/100",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Индекс здоровья бизнеса",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Подробнее",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = result.healthRating.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = ratingColor
                    )
                    Text(
                        text = result.healthRating.level,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    Text(
                        text = "Факторы оценки устойчивости:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyanBrand
                    )

                    result.healthBreakdown.forEach { factor ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (factor.isPositive) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (factor.isPositive) SuccessEmerald else WarningAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = factor.name,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = factor.status,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (factor.isPositive) SuccessEmerald else WarningAmber
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FirstStepsWidget(
    checklist: FirstStepsChecklist,
    onStepClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isDismissed by remember { mutableStateOf(false) }

    if (checklist.isAllDone && isDismissed) return

    val steps = listOf(
        Triple("Ниша и валюта выбраны", checklist.nicheSelected && checklist.currencySelected, "niche"),
        Triple("Первый расчет прибыли", checklist.firstCalcDone, "calc"),
        Triple("Конвертация в калькуляторе валют", checklist.currencyConverted, "currencies"),
        Triple("Выставление первого счета", checklist.invoiceCreated, "invoice"),
        Triple("Вопрос ИИ-советнику", checklist.advisorAsked, "advisor"),
        Triple("Урок в Академии ИИ", checklist.firstLessonDone, "academy")
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (checklist.isAllDone) GoldCrown.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = if (checklist.isAllDone) androidx.compose.foundation.BorderStroke(1.5.dp, GoldCrown) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (checklist.isAllDone) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = "Золотая корона",
                        tint = GoldCrown,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Первые шаги пройдены! 👑",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldCrown
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { isDismissed = true }) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = GoldCrown)
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.PlaylistAddCheck,
                        contentDescription = "Чек-лист",
                        tint = CyanBrand,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Чек-лист «Первые шаги»",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${checklist.completedCount}/7",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBrand
                    )
                }
            }

            LinearProgressIndicator(
                progress = { checklist.completedCount / 7f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(6.dp),
                color = if (checklist.isAllDone) GoldCrown else CyanBrand,
                trackColor = MaterialTheme.colorScheme.surface
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                steps.forEach { (title, done, tag) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                SoundManager.playClick(context)
                                onStepClick(tag)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (done) SuccessEmerald.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (done) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = SuccessEmerald,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            color = if (done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (done) FontWeight.Normal else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
