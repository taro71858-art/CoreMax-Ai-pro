package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.data.models.Currency
import com.example.data.models.Niche
import com.example.ui.navigation.AppScreen
import com.example.ui.navigation.DrawerGroup
import com.example.ui.theme.*

@Composable
fun AppDrawerContent(
    businessName: String,
    currentNiche: Niche,
    defaultCurrency: Currency,
    themeMode: AppThemeMode,
    isSoundEnabled: Boolean,
    recentScreens: List<AppScreen>,
    onSelectScreen: (AppScreen) -> Unit,
    onToggleTheme: () -> Unit,
    onToggleSound: () -> Unit,
    onSelectNiche: () -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val expandedGroups = remember {
        mutableStateMapOf(
            DrawerGroup.CALCULATIONS to true,
            DrawerGroup.AI_HELPERS to true,
            DrawerGroup.SETTINGS to false,
            DrawerGroup.EDUCATION to false
        )
    }

    ModalDrawerSheet(
        modifier = modifier
            .fillMaxHeight()
            .widthIn(max = 360.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header: Business Profile Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CyanBrand.copy(alpha = 0.2f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = CyanBrand,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = businessName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Ниша: ${currentNiche.name}",
                                fontSize = 12.sp,
                                color = CyanBrand,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = " • ${defaultCurrency.code}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            SoundManager.playClick(context)
                            onCloseDrawer()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick Action Switches (Theme, Sound, Niche, Currency)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Theme Toggle Chip
                AssistChip(
                    onClick = {
                        SoundManager.playSwitch(context)
                        onToggleTheme()
                    },
                    label = { Text(themeMode.name.take(4), fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (themeMode == AppThemeMode.DARK) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                // Sound Toggle Chip
                AssistChip(
                    onClick = {
                        SoundManager.playSwitch(context)
                        onToggleSound()
                    },
                    label = { Text(if (isSoundEnabled) "Звук" else "Без зв.", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                // Currency Chip
                AssistChip(
                    onClick = {
                        SoundManager.playClick(context)
                        onSelectScreen(AppScreen.DEFAULT_CURRENCY)
                    },
                    label = { Text(defaultCurrency.symbol, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    leadingIcon = {
                        Text(defaultCurrency.flag, fontSize = 12.sp)
                    },
                    modifier = Modifier.weight(0.9f)
                )
            }

            // Global Search Bar across all tools
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = { Text("Поиск по всем 24 разделам...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Поиск", modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Scrollable Menu List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                // Search Results mode
                if (searchQuery.isNotBlank()) {
                    val filtered = AppScreen.entries.filter {
                        it != AppScreen.MAIN && (
                            it.title.contains(searchQuery, ignoreCase = true) ||
                            it.subtitle.contains(searchQuery, ignoreCase = true) ||
                            it.group.title.contains(searchQuery, ignoreCase = true)
                        )
                    }

                    item {
                        Text(
                            text = "Найдено (${filtered.size}):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanBrand,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    items(filtered) { screen ->
                        DrawerItemRow(
                            screen = screen,
                            onClick = {
                                SoundManager.playClick(context)
                                onSelectScreen(screen)
                            }
                        )
                    }
                } else {
                    // Recent items block
                    if (recentScreens.isNotEmpty()) {
                        item {
                            Text(
                                text = "НЕРАВНОДУШНЫЕ (НЕДАВНИЕ)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        items(recentScreens.take(3)) { screen ->
                            DrawerItemRow(
                                screen = screen,
                                isRecent = true,
                                onClick = {
                                    SoundManager.playClick(context)
                                    onSelectScreen(screen)
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    // Accordion Groups
                    listOf(
                        DrawerGroup.CALCULATIONS,
                        DrawerGroup.AI_HELPERS,
                        DrawerGroup.SETTINGS,
                        DrawerGroup.EDUCATION
                    ).forEach { group ->
                        val isExpanded = expandedGroups[group] == true
                        val groupScreens = AppScreen.entries.filter { it.group == group }

                        item {
                            Surface(
                                onClick = {
                                    SoundManager.playClick(context)
                                    expandedGroups[group] = !isExpanded
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Transparent,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = group.title.uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isExpanded) CyanBrand else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        if (isExpanded) {
                            items(groupScreens) { screen ->
                                DrawerItemRow(
                                    screen = screen,
                                    onClick = {
                                        SoundManager.playClick(context)
                                        onSelectScreen(screen)
                                    }
                                )
                            }
                        }
                    }

                    // Block: «Скоро в CoreMaX»
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, IndigoTag.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = IndigoTag.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "СКОРО В COREMAX",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = IndigoTag,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Авто-анализ аудио диалогов",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "ИИ будет сам слушать записи звонков и диалоги из мессенджеров. Ручной разбор уже доступен в разделе «Разговоры с клиентами»!",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Footer with Instagram link
                    item {
                        Surface(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/tojprogs/"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = CyanBrand, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Наш Instagram: @tojprogs", fontSize = 12.sp, color = CyanBrand, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerItemRow(
    screen: AppScreen,
    onClick: () -> Unit,
    isRecent: Boolean = false
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isRecent) CyanBrand.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = null,
                        tint = if (isRecent) CyanBrand else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = screen.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = screen.subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
