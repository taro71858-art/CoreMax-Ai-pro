package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
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
import com.example.ui.theme.CyanBrand
import com.example.ui.theme.IndigoTag

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppHeader(
    currentNicheName: String,
    isAmountsHidden: Boolean,
    onMenuClick: () -> Unit,
    onNicheClick: () -> Unit,
    onEyeToggle: () -> Unit,
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hamburger menu button (min 48dp target)
            IconButton(
                onClick = {
                    SoundManager.playClick(context)
                    onMenuClick()
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Меню",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            // Title: «Бизнес-аналитик» strictly in one line
            Text(
                text = "Бизнес-аналитик",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // 1. Niche picker chip «Кафе ▾»
            Surface(
                onClick = {
                    SoundManager.playClick(context)
                    onNicheClick()
                },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .height(34.dp)
                    .defaultMinSize(minWidth = 48.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currentNicheName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyanBrand,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Выбор ниши",
                        tint = CyanBrand,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // 2. Eye icon (hide amounts)
            IconButton(
                onClick = {
                    SoundManager.playSwitch(context)
                    onEyeToggle()
                },
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = if (isAmountsHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (isAmountsHidden) "Показать суммы" else "Скрыть суммы",
                    tint = if (isAmountsHidden) IndigoTag else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // 3. Instagram tojprogs icon (with long press toast)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .combinedClickable(
                        onClick = {
                            SoundManager.playClick(context)
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/tojprogs/"))
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Открытие Instagram @tojprogs", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onLongClick = {
                            SoundManager.playClick(context)
                            Toast.makeText(context, "Наш Instagram и сайт компании: @tojprogs", Toast.LENGTH_LONG).show()
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Instagram tojprogs",
                    tint = CyanBrand,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Contextual help "?" button in header
            IconButton(
                onClick = {
                    SoundManager.playClick(context)
                    onHelpClick()
                },
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "Помощь по экрану",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
