# Ficha D&D 3.5 — Android

Aplicativo Android nativo para criar, consultar e gerenciar fichas de personagens de **Dungeons & Dragons 3.5**, com armazenamento local e foco no uso à mesa.

> **Estado do projeto:** candidato à primeira versão beta pública. O rótulo GitHub `v0.0.1-beta` ainda precisa ser publicado após validar a compilação, os testes e o APK distribuível.

## Recursos disponíveis

- **Fichas de personagem:** criação e edição de personagens, com dados e estatísticas derivados da ficha.
- **Regras de D&D 3.5:** catálogos e cálculos de apoio para classes, atributos, perícias, talentos e magias, com auditoria priorizando a edição brasileira do *Livro do Jogador*.
- **Perícias e talentos:** acompanhamento de graduações e sinalização visual de pré-requisitos de talentos sem impedir que o usuário registre uma escolha.
- **Grimório:** catálogo de magias e cálculo automático de espaços diários e magias conhecidas para as classes conjuradoras implementadas, incluindo multiclasse.
- **Combate:** consulta às estatísticas relevantes da ficha.
- **Inventário e equipamento:** itens do catálogo e personalizados, equipamento, carga/mochila e controle de munição por pilha e por pacote.
- **Itens personalizados com fotos:** imagens salvas no armazenamento privado do aplicativo e associadas ao item.
- **Exportação:** geração de ficha em PDF.
- **Uso offline:** os dados da ficha são armazenados localmente no dispositivo. O aplicativo não depende de Firebase ou de um serviço em nuvem para as funções atuais.

## Limites conhecidos desta beta

- Esta versão é um **companion de ficha em um dispositivo**. Sessões entre vários celulares, QR Code, sincronização local entre jogadores, aprovação de ações pelo Mestre, mensagens privadas e baú compartilhado são ideias para uma etapa futura; **não estão anunciados como funcionalidades já disponíveis**.
- A validação de regras é assistiva: o aplicativo pode sinalizar informações, mas não pretende substituir o julgamento do grupo ou do Mestre.
- Antes de distribuir um APK, valide a compilação e os testes na CI e faça um teste prático no Android. A aprovação dos testes automatizados não substitui a verificação de comportamento no aparelho.
- A keystore de publicação não é incluída no repositório. Uma versão assinada exige configurar `KEYSTORE_PATH`, `STORE_PASSWORD` e `KEY_PASSWORD` (alias `upload`).

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

Também é possível abrir a pasta no Android Studio e usar a sincronização Gradle. Se a compilação falhar por falta de SDK, instale no SDK Manager a plataforma e as ferramentas Android exigidas por `app/build.gradle.kts`.

## Histórico de correções e auditorias

Consulte [`RELATORIO_CORRECOES.md`](RELATORIO_CORRECOES.md) para o histórico de correções, decisões de auditoria, limitações conhecidas e próximos passos.
