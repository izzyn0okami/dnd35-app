# Log de implementação — Fichas do Livro dos Monstros

## 2026-11-12 — Estrutura visual inicial

### Decisões consolidadas
- Não pesquisar, baixar ou inserir imagens automaticamente.
- Reservar em cada ficha uma área visual vazia para imagem, que será escolhida manualmente pelo usuário em etapa futura.
- Reaproveitar o catálogo `CreatureCatalog` e seus 230 registros existentes; não duplicar dados.
- Manter a terminologia e a ordem conceitual do Livro dos Monstros 3.5 brasileiro.
- A tela de bestiário deve funcionar mesmo quando não houver personagem criado ou selecionado.

### Implementado nesta etapa
- Nova aba **Monstros** na navegação principal.
- Busca por nome e filtros combináveis por tipo e tamanho.
- Lista resumida com nome, tamanho, tipo e Nível de Desafio.
- Ficha detalhada com área reservada à imagem e os campos atualmente disponíveis no modelo:
  - Dados de Vida e PV;
  - Iniciativa e Deslocamento;
  - Classe de Armadura, incluindo CA de toque e surpresa no texto original;
  - Ataque Base/Agarrar, Ataque e Ataque Total;
  - Espaço/Alcance, Ataques Especiais e Qualidades Especiais;
  - Testes de Resistência;
  - seis Habilidades;
  - Perícias e Talentos;
  - Ambiente, Organização, Nível de Desafio, Tesouro, Tendência, Progressão e Ajuste de Nível.
- A navegação para a aba Monstros não depende da existência de ficha de personagem.

### Referência de regras e estrutura
Conferência inicial no PDF brasileiro fornecido, Introdução, páginas impressas 5–6 (páginas do arquivo PDF 9–10): o verbete apresenta bloco de estatísticas com nome, tamanho/tipo, Dados de Vida, Iniciativa, Deslocamento, CA, Ataque Base/Agarrar, Ataque, Ataque Total, Espaço/Alcance, ataques/qualidades especiais, resistências, habilidades, perícias, talentos, ambiente, organização, ND, tesouro, tendência, progressão e ajuste de nível. O livro também prevê texto descritivo e explicação de Combate após o bloco.

### Pendências conhecidas — não tratar como concluídas
- Extrair e conferir descrição narrativa e seção Combate de cada criatura, sem completar lacunas por inferência.
- Representar variantes/versões alternativas quando houver múltiplos blocos de estatísticas no mesmo verbete.
- Tornar o espaço de imagem capaz de receber e persistir uma imagem local escolhida pelo usuário; nesta etapa ele é apenas um placeholder.
- Auditar a cobertura do catálogo de 230 registros contra o índice/listagem do livro. A contagem atual não comprova, sozinha, cobertura integral do livro.
- Criar testes de interface e executar a suíte Gradle/CI.

### Regra contra redundância
Não reimplementar a listagem, busca, filtros por tipo/tamanho ou os campos mecânicos já presentes nesta etapa. Próximas alterações devem avançar nas pendências acima e atualizar este log.
