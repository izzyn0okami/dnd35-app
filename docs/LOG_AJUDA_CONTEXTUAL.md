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
