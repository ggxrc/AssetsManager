package com.ads.assetsmanager.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.ads.assetsmanager.data.model.EntityResource
import com.ads.assetsmanager.data.model.GameEntity
import com.ads.assetsmanager.data.model.ResourceType
import com.ads.assetsmanager.ui.components.*
import com.ads.assetsmanager.ui.theme.*
import com.ads.assetsmanager.viewmodel.ExportState
import com.ads.assetsmanager.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val entityWithResources by viewModel.resourcesByEntity.observeAsState()
    val exportState by viewModel.exportState.observeAsState(ExportState.Idle)
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf<String?>(null) }

    val currentData = entityWithResources
    
    // Launcher para criar arquivo ZIP
    val createZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let {
            currentData?.let { data ->
                viewModel.exportEntityAsZip(context, data.gameEntity, data.resources, it)
            }
        }
    }
    
    // Launcher para selecionar pasta de exportação
    val selectFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            // Persistir permissão
            context.contentResolver.takePersistableUriPermission(
                it,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            currentData?.let { data ->
                viewModel.exportEntityAsFolder(context, data.gameEntity, data.resources, it)
            }
        }
    }
    
    // Mostrar resultado da exportação
    LaunchedEffect(exportState) {
        when (exportState) {
            is ExportState.Success -> {
                // Pode mostrar snackbar aqui
                viewModel.resetExportState()
            }
            is ExportState.Error -> {
                viewModel.resetExportState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            Column {
                GamerTopBar(
                    title = currentData?.gameEntity?.name ?: "Carregando...",
                    subtitle = currentData?.gameEntity?.description,
                    onBackClick = onBack,
                    actions = {
                        // Botão de exportar
                        if (currentData != null) {
                            IconButton(onClick = { showExportDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Exportar",
                                    tint = NeonGreen
                                )
                            }
                        }
                    }
                )
                
                if (currentData != null) {
                    EntityInfoHeader(
                        entity = currentData.gameEntity,
                        resourceCount = currentData.resources.size
                    )
                }
                PixelDivider()
            }
        },
        floatingActionButton = {
            if (currentData != null) {
                PixelFab(
                    onClick = { showAddDialog = true },
                    containerColor = NeonCyan
                )
            }
        }
    ) { padding ->
        if (currentData == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonCyan)
            }
        } else {
            val ownerId = currentData.gameEntity.entityId
            val allResources = currentData.resources
            
            val filteredResources = if (selectedFilter != null) {
                allResources.filter { it.type == selectedFilter }
            } else {
                allResources
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                ResourceTypeFilters(
                    resources = allResources,
                    selectedFilter = selectedFilter,
                    onFilterChange = { selectedFilter = it }
                )
                
                if (filteredResources.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStateMessage(
                            icon = Icons.Default.Attachment,
                            title = if (selectedFilter != null) "Nenhum $selectedFilter" else "Sem assets",
                            subtitle = "Adicione sprites, sons, textos e mais!"
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            top = 16.dp,
                            bottom = 80.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(
                            items = filteredResources,
                            key = { it.resourceId }
                        ) { resource ->
                            ResourceItem(
                                resource = resource,
                                entityId = ownerId,
                                onDelete = { viewModel.deleteResource(resource) },
                                onSetAsThumbnail = if (resource.type == ResourceType.SPRITE) {
                                    { viewModel.setAsEntityThumbnail(ownerId, resource.value) }
                                } else null
                            )
                        }
                    }
                }
            }

            // Dialog para adicionar recurso
            if (showAddDialog) {
                AddResourceDialogNew(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { type, uri, label, mimeType, fileName, fileSize ->
                        viewModel.insertResource(ownerId, type, uri, label, mimeType, fileName, fileSize)
                        showAddDialog = false
                    }
                )
            }
            
            // Dialog de exportação
            if (showExportDialog) {
                ExportDialog(
                    entityName = currentData.gameEntity.name,
                    onDismiss = { showExportDialog = false },
                    onExportAsZip = {
                        showExportDialog = false
                        createZipLauncher.launch("${currentData.gameEntity.name}_export.zip")
                    },
                    onExportAsFolder = {
                        showExportDialog = false
                        selectFolderLauncher.launch(null)
                    }
                )
            }
        }
    }
    
    // Loading overlay durante exportação
    if (exportState is ExportState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = NeonCyan)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "EXPORTANDO...",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonCyan
                )
            }
        }
    }
}

