# Log — sistema de referência e glossário de talentos

Data: 2026-11-15

## Escopo implementado
- Criado índice de pesquisa textual do catálogo de talentos do Livro do Jogador.
- Pesquisa por nome, pré-requisitos, resumo e benefício.
- Busca sem distinção entre maiúsculas/minúsculas e acentuação.
- Resultados priorizam correspondência exata e início do nome.
- Criado diálogo de glossário com lista de resultados e visualização detalhada.
- A ficha permite abrir o glossário pelo botão de consulta e tocar no nome de um talento registrado para abrir a entrada correspondente.
- No catálogo de inclusão, tocar no nome abre a referência detalhada; tocar no cartão continua adicionando o talento, preservando o fluxo atual.

## Referência bibliográfica
- Livro do Jogador brasileiro, Capítulo 5 — Talentos, páginas impressas 73–89.
- O catálogo consultado é `app/src/main/resources/dnd35/feats.json`, já usado pelo app como catálogo do Livro do Jogador.
- A referência é identificada no glossário como Livro do Jogador, Capítulo 5. O conteúdo apresentado é o cadastro atual do app, não uma reprodução integral do texto editorial.

## Limites preservados
- A avaliação de pré-requisitos continua informativa: não bloqueia inclusão, edição ou manutenção de talentos na ficha.
- Talentos personalizados continuam editáveis e não são inseridos automaticamente no catálogo oficial.
- Nenhum cálculo, regra mecânica, dado persistido ou formato de ficha foi alterado.
- A pesquisa é textual e não afirma validar automaticamente escolhas que a ficha não registra (por exemplo, arma ou escola específica).

## Validação pendente
- Executar testes unitários, CI e compilação Android.
- A auditoria de fidelidade de cada benefício e pré-requisito do catálogo continua sendo uma etapa separada; esta alteração implementa o sistema de consulta, não certifica individualmente todos os verbetes.
