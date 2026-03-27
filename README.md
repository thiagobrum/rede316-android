# Rede 3.16 — App Android com Android Auto

App de rádio ao vivo da Rede 3.16 (Junta de Missões Nacionais — CBB) com suporte a Android Auto.

## Funcionalidades

- 🎵 **3 estações ao vivo**: Brasil (Nacional), Planalto Central (Regional), Bahia (Regional)
- 🚗 **Android Auto**: Navega e toca estações pelo painel do carro
- 🔔 **Notificação de mídia**: Controles na barra de notificação e tela de bloqueio
- 📻 **Background play**: Continua tocando com a tela desligada
- 📡 **Streams Icecast/BRLOGIC**: Conexão otimizada para live streaming
- 🕐 **Programação automática**: Mostra o programa no ar baseado no horário

## Estações

| Estação | URL do Stream |
|---------|---------------|
| Brasil (Nacional) | `https://servidor21.brlogic.com:7976/live` |
| Planalto Central | `https://servidor31.brlogic.com:7018/live` |
| Bahia | `https://servidor33.brlogic.com:7126/live` |

## Como gerar o APK

### Opção 1: GitHub Actions (recomendado)

1. Crie um repositório no GitHub
2. Suba todos os arquivos deste projeto
3. Vá em **Actions** → o workflow "Build APK" roda automaticamente
4. Baixe o APK em **Artifacts** quando o build concluir (~3 min)

### Opção 2: Codemagic.io

1. Crie conta em [codemagic.io](https://codemagic.io)
2. Conecte seu repositório GitHub
3. Configure build Android com Gradle
4. Run build → baixe o APK

### Opção 3: Android Studio (local)

1. Instale [Android Studio](https://developer.android.com/studio)
2. Abra este projeto (File → Open → selecione a pasta)
3. Build → Build APK(s)
4. O APK estará em `app/build/outputs/apk/debug/`

## Estrutura do Projeto

```
rede316-android/
├── app/src/main/
│   ├── AndroidManifest.xml          # Permissões + Android Auto
│   ├── java/br/com/rede316/app/
│   │   ├── Rede316App.java          # Application (notification channel)
│   │   ├── RadioStation.java        # Modelo de dados das estações
│   │   ├── RadioService.java        # MediaLibraryService (Auto + background)
│   │   └── MainActivity.java        # UI principal
│   └── res/
│       ├── layout/activity_main.xml # Layout da tela
│       ├── drawable/                # Ícones e backgrounds
│       ├── values/                  # Cores, strings, estilos
│       └── xml/
│           ├── automotive_app_desc.xml      # Declaração Android Auto
│           └── network_security_config.xml  # Config de rede
├── build.gradle                     # Root build
├── app/build.gradle                 # Dependencies (Media3/ExoPlayer)
├── settings.gradle
├── gradle.properties
├── gradlew                          # Gradle wrapper
└── .github/workflows/build.yml     # CI/CD automático
```

## Tecnologias

- **Media3 (ExoPlayer)** 1.2.1 — player de áudio otimizado para streaming
- **MediaLibraryService** — integração Android Auto com browsing de estações
- **MediaSession** — controles em notificação, lock screen e Android Auto
- **ConstraintLayout** — layout responsivo
- **Gradle 8.5** + **AGP 8.2.0** + **Java 17**

## Android Auto

O app aparece automaticamente no Android Auto quando instalado. O motorista vê:
- Lista de 3 estações (Brasil, Planalto Central, Bahia)
- Toque para tocar
- Controles play/pause no painel
- Artwork e nome do programa no ar

## Personalização

### Trocar o ícone do app
Substitua os arquivos em `res/mipmap-*/` e `res/drawable/ic_launcher_foreground.xml`.
Use [Android Asset Studio](https://romannurik.github.io/AndroidAssetStudio/) para gerar todos os tamanhos.

### Trocar logo
Substitua `res/drawable/ic_logo.xml` por uma imagem PNG da logo em `res/drawable/ic_logo.png`.

### Adicionar nova estação
Edite o array `ALL` em `RadioStation.java`:
```java
new RadioStation("nova_id", "Nome da Estação", "Descrição", "https://url-do-stream/live"),
```

## Licença

Desenvolvido para Rede 3.16 — Junta de Missões Nacionais da Convenção Batista Brasileira.
