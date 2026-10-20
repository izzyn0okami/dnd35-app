# Ficha D&D 3.5 — Android

Aplicativo Android nativo para criar e gerenciar fichas de personagens de Dungeons & Dragons 3.5.

## Tecnologias

- Kotlin e Jetpack Compose / Material 3
- Room / SQLite para persistência local
- Kotlin Coroutines
- Exportação de ficha em PDF

## Estrutura principal

- `model/Dnd35Rules.kt`: tabelas e definições de regras
- `model/CalculatedCharacterStats.kt`: cálculo das estatísticas derivadas
- `model/CharacterModels.kt`: entidades e modelos de dados
- `model/CharacterJsonAdapters.kt`: serialização de coleções em JSON
- `data/`: banco, DAO e repositório
- `viewmodel/CharacterViewModel.kt`: estado e operações de personagem
- `ui/`: tela principal, abas e diálogos
- `pdf/CharacterPdfExporter.kt`: exportação de PDF

## Compilação

### Requisitos

- Android Studio atualizado, com Android SDK Platform 36.1 (ou a plataforma exigida pelo `compileSdk`).
- JDK 21 ou a versão exigida pela versão do Android Gradle Plugin configurada.
- Acesso à internet na primeira execução para baixar o Gradle e as dependências.

### Linux / macOS

Na raiz do projeto:

```bash
chmod +x gradlew
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

### Windows

Na raiz do projeto, no Prompt de Comando ou PowerShell:

```bat
gradlew.bat assembleDebug
gradlew.bat testDebugUnitTest
```

Os scripts `gradlew` e `gradlew.bat` incluídos neste pacote são lançadores de bootstrap: baixam a distribuição oficial Gradle 9.3.1 e a armazenam no diretório do usuário. Eles não dependem de `gradle-wrapper.jar`. O primeiro uso exige conexão com `services.gradle.org` e ferramentas de extração disponíveis no sistema.

Também é possível abrir a pasta no Android Studio e usar a sincronização Gradle. Se a compilação falhar por falta de SDK, instale no SDK Manager a plataforma e as ferramentas Android exigidas pelo `app/build.gradle.kts`.

O projeto não inclui credenciais privadas, arquivo `google-services.json` nem keystore de publicação. O plugin Google Services está configurado para emitir aviso quando o JSON estiver ausente; serviços que dependam de configuração Firebase podem não funcionar sem configuração própria. A compilação de release assinada requer uma keystore válida e as variáveis de ambiente descritas em `.env.example`.

## Correções

Consulte [`RELATORIO_CORRECOES.md`](RELATORIO_CORRECOES.md) para o escopo das correções, limitações de validação e próximos passos.
