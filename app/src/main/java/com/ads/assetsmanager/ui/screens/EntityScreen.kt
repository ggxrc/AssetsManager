package com.ads.assetsmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ads.assetsmanager.data.model.GameEntity
import com.ads.assetsmanager.ui.components.*
import com.ads.assetsmanager.ui.theme.*
import com.ads.assetsmanager.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntityScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    onEntityClick: (Int) -> Unit
) {
    val categoryWithEntities by viewModel.entitiesByCategory.observeAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            Column {
                GamerTopBar(
                    title = categoryWithEntities?.category?.name ?: "Carregando...",
                    subtitle = "${categoryWithEntities?.entities?.size ?: 0} entidades",
                    onBackClick = onBack
                )
                PixelDivider()
            }
        },
        floatingActionButton = {
            PixelFab(
                onClick = { showDialog = true },
                containerColor = NeonGreen
            )
        }
    ) { padding ->

        if (categoryWithEntities == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonCyan)
            }
        } else {
            val currentCategory = categoryWithEntities!!.category
            val entityList = categoryWithEntities!!.entities

            if (entityList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateMessage(
                        icon = Icons.Default.Person,
                        title = "Nenhuma entidade",
                        subtitle = "Crie personagens, itens, inimigos e mais!"
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
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(entityList, key = { it.entityId }) { entity ->
                        EntityItem(
                            entity = entity,
                            onClick = { onEntityClick(entity.entityId) },
                            onDelete = { viewModel.deleteEntity(entity) }
                        )
                    }
                }
            }

            if (showDialog) {
                AddEntityDialog(
                    onDismiss = { showDialog = false },
                    onConfirm = { name, description, lore ->
                        viewModel.insertEntity(currentCategory.id, name, description, lore)
                        showDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun EntityItem(
    entity: GameEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    GamerCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        borderColor = NeonPink,
        glowEnabled = true
    ) {
        Column {
            // Header com thumbnail placeholder
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                NeonPink.copy(alpha = 0.2f),
                                NeonPurple.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail da entidade
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = DarkSurface,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(NeonPink, NeonPurple)
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (entity.thumbnailUri != null) {
                        // TODO: Carregar imagem com Coil
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = NeonPink,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        // Placeholder com inicial
                        Text(
                            text = entity.name.take(2).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = NeonPink,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entity.name.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (entity.description.isNotBlank()) {
                        Text(
                            text = entity.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = PixelRed.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = PixelRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            // Footer com info adicional
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Badges de tipos de recursos (placeholder)
                    ResourceTypeChip(type = "SPRITE")
                    if (entity.lore != null) {
                        ResourceTypeChip(type = "TEXT")
                    }
                }
                
                Text(
                    text = "▶ VER ASSETS",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonGreen
                )
            }
        }
    }
}

@Composable
fun AddEntityDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String, lore: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var lore by remember { mutableStateOf("") }
    var showLoreField by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        shape = RoundedCornerShape(8.dp),
        title = {
            Text(
                text = "👤 NOVA ENTIDADE",
                style = MaterialTheme.typography.titleLarge,
                color = NeonPink
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Crie um personagem, item ou qualquer entidade do seu jogo:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Entidade") },
                    placeholder = { Text("Ex: João, Goblin, Espada...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPink,
                        unfocusedBorderColor = NeonPurple.copy(alpha = 0.5f),
                        focusedLabelColor = NeonPink,
                        cursorColor = NeonPink
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição Curta") },
                    placeholder = { Text("Uma breve descrição...") },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = NeonPurple.copy(alpha = 0.5f),
                        focusedLabelColor = NeonCyan,
                        cursorColor = NeonCyan
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Toggle para mostrar campo de lore
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLoreField = !showLoreField }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (showLoreField) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = NeonPurple
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Adicionar Lore/História",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonPurple
                    )
                }
                
                if (showLoreField) {
                    OutlinedTextField(
                        value = lore,
                        onValueChange = { lore = it },
                        label = { Text("Lore / História") },
                        placeholder = { Text("A história por trás desta entidade...") },
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = NeonPurple.copy(alpha = 0.5f),
                            focusedLabelColor = NeonGreen,
                            cursorColor = NeonGreen
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            NeonButton(
                onClick = { 
                    if (name.isNotBlank()) {
                        onConfirm(name, description, lore.ifBlank { null })
                    }
                },
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