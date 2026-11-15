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


## 2026-11-14 — Ampliação vertical da área de arte

### Alterações
- Aumentada a altura da ilustração frontal de 104 dp para 150 dp, dando mais destaque visual à criatura.
- Aumentada a altura total da carta de 500 dp para 540 dp.
- Aumentada a área da pilha de 520 dp para 560 dp e ajustada a altura das cartas sobrepostas para acompanhar a nova dimensão.
- Mantidos os espaços destinados aos atributos de combate e às informações textuais, evitando que o aumento da arte os comprima.

### Validação
- Alteração agrupada na PR #374; aguardando CI Android e testes automatizados.

## 2026-11-14 — Frente orientada ao combate e ilustração ampliada

### Alterações
- Ampliada a ilustração frontal de 150 dp para 210 dp.
- Aumentada a altura da carta para 600 dp e da área da pilha para 620 dp, acompanhando a nova composição.
- Priorizados na frente PV, CA, iniciativa e deslocamento.
- Incluídos Fortitude, Reflexos e Vontade em uma segunda linha compacta.
- Mantidos Ataque, Ataque total e ataques especiais quando existentes, sem botões de rolagem.
- Retirado o BBA da frente para evitar redundância com os bônus já descritos nos ataques; BBA e agarrar permanecem nas informações do verso.
- Preservados o verso, a ficha completa por pressão prolongada, a seleção e a navegação por gesto.
- Aplicado no componente compartilhado, abrangendo Bestiário, invocações e companheiros animais.

### Validação
- Aguardando CI Android e testes automatizados da PR #374.
- A conferência visual em aparelho continua necessária para confirmar enquadramento da arte, legibilidade e ausência de cortes em cartas com ataques especiais extensos.


## 2026-11-15 — Ampliação da ilustração e largura responsiva das cartas

### Alterações
- Aumentada a área de ilustração da frente de 210 dp para 300 dp, mantendo o enquadramento por recorte e os dados essenciais de combate.
- Aumentada a altura da carta de 600 dp para 690 dp e a área da pilha de 620 dp para 710 dp, evitando comprimir os atributos e textos restantes.
- Removida a largura fixa de 300 dp das cartas empilhadas; agora elas acompanham a largura útil disponível, evitando corte em aparelhos estreitos.
- Mantidos PV, CA, iniciativa, deslocamento, resistências, ataques e informações especiais na frente.
- Confirmado que o componente compartilhado de cartas de criaturas não contém botões de rolagem. Os controles de rolagem da seção própria de armas/ataques da ficha do personagem permanecem independentes.

### Escopo e validação
- Alteração de apresentação compartilhada pelo Bestiário, seleção de criaturas, familiares e companheiros animais.
- Nenhum dado, regra, cálculo, persistência ou comportamento de seleção/virada da carta foi alterado.
- CI Android, testes unitários e geração do APK debug obrigatórios antes do merge.
- Conferência visual manual continua recomendada em telas estreitas e em cartas com ataques especiais longos.
