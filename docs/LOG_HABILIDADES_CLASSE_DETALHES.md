# Log — detalhes das habilidades de classe na aba Personagem

Data: 2026-11-15

## Decisão de interface
- Cada habilidade de classe desbloqueada é uma entrada clicável.
- A lista é agrupada por classe e nível, preservando a origem da habilidade em personagens multiclasse.
- O diálogo exibe a descrição cadastrada, o nível de aquisição e os marcos de progressão existentes no catálogo da classe.
- Marcos de progressão acima do nível atual são mostrados como futuros; marcos já alcançados recebem indicação visual.
- Quando não há outros marcos cadastrados para a habilidade, a interface informa isso explicitamente em vez de inventar uma progressão.
- O diálogo oferece um link direto para a página correspondente da classe no d20 SRD 3.5, para consulta das regras completas.

## Fonte e limites do primeiro incremento
- Referência externa: https://www.d20srd.org/srd/classes/
- O d20 SRD é usado como referência rápida para as regras das onze classes básicas.
- A descrição exibida no diálogo é o texto atualmente registrado em `Dnd35Rules.ClassFeature`; o link SRD dá acesso à redação integral da regra.
- Este incremento não altera cálculos de personagem, desbloqueios, progressão mecânica, dados salvos ou formato das fichas.
- Não considerar uma progressão inexistente no catálogo como prova de que a habilidade não evolui nas regras: lacunas do catálogo devem ser preenchidas em auditoria de conteúdo posterior.

## Validação
- PR #380: https://github.com/izzyn0okami/dnd35-app/pull/380
- Aguardar CI e revisão de compilação Android antes do merge.
