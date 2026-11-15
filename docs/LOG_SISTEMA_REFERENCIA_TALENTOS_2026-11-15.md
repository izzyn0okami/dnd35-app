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


## Separação entre resumo da ficha e referência completa (15/11/2026)

- O primeiro diálogo repetia os campos `summary` e `benefit` do catálogo de ficha. Isso não atendia ao objetivo de oferecer uma consulta realmente explicativa.
- Criados catálogos independentes: `feat_references.json` e `class_feature_references.json`. Eles não substituem nem alteram os textos curtos exibidos na ficha.
- Cada referência detalhada tem campos próprios para funcionamento, regra normal, casos especiais, ativação, duração, restrições, progressão e fonte/página, conforme o tipo de entrada.
- O diálogo de talentos agora apresenta pré-requisitos e, quando auditado, o texto expandido; não repete o resumo/benefício da ficha. Entradas ainda não auditadas são identificadas como pendentes, em vez de receberem um texto curto disfarçado de explicação completa.
- O glossário de habilidades de classe segue a mesma regra: a ficha mantém a descrição curta e o glossário exibe campos detalhados separados; verbetes pendentes são identificados.
- Referências conferidas no PDF brasileiro do Livro do Jogador:
  - Talentos: Ataque Poderoso (p. 78), Foco em Arma (p. 81), Esquiva (p. 81), Iniciativa Aprimorada (p. 82), Combater com Duas Armas (p. 78), Mobilidade (p. 83), Tiro Preciso (p. 85), Vitalidade (p. 86) e Ataque em Movimento (p. 75).
  - Bárbaro: Fúria (pp. 21–22), Esquiva Sobrenatural, Sentir Armadilhas, Esquiva Sobrenatural Aprimorada e Redução de Dano (p. 23).
- Cobertura atual deste incremento: 9 de 110 talentos e 5 referências de habilidades do Bárbaro. As demais entradas continuam disponíveis na busca, mas aparecem explicitamente como pendentes de auditoria detalhada.
- A falha inicial dos testes do glossário foi causada por o novo teste usar JUnit puro ao acessar o catálogo JSON que depende do runtime Android/Robolectric. O teste agora usa `RobolectricTestRunner`, como o teste existente de carregamento dos catálogos. Não foi alterado o carregador de produção.
- Incluídos testes para confirmar que Ataque Poderoso tem referência mais extensa que o resumo da ficha e que Fúria do Bárbaro aponta para as regras detalhadas e páginas corretas.
- Não foram alterados cálculos, progressões, pré-requisitos automáticos, persistência ou formato de ficha.
- CI e compilação Android devem ser executados novamente; não mesclar enquanto ambos não passarem.


## Ampliação da auditoria detalhada (15/11/2026)

- A cobertura de talentos foi ampliada de 9 para **25 referências detalhadas de 110 entradas** do catálogo. As 85 restantes continuam marcadas na interface como pendentes, sem reutilizar o texto curto da ficha.
- Novos talentos auditados: Agarrar Aprimorado, Desviar Objetos, Trespassar, Fintar Aprimorado, Magia Natural, Acuidade com Arma, Especialização em Combate, Especialização em Arma, Tiro Certeiro, Tiro Longo, Combater com Duas Armas Maior, Tiro Rápido, Desarme Aprimorado, Lutar às Cegas, Ataque Giratório e Reflexos de Combate, além dos nove já registrados acima.
- Corrigido o nome do talento no catálogo para **Reflexos de Combate**, conforme a nomenclatura do Livro do Jogador brasileiro. A mudança afeta apenas o nome apresentado no catálogo de seleção; não altera cálculos ou bônus de fichas existentes.
- A cobertura de habilidades de classe foi ampliada para **44 referências detalhadas**, abrangendo as 11 classes básicas: Bárbaro, Bardo, Clérigo, Druida, Feiticeiro, Guerreiro, Ladino, Mago, Monge, Paladino e Ranger.
- Foram acrescentadas referências de Música de Bardo, Forma Selvagem, Inimigo Predileto, Estilo de Combate, companheiro animal do ranger, habilidades centrais do paladino, monge, ladino, clérigo, mago e feiticeiro, além de talentos bônus do guerreiro.
- Corrigido o rótulo da habilidade do ranger para **Maestria do Estilo de Combate**, conforme o texto do livro.
- A busca de habilidades de classe agora prefere a referência de nome mais específico quando há nomes sobrepostos (por exemplo, Esquiva Sobrenatural e Esquiva Sobrenatural Aprimorada), evitando exibir o verbete errado.
- Validação estrutural dos JSONs: 25 referências de talentos correspondem a nomes existentes no catálogo; nenhuma duplicidade ou campo obrigatório ausente foi encontrada. As 44 referências de classe possuem chaves únicas e campos obrigatórios preenchidos.
- Foram adicionados testes de regressão para as referências de bárbaro, bardo, druida, ranger, paladino, monge, ladino, clérigo, feiticeiro, mago e guerreiro, além da separação entre resumo e texto detalhado dos talentos.
- Ainda falta executar e aprovar CI e compilação Android no commit final. O PR permanece aberto e não deve ser mesclado enquanto os dois fluxos não passarem.
