# Log — Ajuda contextual do Bestiário e do Grimório

## 2026-11-14 — Glossários acessíveis dentro das abas

### Bestiário
- Adicionado botão de informação no cabeçalho do catálogo e no cabeçalho da ficha individual da criatura.
- A janela de ajuda explica abreviações e termos frequentes: Ext, Sob, SM, ND, DV, PV, CA, CA de toque, CA de surpresa, BBA, agarrar, RD, RM, Fortitude, Reflexos, Vontade, espaço e alcance.
- O glossário é apenas informativo; não altera fichas, estatísticas, filtros ou dados persistidos.

### Grimório
- Adicionado botão de informação ao cabeçalho do Grimório.
- A janela explica os componentes V (verbal), G (gestual), M (material), F (foco), FD (foco divino) e XP (custo em experiência).
- As descrições diferenciam componentes materiais consumidos de focos que permanecem após a conjuração e esclarecem a exigência de mão livre para componentes gestuais.

### Decisões de interface
- Os dois botões usam o mesmo ícone de informação e abrem um diálogo contextual específico para a aba.
- O diálogo possui rolagem vertical para telas menores.
- A ajuda não ocupa espaço permanente nas fichas e pode ser fechada pelo botão “Entendi” ou pelo gesto/botão de voltar.
- Não foram alteradas regras de jogo, catálogos, nomes persistidos ou o conteúdo das magias e criaturas.

### Arquivos
- `RulesGlossaryDialog.kt`: componente compartilhado e conteúdo dos dois glossários.
- `CreatureCatalogTab.kt`: acesso no catálogo e na ficha individual.
- `SpellsTab.kt`: acesso no cabeçalho do Grimório.

## 2026-11-14 — Ampliação do glossário do Bestiário

### Auditoria de acesso
- Confirmado que o glossário já existia no catálogo e na ficha individual, aberto por um ícone de informação.
- O acesso foi considerado pouco explícito; os dois pontos passam a exibir o rótulo textual **Glossário** junto ao ícone.
- O diálogo mantém rolagem vertical e não altera dados das criaturas.

### Conteúdo acrescentado
- Revisadas as definições de Ext, Sob e SM e suas interações com resistência à magia, dissipação, campo antimagia e ataques de oportunidade.
- Acrescentadas explicações para habilidades recorrentes: veneno, doença, paralisia, olhar, sopro, agarrar aprimorado, constrição, bote, rasgar, dilacerar, atropelar, engolfar, engolir inteiro, teia, dreno de sangue, medo, dreno de energia, dano/dreno de atributo e invocação.
- Acrescentadas explicações para sentidos, defesas e estados especiais: visão no escuro, visão na penumbra, sentido cego, percepção às cegas, faro, sentido sísmico, RD, RM, resistência/imunidade/vulnerabilidade a energia, cura acelerada, regeneração, evasão, evasão aprimorada, incorpóreo, forma alternativa, telepatia, psiquismo, resistência à expulsão e características de morto-vivo.
- Incluídas orientações sobre CD, nível de conjurador, usos por dia, habilidades naturais e diferença entre ataques especiais e qualidades especiais.
- Os verbetes particulares de cada criatura continuam sendo a autoridade para valores, alcance, frequência, CD, duração e exceções. O glossário não substitui a descrição específica de habilidades únicas.

### Referências
- d20 SRD 3.5 — Special Abilities: https://www.d20srd.org/srd/specialAbilities.htm
- SRD 3.5 — Special Abilities and Conditions: https://palikhov.github.io/srd-v3.5/basic-rules-and-legal/special-abilities-and-conditions.html
- SRD 3.5 — Types, Subtypes, and Special Abilities: https://palikhov.github.io/srd-v3.5/basic-rules-and-legal/types-subtypes-and-special-abilities.html

### Escopo e segurança das regras
- Conteúdo de referência traduzido e resumido em português; não é transcrição literal.
- Nenhuma ficha, estatística, regra de cálculo ou registro do catálogo de criaturas foi alterado.
- O glossário é informativo e contextual; quando uma habilidade tiver regra própria no verbete do monstro, prevalece a descrição daquela habilidade.

### Correção após validação visual — detalhe em tela cheia
- A captura enviada mostrou que a ficha aberta a partir do fluxo de sessão/seleção de monstros usa `CreatureDetailsOverlay`, compartilhado por `CreatureDeckOverlay`, e não o `CreatureDetail` da aba do catálogo.
- O botão textual **Glossário** foi adicionado ao cabeçalho desse overlay compartilhado, ao lado do botão Fechar.
- O botão abre o mesmo glossário do Bestiário, sem fechar a ficha e sem alterar o fluxo de seleção.
- O cabeçalho compartilhado recebeu uma ação opcional; os demais usos do overlay permanecem sem alteração visual.

### Correções de compilação após CI (2026-11-15)
- A primeira validação da PR #379 falhou em `compileDebugKotlin`.
- Causa: import incorreto de `rememberSaveable` (deve vir de `androidx.compose.runtime.saveable`) e importação `IconButton` removida do catálogo, embora ainda usada na navegação de detalhe.
- Corrigidas ambas as importações. A CI deve ser executada novamente antes do merge.
- O erro adicional de chamada Composable foi reportado no mesmo trecho de `IconButton` e será reavaliado após corrigir a importação.
