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

## 2026-11-14 — Correção da falha de compilação na reorganização da frente

- A CI identificou interpolação Kotlin escapada indevidamente em 26 expressões `\${...}` no componente de cartas; duas linhas de atributos também geravam erro sintático por causa desse escape.
- Corrigidas as expressões para interpolação Kotlin normal (`${...}`), restaurando a renderização dinâmica dos dados e eliminando os erros de sintaxe reportados nas linhas 293–296.
- Confirmado no código-fonte que `CreatureDefinition` fornece os campos utilizados pela carta e que `DiceRollDialog` aceita os parâmetros usados.
- A correção está no commit `0aaefae6abfaf2ceaef87d183ee8066d7e4bbcca`; CI/build desse commit em execução no momento do registro.


## 2026-11-14 — Simplificação da frente: sem rolagens nas cartas

### Decisão
- As cartas são um resumo visual de combate; as rolagens não são necessárias nesse contexto.
- Removidos os botões de rolagem dos ataques e ataques totais para liberar espaço.
- Mantidas as descrições textuais completas de Ataque e Ataque total, sem alterar bônus, dano ou estatísticas do catálogo.
- Removidos o estado temporário de rolagem, a análise de bônus/dano e a dependência de DiceRollDialog do componente de cartas.
- A rolagem continua disponível nas áreas próprias do aplicativo; esta mudança é restrita às cartas.

### Validação
- Aguardando CI Android e testes automatizados nesta alteração.
