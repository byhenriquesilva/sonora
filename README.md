# 🎵 Sonora - Player de Música Moderno para Android

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="Sonora Logo" width="120" height="120" style="border-radius: 24px;" />
</p>

<p align="center">
  <b>Sonora</b> é um reprodutor de áudio local e offline para Android com interface moderna, fluida e imersiva construída com <b>Jetpack Compose</b> e <b>Material Design 3</b>. Inspirado na ergonomia de aplicativos contemporâneos, possui menus e miniplayer flutuantes sobrepostos ao conteúdo, animações suaves e gerenciamento completo de biblioteca musical.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android&logoColor=white" alt="Platform" />
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white" alt="Language" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white" alt="UI" />
  <img src="https://img.shields.io/badge/Audio-Media3%20%2F%20ExoPlayer-FF6F00?style=flat" alt="Media3" />
  <img src="https://img.shields.io/badge/Database-Room-009688?style=flat" alt="Room" />
  <img src="https://img.shields.io/badge/Design-Material%203-6750A4?style=flat" alt="M3" />
</p>

---

## ✨ Principais Funcionalidades

### 🎧 Reprodução e Controle de Áudio Avançado
- **Engine Media3 / ExoPlayer**: Reprodução offline de alta performance com suporte a diversos formatos de áudio (MP3, FLAC, AAC, WAV, OGG, etc.).
- **Miniplayer Flutuante**: Acesso rápido à música atual em qualquer tela, com botões de Play/Pause, Próxima faixa, barra de progresso em miniatura e toque para expandir.
- **Player em Tela Cheia (*Full Player*)**:
  - Exibição destacada da arte do álbum com efeito de iluminação ambiente.
  - Controles completos de reprodução: Play/Pause, Próxima, Anterior e saltos de 10 segundos.
  - Modos de repetição (*Desativado*, *Repetir Tudo*, *Repetir Atual*) e embaralhamento (*Shuffle*).
  - Barra de progresso interativa com busca em tempo real (*seek*) e minutagem precisa.
- **Crossfade Suave**: Transição contínua entre faixas com duração personalizável (0 a 12 segundos), eliminando silêncios abruptos.
- **Timer de Sono (*Sleep Timer*)**: Programador de desligamento automático com opções rápidas (15, 30, 45, 60 minutos, final da música atual ou tempo personalizado).
- **Fila Dinâmica de Reprodução**: Visualização, controle e gerenciamento da ordem das músicas em reprodução.

---

### 🎨 Design Flutuante & Ergonomia (Material 3)
- **Navegação Flutuante Real**: A barra de navegação e o miniplayer flutuam de forma autêntica sobre a tela, permitindo que as listas de faixas e álbuns rolem por trás dos controles.
- **Espaçamento Reativo Inteligente**:
  - Quando o **Miniplayer está ativo**, a base da lista recebe **230.dp** de folga, garantindo que o último item fique 100% visível acima dos controles flutuantes.
  - Quando o **Miniplayer está inativo**, a folga se reduz de forma reativa e animada pela metade (**115.dp**).
- **Superfícies Opacas e Nítidas**: Elementos elevados em superfícies sólidas do Material 3 com contornos definidos e sombras suaves para máxima legibilidade.
- **Layout Adaptativo**: Suporte completo para telas compactas (smartphones) e telas expandidas (tablets/dobráveis) através de uma **NavigationRail** lateral ergonômica.

---

### 📚 Gerenciamento Completo de Biblioteca
- **Escaneamento Automático**: Leitura das faixas do armazenamento local do dispositivo através do `MediaStore` do Android.
- **Navegação por Abas**:
  - **Músicas**: Lista de todas as músicas locais com ordenação por título, artista, data de adição ou duração.
  - **Álbuns**: Grade com capas em alta resolução e quantidade de faixas por álbum.
  - **Artistas**: Grade de artistas com foto circular e agrupamento de obras.
  - **Pastas**: Navegação fiel à estrutura de diretórios do armazenamento do dispositivo.
  - **Favoritos**: Acesso imediato a todas as músicas marcadas como favoritas.
- **Playlists Customizadas**:
  - Criação de playlists ilimitadas com nomes personalizados.
  - Adição e remoção rápida de faixas via menu contextual.
  - Modos "Tocar Tudo" e "Embaralhar" por playlist.
  - Renomeação e exclusão de listas.
- **Busca Global Instantânea**: Pesquisa em tempo real com filtros rápidos por músicas, artistas e álbuns simultaneamente.

---

### 📊 Estatísticas e Histórico de Audição
- **Painel de Estatísticas**:
  - Tempo total de reprodução acumulado.
  - Contadores de faixas mais tocadas e artistas mais ouvidos.
  - Histórico cronológico de reprodução com opção para limpar histórico.

---

## 🛠️ Arquitetura e Tecnologias

