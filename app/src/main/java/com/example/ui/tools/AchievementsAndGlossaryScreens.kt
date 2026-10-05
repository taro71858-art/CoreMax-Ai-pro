package com.example.ui.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AchievementItem
import com.example.data.models.GlossaryTerm
import com.example.data.models.StaticCatalogs
import com.example.ui.theme.CyanBrand
import com.example.ui.theme.GoldCrown
import com.example.ui.theme.SuccessEmerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    achievements: List<AchievementItem>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Достижения и Золотая Корона", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
        ) {
            items(achievements) { ach ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (ach.isCrown) GoldCrown.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = if (ach.isCrown) androidx.compose.foundation.BorderStroke(1.5.dp, GoldCrown) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (ach.isCrown) Icons.Default.WorkspacePremium else Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = if (ach.isUnlocked || ach.isCrown) GoldCrown else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(ach.title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(ach.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (ach.current / ach.target.toFloat()).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp),
                                color = GoldCrown,
                                trackColor = MaterialTheme.colorScheme.surface
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "${ach.current}/${ach.target}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldCrown
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlossaryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTerm by remember { mutableStateOf<GlossaryTerm?>(null) }

    val terms = remember(searchQuery) {
        StaticCatalogs.glossaryTerms.filter {
            searchQuery.isBlank() || it.term.contains(searchQuery, ignoreCase = true) || it.shortDef.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Словарик терминов", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Поиск термина (маржа, EBITDA, LTV)...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(terms) { item ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { selectedTerm = item }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(item.term, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyanBrand)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(item.shortDef, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }

    if (selectedTerm != null) {
        val t = selectedTerm!!
        AlertDialog(
            onDismissRequest = { selectedTerm = null },
            title = { Text(t.term, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(t.fullDef, fontSize = 13.sp)
                    if (t.formula.isNotBlank()) {
                        Text("Формула: ${t.formula}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanBrand)
                    }
                    if (t.example.isNotBlank()) {
                        Text("Пример: ${t.example}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedTerm = null }) { Text("Понятно") }
            }
        )
    }
}
