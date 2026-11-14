# Log de UI — Cartas de criaturas

## 2026-11-14 — Cartas maiores e frente mais informativa

### Escopo
Melhoria do componente compartilhado nas cartas do Bestiário, seleção de criaturas para invocação e cartas de familiares/companheiros animais.

### Alterações
- Aumentada a altura das cartas de 350 dp para 390 dp e a largura de 250 dp para 265 dp.
- Aumentada a área da pilha de cartas de 370 dp para 410 dp, mantendo a navegação por gesto horizontal.
- Preservados nome, tipo/tamanho, ND, ilustração, contexto e resumo de PV/CA/iniciativa na frente.
- Acrescentadas informações de deslocamento, testes de resistência e ataque, usando dados já existentes em CreatureDefinition.
- Ajustados limites de linhas para acomodar os dados adicionais sem alterar o verso ou a ficha completa.
- A ação de seleção, o gesto de virar e a pressão longa para abrir a ficha completa permanecem inalterados.

### Decisões
- Nenhuma estatística ou dado do catálogo foi alterado.
- O componente compartilhado mantém a mesma apresentação no Bestiário, nas invocações e nos vínculos.

### Validação
- A conferência visual em aparelho depende da instalação do APK desta branch.
- Compilação Android e testes automatizados serão verificados pela CI da PR.
