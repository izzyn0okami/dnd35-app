# Log — ícones individuais de magias

## Decisão e escopo (2026-11-09)

- Manter os nove ícones por escola já implementados na PR #347.
- Acrescentar uma camada de ícones específicos por nome de magia, sem gravar qualquer chave no JSON da ficha, banco de dados ou backup.
- Priorizar os recursos Game-icons.net já empacotados e creditados; não criar SVGs novos nesta etapa.
- Usar o ícone da escola como fallback quando não houver associação individual adequada.
- Preservar o nome textual da escola nas linhas e nos detalhes da magia.
- A apresentação é estritamente visual: não altera regras, filtros, catálogo, nível, classe, descrição ou comportamento de conjuração.

## Implementação

- Criado `spellIconKey(spellName)` com associações explícitas para efeitos reconhecíveis.
- Criado `SpellIcon`, que resolve ícone específico e aplica fallback pela escola.
- Aplicado em Preparadas, Conhecidas, Catálogo e detalhes da magia.
- Testes verificam chaves selecionadas, existência dos recursos e cobertura/fallback para todas as magias do catálogo atual (393 entradas).
- Créditos atualizados em `docs/ICONES_CREDITOS.md`.

## Limitações conhecidas

- Nem todo efeito do Livro do Jogador possui um símbolo inequívoco entre os 277 recursos atualmente empacotados.
- Magias sem associação específica continuam representadas pela escola; não atribuir um desenho apenas para aumentar a quantidade de associações.
- Uma expansão posterior com novos desenhos exige selecionar SVGs do Game-icons.net, conferir autor/licença, atualizar o gerador e os créditos e validar os VectorDrawables.


## Segunda etapa da auditoria (2026-11-09)

- Comparação automatizada das chaves de associação com os 393 nomes do catálogo atual.
- Removidas chaves sem correspondência no catálogo; não foram adicionadas magias ao catálogo nesta etapa.
- Expandidas as associações específicas para efeitos de influência, percepção, proteção, deslocamento, invocação, conjuração, energia, clima, natureza, objetos e efeitos de Bigby.
- Cobertura antes desta etapa: 310 magias com ícone individual e 83 com fallback de escola.
- Terceira etapa: adicionadas associações semanticamente diretas para aprisionamento, bênçãos, névoa, detecção de mortos-vivos e tendências, invisibilidade, fortalecimento, escalada, dissipação, efeitos divinos, visão, teleporte, desejo e desintegração.
- Cobertura após esta etapa: 362 magias com ícone individual e 31 com fallback de escola.
- Testes rejeitam nomes de magia sem correspondência e chaves de ícone inexistentes. A terceira etapa preserva o fallback para efeitos sem símbolo visual inequívoco.
- O Livro do Jogador brasileiro confirma que Mãos Flamejantes produz um cone de chamas; seu ícone representa fogo em área, não um ataque de toque.

## Quarta etapa — revisão dos fallbacks (2026-11-09)

- Conferidos os 31 nomes que ainda usavam o ícone da escola após a terceira etapa, comparando os verbetes e efeitos no Livro do Jogador brasileiro com os 277 recursos visuais já registrados.
- Acrescentadas 21 associações quando havia um símbolo existente razoavelmente direto: truques e marcas, pontos de vida temporários, queda/deslocamento, invisibilidade, pedras arremessáveis, superfície escorregadia, proteção contra medo, disfarce de aura, transformação animal, natureza, bloqueio de detecção, raio de frio, convocação planar e vidência.
- Mantidos 10 fallbacks por ausência de símbolo suficientemente específico: Orientação, Intuir Direção, Servo Invisível, Presa Mágica, Disco Flutuante de Tenser, Apagar, Sabedoria da Coruja, Presa Mágica Maior, Repelir Insetos e Destruição Rastejante.
- Nenhum recurso SVG novo foi criado; todos os identificadores usados já existem em GameIcons.
- A cobertura passa de 362 para 383 magias com ícone individual; 10 permanecem com o ícone da escola.
- O escopo continua exclusivamente visual. Os nomes, descrições, níveis, classes e demais regras do catálogo não foram alterados.