O projeto segue as melhores práticas recomendadas pelo Google para desenvolvimento Android moderno:

| Camada | Tecnologia / Biblioteca | Função |
|---|---|---|
| **Linguagem** | Kotlin 2.x | Desenvolvimento robusto e tipagem estática |
| **Interface (UI)** | Jetpack Compose + Material 3 | Construção de UI declarativa, fluida e reativa |
| **Arquitetura** | MVVM (Model-View-ViewModel) + Clean Architecture | Separação de responsabilidades e desacoplamento |
| **Reprodução de Áudio** | AndroidX Media3 / ExoPlayer | Engine de reprodução e gerenciamento de sessões de mídia |
| **Banco de Dados** | Room Database | Persistência local para playlists, favoritos, histórico e contadores |
| **Carregamento de Imagens** | Coil (Compose) | Carregamento assíncrono com cache de capas de álbuns |
| **Concorrência** | Coroutines & Kotlin Flow | Fluxos assíncronos e reatividade de estado |
| **Gerenciamento de Estado** | StateFlow & CompositionLocalProvider | Propagação de estados globais (ex.: `LocalBottomContentPadding`) |

---

## 📂 Estrutura do Projeto

```text
app/src/main/java/com/example/
├── MainActivity.kt               # Ponto de entrada, Scaffold, Navegação e Miniplayer Flutuante
├── data/
│   ├── local/                    # Entidades e DAOs do Room (Playlists, Histórico, Favoritos)
│   ├── model/                    # Modelos de domínio (Song, Album, Artist, Folder, Playlist)
│   └── repository/               # Repositórios de dados e leitor do MediaStore
├── service/                      # Integração com ExoPlayer / Media3 para reprodução de áudio
└── ui/
    ├── components/               # Componentes reutilizáveis
    │   ├── FloatingAcrylicNavBar.kt   # Barra de navegação flutuante M3
    │   ├── MiniPlayer.kt              # Miniplayer flutuante com controles rápidos
    │   ├── LocalBottomContentPadding.kt # Provedor de espaçamento dinâmico reativo
    │   ├── SonoraAlbumArt.kt          # Renderizador de arte de álbum com fallback
    │   └── Dialogs.kt                 # Diálogos de playlists, timer e detalhes
    ├── screens/                  # Telas do aplicativo
    │   ├── HomeScreen.kt              # Tela inicial com atalhos e recentes
    │   ├── ExploreScreen.kt           # Descoberta, gêneros e recomendações
    │   ├── LibraryScreen.kt           # Biblioteca (Músicas, Álbuns, Artistas, Pastas)
    │   ├── PlaylistsScreen.kt         # Gerenciamento de playlists do usuário
    │   ├── FavoritesScreen.kt         # Lista de músicas marcadas como favoritas
    │   ├── FullPlayerScreen.kt        # Reprodutor em tela cheia imersivo
    │   ├── SearchScreen.kt            # Busca instantânea com filtros
    │   ├── StatsScreen.kt             # Métricas e histórico de audição
    │   ├── SettingsScreen.kt          # Configurações de áudio, tema e biblioteca
    │   └── DetailScreens.kt           # Detalhes de artista, álbum e pasta
    ├── theme/                    # Cores, Tipografia, Formas e Esquemas do Material 3
    └── viewmodel/                # MusicViewModel gerenciando o estado central do aplicativo
```

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
- **Android Studio** Ladybug (ou versão mais recente)
- **Android SDK** com `minSdk 26` e `compileSdk 35`
- **JDK 17** ou superior

### Passos de Instalação
1. Clone o repositório:
   ```bash
   git clone https://github.com/henrysilva1707/sonora.git
   cd sonora
   ```

2. Abra o projeto no **Android Studio**.

3. Aguarde a sincronização do Gradle (todas as dependências necessárias serão resolvidas automaticamente).

4. Conecte um dispositivo Android físico com depuração USB habilitada ou inicialize um emulador Android.

5. Clique no botão **Run** (`Shift + F10`) no Android Studio ou execute via terminal:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 🔒 Permissões Utilizadas

- `READ_MEDIA_AUDIO` (Android 13+ / API 33+) e `READ_EXTERNAL_STORAGE` (Android 12 e anteriores): Necessárias para indexar e reproduzir as faixas de áudio armazenadas localmente no seu aparelho.
- `POST_NOTIFICATIONS` (Android 13+): Necessária para exibir os controles de reprodução na barra de notificações e na tela de bloqueio.
- `FOREGROUND_SERVICE` e `FOREGROUND_SERVICE_MEDIA_PLAYBACK`: Permite a reprodução contínua em segundo plano enquanto a tela está bloqueada ou outro app está em uso.

---

## 📄 Licença

Este projeto é disponibilizado sob a licença [MIT](LICENSE).
Sinta-se à vontade para utilizar, modificar e contribuir!

