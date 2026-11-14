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

## 2026-11-14 — Frente reorganizada como Resumo de Combate

### Decisão do usuário
- A frente contém PV, CA, BBA, deslocamento, ataques/magias e rolagens.
- Iniciativa, resistências, atributos, perícias, talentos, qualidades e demais dados passam para o verso ou ficha completa.
- A carta pode crescer para preservar legibilidade.

### Implementação
- Cartas ampliadas para 300 × 500 dp e pilha para 520 dp.
- Frente reorganizada com PV, CA, BBA e deslocamento em destaque; ataques e ataque total em linhas próprias.
- Ataques com bônus numérico identificável oferecem rolagem d20 pelo diálogo existente; o dano descrito é encaminhado ao mesmo diálogo.
- Ataques especiais e habilidades mágicas disponíveis no catálogo aparecem em área própria.
- Iniciativa, resistências, atributos, CA de toque/surpresa, agarrar, espaço/alcance, qualidades, perícias, talentos e referências passam para o verso.

### Limites de dados
- `CreatureDefinition` não possui uma lista estruturada independente de magias preparadas/conhecidas nem CDs por magia. A frente mostra o texto mágico já disponível em ataques especiais; não infere magias ou CDs.
- O botão de rolagem aparece somente quando o texto do ataque contém um bônus numérico identificável. Em ataques múltiplos, a rolagem usa o primeiro bônus encontrado na linha.

### Validação pendente
- CI Android e testes automatizados.
- Conferência visual no aparelho, especialmente para ataques múltiplos e textos extensos de habilidades mágicas.
