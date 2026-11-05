package com.example.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScrollSearchStateBus
import com.example.model.OverlayUiVersion
import com.example.model.SearchEngine
import com.example.model.SearchType
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.SafeSearchOffGreen
import com.example.ui.theme.SafeSearchOffGreenBg
import com.example.ui.theme.TikTokCyan
import com.example.ui.theme.TikTokDarkText
import com.example.ui.theme.TikTokRed

@Composable
fun OverlayHeader(
    username: String?,
    currentVersion: OverlayUiVersion,
    currentEngine: SearchEngine,
    isSafeSearchDisabled: Boolean = true,
    onVersionSelected: (OverlayUiVersion) -> Unit,
    onEngineSelected: (SearchEngine) -> Unit,
    onToggleSafeSearch: () -> Unit = {},
    onMinimize: () -> Unit,
    onClose: () -> Unit,
    onRefresh: () -> Unit,
    onMoveDrag: ((deltaX: Float, deltaY: Float) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showVersionMenu by remember { mutableStateOf(false) }
    var showEngineMenu by remember { mutableStateOf(false) }

    val isDark by ScrollSearchStateBus.isDarkTheme.collectAsState()
    val searchType by ScrollSearchStateBus.selectedSearchType.collectAsState()
    val isSuperFullscreen by ScrollSearchStateBus.isSuperFullscreenEnabled.collectAsState()

    val surfaceColor = if (isDark) DarkSurface else Color.White
    val textPrimaryColor = if (isDark) Color.White else TikTokDarkText
    val textMutedColor = if (isDark) Color(0xFFA0A0C0) else Color(0xFF6B6C7B)
    val pillBgColor = if (isDark) DarkSurfaceElevated else Color(0xFFF1F1F4)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(surfaceColor)
    ) {
        val heightRatio by ScrollSearchStateBus.heightRatio.collectAsState()
        // Top handle strictly for RESIZING card height (expanding/shrinking card without lifting modal)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        val deltaRatio = -dragAmount / 1000f
                        ScrollSearchStateBus.setHeightRatio(heightRatio + deltaRatio)
                    }
                }
                .testTag("resize_card_handle"),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDark) Color.White.copy(alpha = 0.50f) else Color.Black.copy(alpha = 0.35f)
                    )
            )
        }

        // Main Header Controls Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // TikTok Video Creator Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            ) {
                // TikTok Cyan & Red Neon gradient indicator ring
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(TikTokCyan, TikTokRed)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(surfaceColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "@",
                            color = TikTokRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Text(
                        text = if (!username.isNullOrBlank()) "@$username" else "TikTokLens...",
                        color = textPrimaryColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "TikTok Vídeos Live Search",
                        color = TikTokRed,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }

            // Controls & Toggles Row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // UI Version Selector (F1-F7, V1-V7)
                Box {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = pillBgColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showVersionMenu = true }
                            .testTag("version_picker_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Versões",
                                tint = TikTokRed,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = currentVersion.code,
                                color = textPrimaryColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Abrir menu",
                                tint = textMutedColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showVersionMenu,
                        onDismissRequest = { showVersionMenu = false },
                        modifier = Modifier.background(DarkSurfaceElevated)
                    ) {
                        OverlayUiVersion.entries.forEach { version ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "${version.code}: ${version.title}",
                                            color = if (version == currentVersion) TikTokRed else Color.White,
                                            fontWeight = if (version == currentVersion) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = version.subtitle,
                                            color = Color.Gray,
                                            fontSize = 10.sp
                                        )
                                    }
                                },
                                onClick = {
                                    showVersionMenu = false
                                    onVersionSelected(version)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Search Engine Selector
                Box {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = pillBgColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showEngineMenu = true }
                            .testTag("engine_picker_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = currentEngine.shortName,
                                color = TikTokCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Selecionar buscador",
                                tint = textMutedColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showEngineMenu,
                        onDismissRequest = { showEngineMenu = false },
                        modifier = Modifier.background(DarkSurfaceElevated)
                    ) {
                        SearchEngine.entries.forEach { engine ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = engine.displayName,
                                        color = if (engine == currentEngine) TikTokCyan else Color.White,
                                        fontWeight = if (engine == currentEngine) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                },
                                onClick = {
                                    showEngineMenu = false
                                    onEngineSelected(engine)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // "SEM CENSURA" Toggle Button (Exact button requested by user and matching screenshot!)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSafeSearchDisabled) SafeSearchOffGreenBg else pillBgColor,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSafeSearchDisabled) SafeSearchOffGreen else Color.Gray.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onToggleSafeSearch() }
                        .testTag("safesearch_toggle_button")
                ) {
                    Text(
                        text = if (isSafeSearchDisabled) "Safe: OFF" else "Safe: ON",
                        color = if (isSafeSearchDisabled) SafeSearchOffGreen else textMutedColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(3.dp))

                // Filtro de Alta Qualidade (HD)
                val isHighQualityFilter by ScrollSearchStateBus.isHighQualityFilterEnabled.collectAsState()
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isHighQualityFilter) TikTokCyan.copy(alpha = 0.20f) else pillBgColor,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isHighQualityFilter) TikTokCyan else Color.Gray.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { ScrollSearchStateBus.toggleHighQualityFilter() }
                        .testTag("hd_filter_toggle_button")
                ) {
                    Text(
                        text = if (isHighQualityFilter) "HD: ON" else "HD: OFF",
                        color = if (isHighQualityFilter) TikTokCyan else textMutedColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Theme Toggle (TikTok Claro / Escuro)
                IconButton(
                    onClick = { ScrollSearchStateBus.toggleTheme() },
                    modifier = Modifier.size(28.dp).testTag("theme_toggle_header_btn")
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Alternar Tema",
                        tint = if (isDark) Color(0xFFFFD600) else Color(0xFF161823),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Refresh Button
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(28.dp).testTag("overlay_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Atualizar busca",
                        tint = textMutedColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Minimize Button
                IconButton(
                    onClick = onMinimize,
                    modifier = Modifier.size(28.dp).testTag("overlay_minimize_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Minimizar",
                        tint = textMutedColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Close Button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp).testTag("overlay_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = Color(0xFFFF4D4F),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Secondary Tabs Bar: [Imagens] [Vídeos] [Sites] [Notícias] + [Super Tela Cheia]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Search Mode Tabs (Imagens, Vídeos, Sites, Notícias)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(SearchType.entries) { type ->
                    val isSelected = searchType == type
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) TikTokRed.copy(alpha = 0.20f) else Color.Transparent,
                        border = if (isSelected) {
                            androidx.compose.foundation.BorderStroke(1.dp, TikTokRed)
                        } else null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { ScrollSearchStateBus.setSearchType(type) }
                            .testTag("search_tab_${type.id}")
                    ) {
                        Text(
                            text = type.displayName,
                            color = if (isSelected) TikTokRed else textMutedColor,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // "Super Tela Cheia" Quick Toggle Chip in Header (active by default)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSuperFullscreen) TikTokRed.copy(alpha = 0.22f) else pillBgColor,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSuperFullscreen) TikTokRed else Color.Transparent
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { ScrollSearchStateBus.toggleSuperFullscreen() }
                    .testTag("super_fullscreen_header_chip")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Super Tela Cheia",
                        tint = if (isSuperFullscreen) TikTokRed else textMutedColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isSuperFullscreen) "Super Cheia" else "Tela Padrão",
                        color = if (isSuperFullscreen) TikTokRed else textMutedColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
