package com.ads.assetsmanager.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ads.assetsmanager.ui.theme.*

// === GAMER CARD - Card estilizado com borda neon ===
@Composable
fun GamerCard(
    modifier: Modifier = Modifier,
    borderColor: Color = NeonCyan,
    glowEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(4.dp) // Cantos mais pixelados
    
    Card(
        modifier = modifier
            .then(
                if (glowEnabled) {
                    Modifier
                        .shadow(
                            elevation = 8.dp,
                            shape = shape,
                            ambientColor = borderColor.copy(alpha = 0.3f),
                            spotColor = borderColor.copy(alpha = 0.5f)
                        )
                } else Modifier
            )
            .border(
                width = 2.dp,
                color = borderColor,
                shape = shape
            ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = DarkCard
        )
    ) {
        Column(content = content)
    }
}

// === NEON BUTTON - Botão com efeito neon ===
@Composable
fun NeonButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector? = null,
    color: Color = NeonCyan,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(color, color.copy(alpha = 0.5f), color)
                ),
                shape = RoundedCornerShape(4.dp)
            ),
        enabled = enabled,
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.2f),
            contentColor = color
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text.uppercase(),
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}

// === PIXEL FAB - Floating Action Button estilo pixel ===
@Composable
fun PixelFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Add,
    containerColor: Color = NeonPink,
    contentColor: Color = DarkBackground
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .border(
                width = 3.dp,
                color = containerColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp)
            ),
        shape = RoundedCornerShape(4.dp),
        containerColor = containerColor,
        contentColor = contentColor
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Adicionar",
            modifier = Modifier.size(28.dp)
        )
    }
}

// === GAMER TOP BAR - Barra superior estilizada ===
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamerTopBar(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonCyan,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = NeonPink
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DarkBackground,
            titleContentColor = NeonCyan
        )
    )
}

// === RESOURCE TYPE CHIP - Badge para tipo de recurso ===
@Composable
fun ResourceTypeChip(
    type: String,
    modifier: Modifier = Modifier
) {
    val (icon, color, label) = when (type.uppercase()) {
        "IMAGE" -> Triple(Icons.Default.Image, ImageColor, "IMG")
        "AUDIO" -> Triple(Icons.Default.Audiotrack, AudioColor, "SFX")
        "TEXT" -> Triple(Icons.Default.Description, TextColor, "TXT")
        "LINK" -> Triple(Icons.Default.Link, LinkColor, "URL")
        "ANIMATION" -> Triple(Icons.Default.Animation, NeonPurple, "ANIM")
        "SPRITE" -> Triple(Icons.Default.GridOn, NeonGreen, "SPR")
        else -> Triple(Icons.Default.Attachment, TextSecondary, type.take(3).uppercase())
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(2.dp),
        color = color.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// === PIXEL DIVIDER - Divisor estilizado ===
@Composable
fun PixelDivider(
    modifier: Modifier = Modifier,
    color: Color = NeonPurple.copy(alpha = 0.3f)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        color,
                        color,
                        Color.Transparent
                    )
                )
            )
    )
}

// === EMPTY STATE - Estado vazio estilizado ===
@Composable
fun EmptyStateMessage(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = NeonPurple.copy(alpha = 0.5f)
        )
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
    }
}

// === STAT BADGE - Badge para stats/contadores ===
@Composable
fun StatBadge(
    label: String,
    value: String,
    color: Color = NeonCyan,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.7f)
        )
    }
}

// === GLOW BOX - Container com efeito de brilho ===
@Composable
fun GlowBox(
    modifier: Modifier = Modifier,
    glowColor: Color = NeonCyan,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(8.dp),
                ambientColor = glowColor.copy(alpha = 0.4f),
                spotColor = glowColor.copy(alpha = 0.6f)
            )
            .background(
                color = DarkCard,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        glowColor,
                        glowColor.copy(alpha = 0.5f),
                        glowColor
                    )
                ),
                shape = RoundedCornerShape(8.dp)
            ),
        content = content
    )
}
