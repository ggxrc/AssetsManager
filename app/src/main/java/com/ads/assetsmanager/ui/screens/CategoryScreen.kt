package com.ads.assetsmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ads.assetsmanager.data.model.Category
import com.ads.assetsmanager.ui.components.*
import com.ads.assetsmanager.ui.theme.*
import com.ads.assetsmanager.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    viewModel: GameViewModel,
    onCategoryClick: (Int) -> Unit
) {
    val categories by viewModel.allCategories.observeAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            Column {
                GamerTopBar(
                    title = "⚔️ Assets Manager",
                    subtitle = "Organize seus assets de jogo"
                )
                // Header decorativo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    NeonPink.copy(alpha = 0.3f),
                                    NeonCyan.copy(alpha = 0.3f),
                                    NeonPurple.copy(alpha = 0.3f)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatBadge(
                            label = "Categorias",
                            value = categories.size.toString(),
                            color = NeonCyan
                        )
                        StatBadge(
                            label = "Status",
                            value = if (categories.isEmpty()) "VAZIO" else "ATIVO",
                            color = if (categories.isEmpty()) NeonOrange else NeonGreen
                        )
                    }
                }
                PixelDivider()
            }
        },
        floatingActionButton = {
            PixelFab(
                onClick = { showDialog = true },
                containerColor = NeonPink
            )
        }
    ) { padding ->
        if (categories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateMessage(
                    icon = Icons.Default.FolderOpen,
                    title = "Nenhuma categoria",
                    subtitle = "Toque no + para criar sua primeira categoria de assets"
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 16.dp,
                    bottom = padding.calculateBottomPadding() + 80.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(categories, key = { it.id }) { category ->
                    CategoryItem(
                        category = category,
                        onClick = { onCategoryClick(category.id) },
                        onDelete = { viewModel.deleteCategory(category) }
                    )
                }
            }
        }

        if (showDialog) {
            AddCategoryDialog(
                onDismiss = { showDialog = false },
                onConfirm = { name ->
                    viewModel.insertCategory(name)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    GamerCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        borderColor = NeonCyan,
        glowEnabled = true
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícone estilizado
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = NeonCyan.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .border(
                        width = 2.dp,
                        color = NeonCyan,
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "▶ TOQUE PARA ACESSAR",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonGreen.copy(alpha = 0.7f)
                )
            }
            
            // Botão delete
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = PixelRed.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = PixelRed.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Excluir",
                    tint = PixelRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AddCategoryDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        shape = RoundedCornerShape(8.dp),
        title = {
            Text(
                text = "📁 NOVA CATEGORIA",
                style = MaterialTheme.typography.titleLarge,
                color = NeonCyan
            )
        },
        text = {
            Column {
                Text(
                    text = "Digite o nome da categoria:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Ex: NPCs, Itens, Cenários") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = NeonPurple.copy(alpha = 0.5f),
                        focusedLabelColor = NeonCyan,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = NeonCyan
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            NeonButton(
                onClick = { if (text.isNotBlank()) onConfirm(text) },
                text = "Criar",
                icon = Icons.Default.Add,
                color = NeonGreen
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "CANCELAR",
                    color = PixelRed
                )
            }
        }
    )
}