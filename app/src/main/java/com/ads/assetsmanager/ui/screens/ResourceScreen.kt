package com.ads.assetsmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ads.assetsmanager.data.model.EntityResource
import com.ads.assetsmanager.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val entityWithResources by viewModel.resourcesByEntity.observeAsState()
    var showDialog by remember { mutableStateOf(false) }

    // Captura o estado localmente para permitir Smart Casting (evita !!)
    val currentData = entityWithResources

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = currentData?.gameEntity?.name ?: "Recursos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            // Só mostra o botão se os dados existirem
            if (currentData != null) {
                FloatingActionButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Novo Recurso")
                }
            }
        }
    ) { padding ->
        if (currentData == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val ownerId = currentData.gameEntity.entityId
            val list = currentData.resources

            if (list.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Nenhum recurso adicionado.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = padding,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = list,
                        key = { it.resourceId } // Otimização de performance
                    ) { resource ->
                        ResourceItem(
                            resource = resource,
                            onDelete = { viewModel.deleteResource(resource) }
                        )
                    }
                }
            }

            if (showDialog) {
                AddResourceDialog(
                    onDismiss = { showDialog = false },
                    onConfirm = { type, value, label ->
                        viewModel.insertResource(ownerId, type, value, label)
                        showDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun ResourceItem(resource: EntityResource, onDelete: () -> Unit) {
    // Define ícone baseado no tipo (String)
    val icon = when (resource.type) {
        "IMAGE" -> Icons.Default.Image
        "AUDIO" -> Icons.Default.Audiotrack
        "LINK" -> Icons.Default.Link
        else -> Icons.Default.Description
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(text = resource.label, fontWeight = FontWeight.Bold)
                // Exibe o valor (URI ou Texto) de forma truncada se for longo
                Text(text = resource.value, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                Text(text = resource.type, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color.Red)
            }
        }
    }
}

@Composable
fun AddResourceDialog(
    onDismiss: () -> Unit,
    onConfirm: (type: String, value: String, label: String) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("TEXT") }

    val types = listOf("TEXT", "IMAGE", "AUDIO", "LINK")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Recurso") },
        text = {
            Column {
                // Seletor de Tipo Simples (Row de Botões)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    types.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.take(1)) } // Mostra só a inicial pra caber
                        )
                    }
                }
                Text("Tipo selecionado: $selectedType", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Rótulo (ex: Sprite Andando)") }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text(if(selectedType == "TEXT") "Conteúdo" else "URI / Caminho / URL") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (label.isNotBlank() && value.isNotBlank()) onConfirm(selectedType, value, label)
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}