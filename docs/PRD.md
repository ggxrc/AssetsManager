# PRD - Assets Manager (Game Assets Organizer)

## 📋 Visão Geral do Produto

**Nome:** Assets Manager  
**Versão:** 1.0  
**Plataforma:** Android (minSdk 24, targetSdk 36)  
**Stack Tecnológico:** Kotlin, Jetpack Compose, Room Database, MVVM Architecture, Navigation Compose

### Descrição
O Assets Manager é um aplicativo Android para **organização e documentação de assets de projetos de jogos**. Ele permite que desenvolvedores, artistas e game designers cataloguem, agrupem e gerenciem todos os recursos (sprites, animações, sons, textos, links) associados às entidades de um jogo.

### Problema que Resolve
Projetos de jogos frequentemente possuem centenas ou milhares de assets dispersos em pastas, sem uma forma centralizada de documentar suas relações com personagens, inimigos, itens ou cenários. O Assets Manager resolve isso criando uma **hierarquia organizada** de Categorias → Entidades → Recursos.

---

## 🎯 Objetivos do Produto

### Objetivos Primários
1. Permitir organização hierárquica de assets de jogos
2. Documentar entidades com nome, descrição e recursos associados
3. Facilitar a visualização e navegação entre assets relacionados
4. Oferecer exportação/download de entidades com todos seus recursos

### Objetivos Secundários
1. Trabalhar offline (persistência local com Room)
2. Interface intuitiva e moderna com Material Design 3
3. Suporte a múltiplos tipos de recursos (imagens, áudio, texto, links)

---

## 👥 Público-Alvo

| Persona | Descrição | Necessidade |
|---------|-----------|-------------|
| **Game Developer** | Desenvolvedor indie ou de estúdio | Organizar assets por personagem/sistema |
| **Game Designer** | Designer de jogos | Documentar lore, stats e comportamentos |
| **Artista 2D/3D** | Criador de arte para jogos | Agrupar sprites e animações por entidade |
| **Sound Designer** | Criador de áudio para jogos | Associar sons a entidades específicas |

---

## 🏗️ Arquitetura do Sistema

### Modelo de Dados (Hierarquia de 3 Níveis)

```
📁 Category (Nível 1)
├── 👤 GameEntity (Nível 2)
│   ├── 🖼️ EntityResource - IMAGE
│   ├── 🎵 EntityResource - AUDIO
│   ├── 📝 EntityResource - TEXT
│   └── 🔗 EntityResource - LINK
│
├── 👤 GameEntity
│   └── ...
```

### Entidades do Banco de Dados (Room)

#### 1. Category (Categoria)
```kotlin
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)
```
**Propósito:** Agrupador de alto nível (ex: "NPCs", "Inimigos", "Itens", "Cenários")

#### 2. GameEntity (Entidade de Jogo)
```kotlin
@Entity(
    tableName = "game_entities",
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val entityId: Int = 0,
    val categoryId: Int,
    val name: String,
    val description: String
)
```
**Propósito:** Representa uma entidade do jogo (ex: "João", "Goblin", "Espada Flamejante")

#### 3. EntityResource (Recurso)
```kotlin
@Entity(
    tableName = "entity_resources",
    foreignKeys = [ForeignKey(
        entity = GameEntity::class,
        parentColumns = ["entityId"],
        childColumns = ["ownerId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class EntityResource(
    @PrimaryKey(autoGenerate = true) val resourceId: Int = 0,
    val ownerId: Int,
    val type: String,    // "IMAGE", "AUDIO", "TEXT", "LINK"
    val value: String,   // URI ou conteúdo textual
    val label: String    // Nome legível do recurso
)
```
**Propósito:** Qualquer asset associado a uma entidade

---

## 📱 Telas do Aplicativo

### 1. CategoryScreen (Tela Inicial)
**Rota:** `categories`

| Elemento | Descrição |
|----------|-----------|
| TopAppBar | Título: "Categorias de Assets" |
| LazyColumn | Lista de categorias com ícone de pasta |
| FAB (+) | Abre dialog para criar nova categoria |
| Ação Delete | Ícone de lixeira para excluir categoria |

**Fluxo:**
- Usuário visualiza todas as categorias
- Toque em categoria → Navega para EntityScreen
- Toque em (+) → Dialog de criação
- Toque em 🗑️ → Remove categoria (cascade remove entidades e recursos)

### 2. EntityScreen (Lista de Entidades)
**Rota:** `entities/{catId}`

| Elemento | Descrição |
|----------|-----------|
| TopAppBar | Nome da categoria + botão voltar |
| LazyColumn | Lista de entidades com nome e descrição |
| FAB (+) | Abre dialog para criar nova entidade |
| Ação Delete | Ícone de lixeira para excluir entidade |

