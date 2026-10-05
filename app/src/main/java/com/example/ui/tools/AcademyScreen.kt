package com.example.ui.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.data.models.AcademyLesson
import com.example.data.models.StaticCatalogs
import com.example.ui.theme.CyanBrand
import com.example.ui.theme.GoldCrown
import com.example.ui.theme.SuccessEmerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademyScreen(
    onLessonCompleted: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedLesson by remember { mutableStateOf<AcademyLesson?>(null) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }

    val lessons = remember { StaticCatalogs.academyLessons }
    val completedCount = lessons.count { it.isCompleted }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Академия ИИ в бизнесе", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
            // Header Progress Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.School, contentDescription = null, tint = CyanBrand, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Прогресс обучения: $completedCount из ${lessons.size} уроков", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        LinearProgressIndicator(
                            progress = { completedCount / lessons.size.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                                .height(6.dp),
                            color = CyanBrand,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                    }
                }
            }

            // Lessons List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(lessons) { lesson ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            selectedLesson = lesson
                            selectedOption = null
                            isSubmitted = false
                            SoundManager.playClick(context)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (lesson.isCompleted) SuccessEmerald.copy(alpha = 0.2f) else CyanBrand.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (lesson.isCompleted) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = SuccessEmerald)
                                    } else {
                                        Text("${lesson.id}", fontWeight = FontWeight.Bold, color = CyanBrand)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(lesson.title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("${lesson.subtitle} • ${lesson.readingTimeMin} мин", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    // Active Lesson Modal Dialog
    if (selectedLesson != null) {
        val lesson = selectedLesson!!

        AlertDialog(
            onDismissRequest = { selectedLesson = null },
            title = { Text(lesson.title, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(lesson.content, fontSize = 13.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurface)

                    HorizontalDivider()

                    Text("Проверочный вопрос:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyanBrand)
                    Text(lesson.question, fontSize = 12.sp, fontWeight = FontWeight.Medium)

                    lesson.options.forEachIndexed { idx, opt ->
                        val isSelected = selectedOption == idx
                        val isCorrect = idx == lesson.correctIndex

                        val btnColor = if (isSubmitted) {
                            if (isCorrect) SuccessEmerald.copy(alpha = 0.2f) else if (isSelected) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            if (isSelected) CyanBrand.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                        }

                        Surface(
                            onClick = { if (!isSubmitted) selectedOption = idx },
                            shape = RoundedCornerShape(10.dp),
                            color = btnColor,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${idx + 1}. $opt",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (isSubmitted) {
                        Text(
                            text = "💡 " + lesson.explanation,
                            fontSize = 11.sp,
                            color = CyanBrand,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                if (!isSubmitted) {
                    Button(
                        onClick = {
                            if (selectedOption != null) {
                                isSubmitted = true
                                if (selectedOption == lesson.correctIndex) {
                                    lesson.isCompleted = true
                                    SoundManager.playFanfare(context)
                                    onLessonCompleted()
                                } else {
                                    SoundManager.playError(context)
                                }
                            }
                        },
                        enabled = selectedOption != null
                    ) {
                        Text("Проверить ответ")
                    }
                } else {
                    Button(onClick = { selectedLesson = null }) {
                        Text("Завершить урок")
                    }
                }
            }
        )
    }
}