@Composable
fun EntityInfoHeader(
    entity: GameEntity,
    resourceCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        NeonPink.copy(alpha = 0.2f),
                        NeonPurple.copy(alpha = 0.1f),
                        NeonCyan.copy(alpha = 0.2f)
                    )
                )
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    color = DarkSurface,
                    shape = RoundedCornerShape(4.dp)
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(NeonPink, NeonCyan)
                    ),
                    shape = RoundedCornerShape(4.dp)
                )
                .clip(RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (entity.thumbnailUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(entity.thumbnailUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = entity.name.take(2).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = NeonPink,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            if (entity.lore != null) {
                Text(
                    text = entity.lore,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        StatBadge(
            label = "Assets",
            value = resourceCount.toString(),
            color = NeonCyan
        )
    }
}

@Composable
fun ResourceTypeFilters(
    resources: List<EntityResource>,
    selectedFilter: String?,
    onFilterChange: (String?) -> Unit
) {
    val types = listOf(
        null to "TODOS",
        ResourceType.SPRITE to "🖼️ SPRITE",
        ResourceType.ANIMATION to "🎬 ANIM",
        ResourceType.AUDIO to "🎵 AUDIO",
        ResourceType.TEXT to "📝 TEXT",
        ResourceType.LINK to "🔗 LINK"
    )
    
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(types) { (type, label) ->
            val isSelected = selectedFilter == type
            val count = if (type == null) resources.size else resources.count { it.type == type }
            
            FilterChip(
                selected = isSelected,
                onClick = { onFilterChange(type) },
                label = {
                    Text(
                        text = "$label ($count)",
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NeonCyan.copy(alpha = 0.3f),
                    selectedLabelColor = NeonCyan,
                    containerColor = DarkCard,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if (isSelected) NeonCyan else NeonPurple.copy(alpha = 0.3f),
                    selectedBorderColor = NeonCyan,
                    enabled = true,
                    selected = isSelected
                )
            )
        }
    }
}

@Composable
fun ResourceItem(
    resource: EntityResource,
    entityId: Int,
    onDelete: () -> Unit,
    onSetAsThumbnail: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val (icon, color) = when (resource.type) {
        ResourceType.SPRITE -> Icons.Default.Image to ImageColor
        ResourceType.ANIMATION -> Icons.Default.Animation to NeonPurple
        ResourceType.AUDIO -> Icons.Default.Audiotrack to AudioColor
        ResourceType.TEXT -> Icons.Default.Description to TextColor
        ResourceType.LINK -> Icons.Default.Link to LinkColor
        else -> Icons.Default.Attachment to TextSecondary
    }
    
    val isImage = resource.type == ResourceType.SPRITE || resource.type == ResourceType.ANIMATION

    GamerCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = color,
        glowEnabled = false
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Preview de imagem ou ícone
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = color.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clip(RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isImage) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(resource.value)
                            .crossfade(true)
                            .decoderFactory(
                                if (android.os.Build.VERSION.SDK_INT >= 28) 
                                    ImageDecoderDecoder.Factory() 
                                else 
                                    GifDecoder.Factory()
                            )
                            .build(),
                        contentDescription = resource.label,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column(Modifier.weight(1f)) {
                Text(
                    text = resource.label.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Mostrar nome do arquivo se existir
                resource.fileName?.let { fileName ->
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ResourceTypeChip(type = resource.type)
                    
                    // Mostrar tamanho do arquivo
                    resource.fileSize?.let { size ->
                        Text(
                            text = formatFileSize(size),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }
            
            // Ações
            Row {
                // Botão de definir como perfil (apenas para sprites)
                if (onSetAsThumbnail != null) {
                    IconButton(
                        onClick = onSetAsThumbnail,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = NeonGreen.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Definir como perfil",
                            tint = NeonGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
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
        }
    }
}

@Composable
fun AddResourceDialogNew(
    onDismiss: () -> Unit,
    onConfirm: (type: String, uri: String, label: String, mimeType: String?, fileName: String?, fileSize: Long?) -> Unit
) {
    val context = LocalContext.current
    var label by remember { mutableStateOf("") }
    var textContent by remember { mutableStateOf("") }
    var linkUrl by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ResourceType.SPRITE) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedMimeType by remember { mutableStateOf<String?>(null) }
    var selectedFileSize by remember { mutableStateOf<Long?>(null) }
    
    // File picker para sprites (imagens estáticas)
    val spritePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            // Persistir permissão
            context.contentResolver.takePersistableUriPermission(
                it,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            selectedUri = it
            selectedMimeType = context.contentResolver.getType(it)
            
            // Obter nome e tamanho do arquivo
            context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                cursor.moveToFirst()
                selectedFileName = if (nameIndex >= 0) cursor.getString(nameIndex) else null
                selectedFileSize = if (sizeIndex >= 0) cursor.getLong(sizeIndex) else null
            }
        }
    }
    
    // File picker para animações (GIF e spritesheets)
    val animationPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            selectedUri = it
            selectedMimeType = context.contentResolver.getType(it)
            context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                cursor.moveToFirst()
                selectedFileName = if (nameIndex >= 0) cursor.getString(nameIndex) else null
                selectedFileSize = if (sizeIndex >= 0) cursor.getLong(sizeIndex) else null
            }
        }
    }
    
    // File picker para áudio
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            selectedUri = it
            selectedMimeType = context.contentResolver.getType(it)
            context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                cursor.moveToFirst()
                selectedFileName = if (nameIndex >= 0) cursor.getString(nameIndex) else null
                selectedFileSize = if (sizeIndex >= 0) cursor.getLong(sizeIndex) else null
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        shape = RoundedCornerShape(8.dp),
        title = {
            Text(
                text = "📦 NOVO ASSET",
                style = MaterialTheme.typography.titleLarge,
                color = NeonCyan
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Selecione o tipo de asset:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                
                // Seletor de tipo
                val types = listOf(
                    ResourceType.SPRITE to Triple("🖼️ Sprite", "PNG, JPG, WebP", ImageColor),
                    ResourceType.ANIMATION to Triple("🎬 Animação", "GIF, Spritesheet", NeonPurple),
                    ResourceType.AUDIO to Triple("🎵 Áudio", "MP3, OGG, WAV", AudioColor),
                    ResourceType.TEXT to Triple("📝 Texto", "Lore, descrição", TextColor),
                    ResourceType.LINK to Triple("🔗 Link", "URL externa", LinkColor)
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    types.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            row.forEach { (type, info) ->
                                val (name, formats, color) = info
                                val isSelected = selectedType == type
                                
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .selectable(
                                            selected = isSelected,
                                            onClick = { 
                                                selectedType = type
                                                selectedUri = null
                                                selectedFileName = null
                                            }
                                        ),
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isSelected) color.copy(alpha = 0.2f) else DarkSurface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) color else color.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isSelected) color else TextSecondary
                                        )
                                        Text(
                                            text = formats,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                            // Preencher espaço vazio se row impar
                            if (row.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Campo de nome
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Nome do Asset") },
                    placeholder = { Text("Ex: Sprite Idle, Som de Ataque...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = NeonPurple.copy(alpha = 0.5f),
                        focusedLabelColor = NeonCyan,
                        cursorColor = NeonCyan
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Conteúdo específico por tipo
                when (selectedType) {
                    ResourceType.SPRITE -> {
                        FilePickerButton(
                            label = "Selecionar Imagem",
                            selectedFileName = selectedFileName,
                            color = ImageColor,
                            onClick = { spritePickerLauncher.launch(ResourceType.SPRITE_MIME_TYPES) }
                        )
                        
                        // Preview se selecionado
                        selectedUri?.let { uri ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .background(DarkSurface, RoundedCornerShape(4.dp))
                                    .border(1.dp, ImageColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .clip(RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Preview",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                    
                    ResourceType.ANIMATION -> {
                        FilePickerButton(
                            label = "Selecionar GIF/Spritesheet",
                            selectedFileName = selectedFileName,
                            color = NeonPurple,
                            onClick = { animationPickerLauncher.launch(ResourceType.ANIMATION_MIME_TYPES) }
                        )
                        
                        selectedUri?.let { uri ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .background(DarkSurface, RoundedCornerShape(4.dp))
                                    .border(1.dp, NeonPurple.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .clip(RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(uri)
                                        .decoderFactory(
                                            if (android.os.Build.VERSION.SDK_INT >= 28)
                                                ImageDecoderDecoder.Factory()
                                            else
                                                GifDecoder.Factory()
                                        )
                                        .build(),
                                    contentDescription = "Preview",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                    
                    ResourceType.AUDIO -> {
                        FilePickerButton(
                            label = "Selecionar Áudio",
                            selectedFileName = selectedFileName,
                            color = AudioColor,
                            onClick = { audioPickerLauncher.launch(ResourceType.AUDIO_MIME_TYPES) }
                        )
                        
                        selectedUri?.let {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkSurface, RoundedCornerShape(4.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Audiotrack,
                                    contentDescription = null,
                                    tint = AudioColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = selectedFileName ?: "Áudio selecionado",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )
                                    selectedFileSize?.let { size ->
                                        Text(
                                            text = formatFileSize(size),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    ResourceType.TEXT -> {
                        OutlinedTextField(
                            value = textContent,
                            onValueChange = { textContent = it },
                            label = { Text("Conteúdo") },
                            placeholder = { Text("Digite o texto, lore, descrição...") },
                            minLines = 4,
                            maxLines = 6,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TextColor,
                                unfocusedBorderColor = NeonPurple.copy(alpha = 0.5f),
                                focusedLabelColor = TextColor,
                                cursorColor = TextColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    ResourceType.LINK -> {
                        OutlinedTextField(
                            value = linkUrl,
                            onValueChange = { linkUrl = it },
                            label = { Text("URL") },
                            placeholder = { Text("https://...") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LinkColor,
                                unfocusedBorderColor = NeonPurple.copy(alpha = 0.5f),
                                focusedLabelColor = LinkColor,
                                cursorColor = LinkColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            NeonButton(
                onClick = {
                    if (label.isNotBlank()) {
                        val value = when (selectedType) {
                            ResourceType.TEXT -> textContent
                            ResourceType.LINK -> linkUrl
                            else -> selectedUri?.toString() ?: ""
                        }
                        if (value.isNotBlank()) {
                            onConfirm(
                                selectedType,
                                value,
                                label,
                                selectedMimeType,
                                selectedFileName,
                                selectedFileSize
                            )
                        }
                    }
                },
                text = "Adicionar",
                icon = Icons.Default.Add,
                color = NeonGreen
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "CANCELAR", color = PixelRed)
            }
        }
    )
}

@Composable
fun FilePickerButton(
    label: String,
    selectedFileName: String?,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, color.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.1f),
            contentColor = color
        )
    ) {
        Icon(
            imageVector = if (selectedFileName != null) Icons.Default.CheckCircle else Icons.Default.FileOpen,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = selectedFileName ?: label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ExportDialog(
    entityName: String,
    onDismiss: () -> Unit,
    onExportAsZip: () -> Unit,
    onExportAsFolder: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        shape = RoundedCornerShape(8.dp),
        title = {
            Text(
                text = "📤 EXPORTAR",
                style = MaterialTheme.typography.titleLarge,
                color = NeonGreen
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Exportar \"$entityName\" como:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                
                // Opção ZIP
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExportAsZip() },
                    shape = RoundedCornerShape(4.dp),
                    color = NeonCyan.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderZip,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Arquivo ZIP",
                                style = MaterialTheme.typography.titleSmall,
                                color = NeonCyan
                            )
                            Text(
                                text = "Compactado, fácil de compartilhar",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
                
                // Opção Pasta
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExportAsFolder() },
                    shape = RoundedCornerShape(4.dp),
                    color = NeonPink.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonPink.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = NeonPink,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Pasta Organizada",
                                style = MaterialTheme.typography.titleSmall,
                                color = NeonPink
                            )
                            Text(
                                text = "Arquivos separados em subpastas",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "CANCELAR", color = TextSecondary)
            }
        }
    )
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}