**Fluxo:**
- Usuário visualiza entidades da categoria selecionada
- Toque em entidade → Navega para ResourceScreen
- Campos de criação: Nome e Descrição

### 3. ResourceScreen (Detalhes da Entidade)
**Rota:** `resources/{entityId}` *(a implementar)*

| Elemento | Descrição |
|----------|-----------|
| TopAppBar | Nome da entidade + botão voltar |
| LazyColumn | Lista de recursos agrupados por tipo |
| FAB (+) | Abre dialog para adicionar recurso |
| Seletor de Tipo | IMAGE, AUDIO, TEXT, LINK |

**Tipos de Recursos:**
- 🖼️ **IMAGE:** Sprites, texturas, ícones
- 🎵 **AUDIO:** Sons, músicas, efeitos
- 📝 **TEXT:** Lore, diálogos, descrições extensas
- 🔗 **LINK:** URLs para referências externas

---

## ✅ Funcionalidades Implementadas (Status Atual)

| Feature | Status | Descrição |
|---------|--------|-----------|
| CRUD Categorias | ✅ Completo | Criar, listar, deletar categorias |
| CRUD Entidades | ✅ Completo | Criar, listar, deletar entidades |
| CRUD Recursos | ✅ Completo | Criar, listar, deletar recursos |
| Navegação Compose | ✅ Parcial | Falta navegação para ResourceScreen |
| Persistência Room | ✅ Completo | Banco de dados local SQLite |
| Reatividade | ✅ Completo | Flow/LiveData para atualizações automáticas |

---

## 🚀 Funcionalidades Futuras (Backlog)

### Prioridade Alta 🔴

#### 1. Navegação Completa para ResourceScreen
**Descrição:** Atualmente, o clique em uma entidade não navega para a tela de recursos.

**Código atual (GameNavHost.kt):**
```kotlin
onEntityClick = { } // Vazio!
```

**Implementação necessária:**
```kotlin
composable(
    route = "resources/{entityId}",
    arguments = listOf(navArgument("entityId") { type = NavType.IntType })
) { backStackEntry ->
    val entityId = backStackEntry.arguments?.getInt("entityId") ?: 0
    LaunchedEffect(entityId) { viewModel.selectEntity(entityId) }
    
    ResourceScreen(
        viewModel = viewModel,
        onBack = { navController.popBackStack() }
    )
}
```

#### 2. Download/Exportação de Entidade
**Descrição:** Permitir exportar uma entidade completa (JSON + arquivos) para compartilhamento.

**Formato de Exportação Proposto:**
```
João_export/
├── metadata.json       # Nome, descrição, lista de recursos
├── sprites/
│   ├── idle.png
│   └── walk.png
├── audio/
│   └── voice_01.mp3
└── text/
    └── lore.txt
```

#### 3. Importação de Assets via Gallery/Files
**Descrição:** Usar `ActivityResultContracts.GetContent()` para permitir seleção de arquivos reais do dispositivo.

### Prioridade Média 🟡

#### 4. Preview de Imagens
**Descrição:** Exibir thumbnail das imagens cadastradas diretamente na lista de recursos usando `Coil` ou `Glide`.

#### 5. Busca/Filtro Global
**Descrição:** Campo de busca para encontrar entidades ou recursos por nome.

#### 6. Tags/Labels Personalizadas
**Descrição:** Adicionar sistema de tags para facilitar organização transversal.

**Nova Entidade:**
```kotlin
@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true) val tagId: Int = 0,
    val name: String,
    val color: String // Hex color
)

@Entity(tableName = "entity_tags") // Many-to-Many
data class EntityTag(
    val entityId: Int,
    val tagId: Int
)
```

#### 7. Edição de Entidades/Recursos
**Descrição:** Atualmente só existe criação e deleção. Adicionar funcionalidade de Update.

### Prioridade Baixa 🟢

#### 8. Temas Personalizados
**Descrição:** Permitir escolha entre Light/Dark mode e cores de destaque.

#### 9. Backup na Nuvem
**Descrição:** Integração com Google Drive ou Firebase para backup automático.

#### 10. Compartilhamento entre Dispositivos
**Descrição:** Sincronização via Firebase Realtime Database ou Cloud Firestore.

#### 11. Campos Customizados por Entidade
**Descrição:** Permitir que o usuário defina campos extras (Stats, HP, Mana, etc).

**Nova Estrutura:**
```kotlin
data class CustomField(
    val fieldId: Int,
    val entityId: Int,
    val fieldName: String,  // "HP", "Mana", "Speed"
    val fieldType: String,  // "NUMBER", "STRING", "BOOLEAN"
    val fieldValue: String
)
```

---

## 🔧 Melhorias Técnicas Sugeridas

### 1. Migração para StateFlow
Substituir LiveData por StateFlow para código mais idiomático com Compose:
```kotlin
// Antes
val allCategories: LiveData<List<Category>> = repository.allCategories.asLiveData()

// Depois
val allCategories: StateFlow<List<Category>> = repository.allCategories
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
```

### 2. Injeção de Dependências com Hilt
Substituir a inicialização manual no `MainActivity` por Hilt:
```kotlin
@HiltAndroidApp
class AssetsManagerApp : Application()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()
}
```

### 3. Sealed Classes para Navegação
```kotlin
sealed class Screen(val route: String) {
    object Categories : Screen("categories")
    data class Entities(val catId: Int) : Screen("entities/$catId")
    data class Resources(val entityId: Int) : Screen("resources/$entityId")
}
```

### 4. Tratamento de Erros
Adicionar estados de Loading/Error/Success:
```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

---

## 📊 Exemplo de Uso (User Story)

### Cenário: Documentando o Personagem "João"

1. **Usuário abre o app** → Vê lista de categorias vazia
2. **Cria categoria "Personagens"** → Toque no FAB (+)
3. **Entra na categoria** → Toque em "Personagens"
4. **Cria entidade "João"** → Preenche nome e descrição:
   - Nome: "João"
   - Descrição: "Protagonista do jogo, guerreiro medieval"
5. **Entra na entidade** → Toque em "João"
6. **Adiciona recursos:**
   - 🖼️ IMAGE: "Sprite Idle" → `content://galeria/joao_idle.png`
   - 🖼️ IMAGE: "Sprite Walk" → `content://galeria/joao_walk.png`
   - 🎵 AUDIO: "Voz de Ataque" → `content://audio/joao_attack.mp3`
   - 📝 TEXT: "Lore" → "João nasceu em uma vila pacífica..."
   - 🔗 LINK: "Referência Visual" → `https://pinterest.com/medieval-knight`
7. **Exporta entidade** → Gera ZIP com todos os assets organizados

---

## 📁 Estrutura de Arquivos do Projeto

```
app/src/main/java/com/ads/assetsmanager/
├── MainActivity.kt                 # Activity principal
├── data/
│   ├── dao/
│   │   └── GameDao.kt              # Operações de banco de dados
│   ├── database/
│   │   └── GameDatabase.kt         # Configuração Room
│   ├── model/
│   │   ├── Category.kt             # Entidade: Categoria
│   │   ├── CategoryWithEntities.kt # Relação 1:N
│   │   ├── GameEntity.kt           # Entidade: Item de Jogo
│   │   ├── EntityResource.kt       # Entidade: Recurso
│   │   └── EntitiesWithResources.kt# Relação 1:N
│   └── repository/
│       └── GameRepository.kt       # Camada de abstração
├── ui/
│   ├── screens/
│   │   ├── CategoryScreen.kt       # Tela de categorias
│   │   ├── EntityScreen.kt         # Tela de entidades
│   │   ├── ResourceScreen.kt       # Tela de recursos
│   │   └── GameNavHost.kt          # Navegação Compose
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── viewmodel/
    ├── GameViewModel.kt            # Lógica de apresentação
    └── GameViewModelFactory.kt     # Factory para ViewModel
```

---

## 📝 Notas de Implementação

### Dependências Principais
- **Room:** 2.x (Persistência SQLite)
- **Compose Navigation:** Navegação declarativa
- **Material3:** Design system
- **Lifecycle:** ViewModel + LiveData/Flow

### Constraints
- **minSdk:** 24 (Android 7.0)
- **targetSdk:** 36 (Android 15)
- **Linguagem:** Kotlin 100%

---

## 🎯 Métricas de Sucesso

| Métrica | Meta |
|---------|------|
| Tempo para criar entidade completa | < 2 minutos |
| Tempo de carregamento inicial | < 1 segundo |
| Crash rate | < 0.1% |
| Retenção D7 | > 30% |

---

## 📅 Roadmap Sugerido

### Fase 1 - MVP (Atual)
- [x] CRUD Categorias
- [x] CRUD Entidades
- [x] CRUD Recursos
- [ ] Navegação completa para ResourceScreen

### Fase 2 - Usabilidade
- [ ] Preview de imagens
- [ ] Busca global
- [ ] Edição de registros

### Fase 3 - Exportação
- [ ] Exportar entidade como ZIP
- [ ] Importar assets do dispositivo
- [ ] Compartilhar via Intent

### Fase 4 - Avançado
- [ ] Tags e filtros
- [ ] Campos customizados
- [ ] Backup na nuvem

---

*Documento criado em: 01/12/2024*  
*Última atualização: 01/12/2024*  
*Versão do App: 1.0*
