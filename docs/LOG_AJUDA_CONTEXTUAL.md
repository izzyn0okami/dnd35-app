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

## 2026-11-14 — Ampliação do glossário do Bestiário

### Auditoria de acesso
- Confirmado que o glossário já existia no catálogo e na ficha individual, aberto por um ícone de informação.
- O acesso foi considerado pouco explícito; os dois pontos passam a exibir o rótulo textual **Glossário** junto ao ícone.
- O diálogo mantém rolagem vertical e não altera dados das criaturas.

### Conteúdo acrescentado
- Revisadas as definições de Ext, Sob e SM e suas interações com resistência à magia, dissipação, campo antimagia e ataques de oportunidade.
- Acrescentadas explicações para habilidades recorrentes: veneno, doença, paralisia, olhar, sopro, agarrar aprimorado, constrição, bote, rasgar, dilacerar, atropelar, engolfar, engolir inteiro, teia, dreno de sangue, medo, dreno de energia, dano/dreno de atributo e invocação.
- Acrescentadas explicações para sentidos, defesas e estados especiais: visão no escuro, visão na penumbra, sentido cego, percepção às cegas, faro, sentido sísmico, RD, RM, resistência/imunidade/vulnerabilidade a energia, cura acelerada, regeneração, evasão, evasão aprimorada, incorpóreo, forma alternativa, telepatia, psiquismo, resistência à expulsão e características de morto-vivo.
- Incluídas orientações sobre CD, nível de conjurador, usos por dia, habilidades naturais e diferença entre ataques especiais e qualidades especiais.
- Os verbetes particulares de cada criatura continuam sendo a autoridade para valores, alcance, frequência, CD, duração e exceções. O glossário não substitui a descrição específica de habilidades únicas.

### Referências
- d20 SRD 3.5 — Special Abilities: https://www.d20srd.org/srd/specialAbilities.htm
- SRD 3.5 — Special Abilities and Conditions: https://palikhov.github.io/srd-v3.5/basic-rules-and-legal/special-abilities-and-conditions.html
- SRD 3.5 — Types, Subtypes, and Special Abilities: https://palikhov.github.io/srd-v3.5/basic-rules-and-legal/types-subtypes-and-special-abilities.html

### Escopo e segurança das regras
- Conteúdo de referência traduzido e resumido em português; não é transcrição literal.
- Nenhuma ficha, estatística, regra de cálculo ou registro do catálogo de criaturas foi alterado.
- O glossário é informativo e contextual; quando uma habilidade tiver regra própria no verbete do monstro, prevalece a descrição daquela habilidade.

### Correção após validação visual — detalhe em tela cheia
- A captura enviada mostrou que a ficha aberta a partir do fluxo de sessão/seleção de monstros usa `CreatureDetailsOverlay`, compartilhado por `CreatureDeckOverlay`, e não o `CreatureDetail` da aba do catálogo.
- O botão textual **Glossário** foi adicionado ao cabeçalho desse overlay compartilhado, ao lado do botão Fechar.
- O botão abre o mesmo glossário do Bestiário, sem fechar a ficha e sem alterar o fluxo de seleção.
- O cabeçalho compartilhado recebeu uma ação opcional; os demais usos do overlay permanecem sem alteração visual.

### Correções de compilação após CI (2026-11-15)
- A primeira validação da PR #379 falhou em `compileDebugKotlin`.
- Causa: import incorreto de `rememberSaveable` (deve vir de `androidx.compose.runtime.saveable`) e importação `IconButton` removida do catálogo, embora ainda usada na navegação de detalhe.
- Corrigidas ambas as importações. A CI deve ser executada novamente antes do merge.
- O erro adicional de chamada Composable foi reportado no mesmo trecho de `IconButton` e será reavaliado após corrigir a importação.


## 2026-11-17 — Auditoria comparativa com o glossário do Livro dos Monstros

### Fonte primária
- PDF brasileiro do Livro dos Monstros 3.5 fornecido na biblioteca, Capítulo 7 — Glossário (páginas impressas 305–318), além da seção de criação de monstros sobre ataques especiais e qualidades especiais.
- O texto foi extraído diretamente do PDF local para comparação com o glossário já existente no aplicativo.
- O Livro dos Monstros distingue ataques especiais (capacidades ofensivas) de qualidades especiais (defesas, sentidos, formas de movimento, resistências, vulnerabilidades e outras capacidades inatas). Características de tipo/subtipo permanecem uma terceira categoria e não devem ser convertidas em habilidades individuais.

### Resultado da comparação com o app
- A ajuda contextual do Bestiário já continha a maior parte das explicações. Esta etapa complementa o conteúdo existente, sem criar um segundo glossário nem duplicar fichas.
- Faro: acrescentados os alcances básicos oficiais (9 m; 18 m a favor do vento; 4,5 m contra o vento), multiplicadores para odores fortes/extremos, distinção entre detectar presença, determinar direção e localizar a fonte, e regra de rastreamento (CD 10 para trilha recente, +2 por hora).
- Faro Aprimorado: documentado como variante dependente do verbete, com o exemplo dos tubarões (60 m para detectar criaturas e 1,5 km para sangue na água), sem tratar esses valores como universais.
- Sentido Cego: explicitadas a linha de efeito, a camuflagem total contra alvos não percebidos, as chances de falha e a perda do bônus de Destreza na CA.
- Percepção às Cegas: explicitada a maior precisão em relação ao Sentido Cego, a capacidade de manobrar/lutar normalmente, a linha de efeito e a ativação quando indicada.
- Acrescentada a distinção de Enxergar no Escuro (Sob), que não deve ser confundido com Visão no Escuro comum.
- Acrescentadas entradas para Imunidade à Magia, Ataque Visual, Ataques Sônicos, Ataques de Enxame, Ferimento Amaldiçoado, Sangramento, Raio/Rugido e Magias/Habilidades Similares à Magia.
- Entradas cujo nome aparece na lista de ataques especiais, mas cuja regra varia entre criaturas, foram deliberadamente descritas como dependentes do verbete. Não foram inventadas regras universais para Raio, Rugido, Sangramento ou Ferimento Amaldiçoado.

### Regras de interpretação preservadas
- A descrição individual do monstro continua prevalecendo para CD, alcance, dano, frequência, duração, recarga, imunidades e exceções.
- Faro Aprimorado é apresentado com valores específicos de cada criatura, não como um alcance geral fixo.
- Sentido Cego e Percepção às Cegas não são sinônimos e não concedem os mesmos benefícios.
- Imunidade à Magia não é equivalente a Resistência à Magia: a primeira não pode ser superada por teste de nível de conjurador, dentro de seu escopo.
- Nenhuma ficha estatística, criatura, regra de cálculo ou dado persistido foi alterado. Mudança restrita ao texto informativo do glossário.

### Arquivos
- `RulesGlossaryDialog.kt`: complementadas as entradas do glossário do Bestiário.


## 2026-11-17 — Exemplos práticos para habilidades especiais

### Critério adotado
- Quando o Livro dos Monstros apresenta um exemplo concreto, o glossário usa a própria criatura e preserva os valores, CDs e efeitos do texto brasileiro.
- Quando o livro não fornece um exemplo aplicável ou a habilidade varia entre criaturas, o texto do app deve usar um cenário hipotético claramente identificado como exemplo prático, sem apresentá-lo como regra oficial.
- Exemplos são explicativos; não criam novos efeitos, bônus, CDs, alcances ou exceções.

### Exemplos acrescentados com base no livro
- Imunidade à Magia: golem de barro e suas exceções específicas contra mover terra e desintegrar.
- Sentido Cego: enxame de morcegos, alcance de 6 m e a distinção entre perceber o alvo e ainda sofrer limitações de camuflagem.
- Percepção às Cegas: exemplo didático de ecolocalização de golfinho, sem atribuir alcance ou bônus novos.
- Faro: exemplo prático de um lobo farejando aventureiros a favor do vento, aplicando o alcance de 18 m e a distinção entre presença e localização.
- Psiquismo: exemplo de githzerai com psiquismo 3/dia e efeitos listados em sua ficha, reforçando que o verbete pode impor limite diário.
- Ataque Visual: ghaele em forma humanoide, alcance de 18 m, CD 18 e efeitos diferenciados por tendência e Dados de Vida.
- Ataques Sônicos: rugido do leonal, cone de 18 m, efeitos de palavra sagrada e 2d6 de dano sônico adicional, com Fortitude CD 20 para anular.
- Ataques de Enxame e Sangramento: enxame de morcegos, dano de 1d6 e perda de 1 PV por rodada; Cura CD 10 ou magia de cura interrompe o sangramento.
- Ferimento Amaldiçoado: golem de barro; o ferimento não se recupera naturalmente, resiste a magias de cura e exige teste de nível de conjurador CD 26 para curar ferimentos ou cura completa.
- Raio e Rugido: ghaele (raio de luz a 100 m, 2d12 de dano) e andro-esfinge (três rugidos por encontro, com efeitos diferentes).

### Validação de conteúdo
- Os exemplos específicos foram conferidos nos verbetes correspondentes do PDF brasileiro do Livro dos Monstros, não apenas no glossário.
- O exemplo de Percepção às Cegas foi redigido como situação didática, sem inventar estatísticas.
- Não foi criado um exemplo genérico para Ferimento Amaldiçoado, Raio ou Rugido que pudesse ser confundido com regra universal; foram usados exemplos de criaturas nomeadas.
- Nenhuma ficha, estatística, regra de cálculo ou dado persistido foi alterado. A mudança continua restrita à ajuda contextual.

### Arquivos
- `RulesGlossaryDialog.kt`: exemplos oficiais e exemplos práticos identificados como tais.


## 2026-11-17 — Segunda etapa: habilidades adicionais do glossário oficial

### Fonte primária
- Livro dos Monstros 3.5 brasileiro, Capítulo 7 — Glossário, páginas impressas 305–318, conferido no PDF fornecido.
- Comparação feita com o conteúdo atualmente integrado em `RulesGlossaryDialog.kt`; PR #394 já foi mesclada antes desta etapa.

### Lacunas identificadas e corrigidas
- **Alterar Forma (Sob)** e **Mudar Forma (Sob)** agora são verbetes separados. O texto diferencia a troca de características físicas e especiais de Alterar Forma da mudança de aparência de Mudar Forma, que preserva os valores de habilidades originais. Exemplos oficiais: ghaele alternando entre forma humanoide e globo incorpóreo; aranea assumindo uma forma humanoide e perdendo o uso de mordida, teia e veneno.
- **Distração (Ext)**: explicada a condição de enjoo, o teste de Fortitude e a exigência de Concentração para conjurar ou manter concentração dentro de um enxame; exemplo com um aventureiro cercado por morcegos.
- **Presença Aterradora (Ext)**: explicados gatilhos, área, limite de DV/nível, teste de Vontade, duração geral e imunidade por 24 horas após sucesso; exemplo de um rugido de dragão.
- **Voo (Ext ou Sob)**: explicado que decolar e aterrissar são ações livres e que o voo sobrenatural é suprimido em campo antimagia; exemplo comparando voo Ext e Sob.
- **Dreno de Energia**: ampliados os efeitos dos níveis negativos, PV temporários recebidos pelo atacante e teste após 24 horas.
- **Paralisia**: explicitadas a incapacidade de realizar ações físicas, ausência de novo teste por rodada e consequências para criaturas voadoras ou nadadoras.
- **Medo e Desespero**: diferenciadas as formas de aura, cone e raio, com referência ao desespero das múmias.
- **Ataque Visual**: incluído o momento do teste por turno e a possibilidade de ativação deliberada como ação de ataque; preservado o exemplo específico do ghaele (18 m, Vontade CD 18 e medo por 2d10 rodadas).
- **Invocar**: acrescentadas a duração usual de uma hora, a restrição de invocação da criatura chamada e a ausência de XP para criaturas invocadas.

### Regras de fidelidade
- Valores gerais foram mantidos como apresentados no glossário do livro; o verbete individual continua prevalecendo quando especifica exceções.
- Exemplos não apresentados como regra oficial foram identificados como práticos/hipotéticos e não criam valores estatísticos novos.
- Não foram alterados dados, estatísticas, fichas ou lógica de combate do catálogo.

### Arquivos
- `RulesGlossaryDialog.kt`: separa as duas formas e acrescenta as habilidades que faltavam, além de aprofundar definições existentes.


### Cruzamento adicional com habilidades nomeadas nos verbetes
- **Sopro (Sob)**: incluída a CD básica (10 + metade dos DV raciais + modificador de Constituição), a regra geral de Reflexos para reduzir o dano à metade e a imunidade da criatura ao próprio sopro, salvo exceção.
- **Sentido Aquático (Ext)**: exemplo do sahuagin, que localiza criaturas submersas a 9 m como Sentido Cego, somente debaixo d'água.
- **Frenesi de Sangue**: exemplo do sahuagin, com ativação após sofrer dano, frequência diária, bônus de Constituição/Força, penalidade na CA e impossibilidade de encerrar voluntariamente.
- **Sensibilidade à Água Doce (Ext)**: regra específica do sahuagin, Fortitude CD 15 e repetição a cada 10 minutos de imersão.
- **Cegueira sob Luz (Ext)**: regra específica do sahuagin, cegueira por 1 rodada e ofuscamento enquanto permanecer sob luz brilhante.
- **Falar com Tubarões (Ext)**: comunicação telepática do sahuagin a até 50 m, limitada a conceitos simples.
- **Nuvem de Tinta (Ext)**: explicada como qualidade variável por criatura; exemplo do kraken (24 m de dispersão) e menção às versões diferentes de lulas e tojanidas.
- **Propulsão (Ext)**: explicada como deslocamento retilíneo para trás em ação de rodada completa sem ataques de oportunidade; exemplos de lula (72 m) e kraken (84 m).
- Os valores e condições foram conferidos diretamente nos verbetes do sahuagin, kraken, lula e tojanida no PDF brasileiro. Os exemplos não transformam essas habilidades específicas em regras universais.


## 2026-11-17 — Terceira etapa: habilidades específicas dos verbetes de criaturas

### Método de cruzamento
- Recontagem do catálogo atual: 320 registros em `app/src/main/resources/dnd35/creatures.json`.
- Comparação dos campos de ataques especiais e qualidades especiais com os termos já explicados em `RulesGlossaryDialog.kt`.
- Os nomes encontrados foram conferidos nos verbetes correspondentes do PDF brasileiro do Livro dos Monstros fornecido na biblioteca. Foram descartados valores de dano, CDs e expressões que eram apenas partes de estatísticas ou texto mal separado no catálogo.
- A regra particular de cada criatura foi mantida como exemplo identificado; nenhuma mecânica foi generalizada quando os verbetes apresentavam versões diferentes.

### Entradas acrescentadas
- **Nuvem Negra** — achaierai: dano e teste de Fortitude contra insanidade.
- **Drenar Sabedoria** — comparação entre allip e lamia, diferenciando dano permanente e recuperação de PV temporários.
- **Sensibilidade à Luz** — esclarece que a condição varia entre espécies e entre luz solar/luz intensa.
- **Aura de Proteção** — exemplo dos anjos, com alcance, bônus defensivos e recriação após dissipação.
- **Aura Antinatural** — reação de animais à presença das aparições.
- **Aparência Horripilante** — comparação entre bruxa do mar e fantasma, sem unificar atributos afetados.
- **Ladrar** — cão yeth, alcance, CD, duração e imunidade temporária ao mesmo usuário.
- **Dança da Destruição** — vrocks, requisito de grupo, preparação, área, dano e interrupção.
- **Esporos** — vrock, dano inicial e recorrente, duração e meios de interromper/destruir os esporos.
- **Insanidade** — característica racial dos derro, com substituição do modificador de Vontade e imunidades.
- **Enxurrada** — elemental da água, distinguindo chamas mundanas de fogo mágico.
- **Deslizar na Terra** — elemental da terra, deslocamento através do solo e interação específica com mover terra.
- **Incendiar** — elemental do fogo, dano misto e teste para evitar pegar fogo.
- **Possessão** — fantasma etéreo, condição de manifestação, ingresso no espaço e resistência da vítima.
- **Toque da Corrupção** — fantasma, dano e diferença de modificadores contra alvos etéreos/corpóreos.
- **Sentir Eletricidade** — lagarto elétrico, detecção de descargas num raio de 30 m.
- **Toque Paralisante** — comparação entre mohrg e lich, diferenciando duração e forma de remoção.
- **Mimetismo** — mímico, limites de volume e teste resistido para perceber o disfarce.
- **Patas de Aranha** — capacidade de escalar superfícies como a magia homônima.
- **Nuvem de Visco** e **Muco** — abolete, com efeitos separados de respiração e transformação da pele.

### Decisões de precisão
- As entradas com valores numéricos preservam a criatura de origem e não estabelecem valores universais.
- Drenar Sabedoria, Aparência Horripilante, Sensibilidade à Luz e Toque Paralisante têm efeitos diferentes conforme o monstro; os exemplos permanecem separados.
- Aura de Proteção não foi confundida com Aura Antinatural; Nuvem de Visco não foi confundida com Nuvem de Tinta.
- A lista não transforma características de tipo/subtipo, valores de resistência ou danos de ataques em habilidades independentes.
- A alteração é exclusivamente textual e informativa. Nenhum bloco de estatísticas, cálculo, ficha de personagem ou dado persistido foi modificado.

### Arquivos
- `RulesGlossaryDialog.kt`: 47 entradas específicas adicionadas ao glossário do Bestiário.


### Complemento do cruzamento: outras habilidades encontradas nos blocos e verbetes
- **Murmúrio** e **Enlouquecer** — allip: efeito hipnótico em área e dano temporário de Sabedoria contra conjuradores que tentem ler/controlar sua mente.
- **Adesão** — stirge e arminho atroz: fixação no corpo da vítima, com regras próprias para remover a criatura.
- **Aderente** — mímico e kuo-toa: adesivo corporal ou aplicado ao escudo; CDs e formas de remoção permanecem vinculadas ao exemplo.
- **Aura de Ameaça** — arcontes: penalidade contra criaturas hostis, com CD variável por espécie.
- **Raios Ópticos** — gauth e beholder: efeitos distintos por raio, quantidade, alcance e CD diferentes.
- **Salto** — bulette: ataque com quatro garras em substituição ao padrão de duas, sem mordida na mesma rodada.
- **Tentáculos Paralisantes** — chuul: aprisionamento e paralisia com Fortitude CD 19, além do dano das mandíbulas.
- **Destruição** — nalfeshnee: explosão de luz profana com Vontade CD 22.
- **Ferimento Infernal**, **Barba** e **Frenesi de Batalha** — diabo barbado; ferimento persistente, doença transmitida pela barba e frenesi com duração e bônus específicos.
- **Raio Elétrico** — gavião-seta: rajada uma vez por rodada, alcance 15 m.
- **Raio de Luz** — arconte luminar: alcance 10 m e capacidade de ignorar RD.
- **Beijo** — vargouille: transformação iniciada contra vítima paralisada, com Fortitude CD 15.
- **Trinado** — verme do gelo: efeito sônico em 30 m, Vontade CD 17 e duração ligada à emissão do som.
- **Passeio Etéreo** — salteadores/saqueadores etéreos: deslocamento entre os Planos Etéreo e Material, com janela de permanência própria.
- **Dano Dobrado contra Objetos** — entes e árvores animadas: aplica-se a ataques totais contra objetos e estruturas, não contra criaturas.
- **Arrebatar** — dragão-tartaruga: agarrar e manter vítimas presas, com possibilidade de arremesso.
- **Maestria Aquática**, **Maestria Aérea** e **Maestria Terrestre** — modificadores condicionais de combate dos elementais, dependentes do ambiente e da posição dos oponentes.
- **Vórtice** e **Vendaval** — transformações especiais dos elementais da água e do ar, com frequência e duração vinculadas aos Dados de Vida.
- **Empurrar** — elemental da terra: inicia Encontrão sem provocar ataque de oportunidade.
- **Toque Pútrido** — múmia: doença sobrenatural que também funciona como maldição, com exigências próprias para magias de cura.

### Precisões adicionais
- Raios Ópticos não foram descritos como uma lista fixa de efeitos: o gauth e o beholder têm quantidades, alcances, CDs e efeitos próprios.
- Adesão e Aderente foram mantidas separadas porque o livro usa os nomes para mecanismos diferentes em criaturas diferentes.
- Ferimento Infernal não foi fundido com Ferimento Amaldiçoado: ambos causam dano persistente, mas têm origem e condições de remoção distintas.
- Maestrias e transformações elementais mantêm os modificadores e limites específicos do Livro dos Monstros.
- Total acumulado nesta etapa: 47 novas entradas específicas, além do glossário geral já existente.


## 2026-11-17 — Complemento: descargas dos lagartos elétricos

### Fonte primária e conferência
- Livro dos Monstros 3.5 brasileiro, verbete **Lagarto Elétrico**, seção Combate e descrições das habilidades especiais (PDF fornecido; páginas impressas do verbete).
- O texto foi conferido diretamente no PDF: Choque Atordoante permite um disparo por rodada contra um único alvo a 1,5 m, causa 2d8 de dano por contusão, Reflexos CD 12 reduz à metade e a CD depende de Constituição.
- Choque Letal exige pelo menos dois lagartos a até 6 m; afeta raio de 6 m centrado em qualquer participante, causa 2d8 por lagarto participante (máximo 12d8) e permite Reflexos para metade, com CD 10 + quantidade de participantes.

### Entradas acrescentadas
- **Choque Atordoante (Sob)** — explica frequência, alvo, alcance, dano por contusão, resistência e exemplo numérico.
- **Choque Letal (Sob)** — explica a cooperação entre lagartos, requisito de proximidade, área, dano escalonado, limite máximo, CD variável e exemplo com três participantes (6d8, CD 13).

### Decisões de precisão
- As duas habilidades são distintas: Choque Atordoante é um ataque individual; Choque Letal é uma descarga combinada em área.
- Não foi inferido que o Choque Letal atinja automaticamente todos os lagartos participantes; o texto preserva a área descrita no livro.
- A entrada **Sentir Eletricidade** permanece separada: detectar descargas num raio de 30 m não concede detecção geral de criaturas ou objetos.
- Nenhuma estatística do catálogo, regra de combate ou dado persistido foi alterado; somente texto explicativo do glossário.

### Arquivos
- `RulesGlossaryDialog.kt`: duas entradas específicas do lagarto elétrico.


## 2026-11-17 — Quarta etapa: habilidades adicionais e correção de alcance do Faro

### Fonte primária
- Livro dos Monstros 3.5 brasileiro, verbetes de ente, bruxa da noite, vultos noturnos, magmin, slaad vermelho, bebilith, andarilho noturno, bisão, tubarão atroz, monstro da ferrugem, sátiro, belker, destrachan, devorador de aranhas e xill.
- Os detalhes abaixo foram conferidos diretamente no PDF fornecido. Quando a mesma habilidade aparece em espécies diferentes, os exemplos e valores foram mantidos vinculados ao monstro de origem.

### Entradas acrescentadas
- **Animar Árvores (SM)** — ente: alcance de 54 m, até duas árvores, ação para desenraizar, deslocamento, controle e vulnerabilidade a fogo.
- **Assombrar os Sonhos (Sob)** — bruxa da noite: invasão de sonhos de criaturas Caóticas ou Más, condição etérea, perda de 1 ponto de Constituição e forma de impedir a intrusão.
- **Aura Degradante (Sob)** — vultos noturnos: emanação profana de 6 m, bônus para mortos-vivos, penalidade em tentativas de expulsão e interação com áreas consagradas/santificadas.
- **Aura Ígnea (Ext)** e **Combustão (Ext)** — magmin: efeitos diferentes; dano recorrente por proximidade versus dano por toque e incêndio de materiais.
- **Aversão à Luz do Dia (Ext)** — vultos noturnos: penalidade sob luz natural do dia, que não é ativada pela magia luz do dia.
- **Coaxar Atordoante (Sob)** — slaad vermelho: uso diário, área, Fortitude CD 16 e atordoamento de 1d3 rodadas.
- **Destruir Armaduras (Ext)** — bebilith: exige acerto com as duas garras e danifica a armadura, não o usuário diretamente.
- **Drenar Magia (Sob)** — vulto alado: reduz bônus de melhoria de itens mágicos e pode neutralizar habilidades especiais.
- **Esmagar Itens (Sob)** — andarilho noturno: limitações de tamanho, manobra Desarmar e Fortitude CD 34 do item.
- **Estouro da Manada (Ext)** — bisões: fuga em grupo, atropelamento e dano escalonado por quantidade de animais.
- **Faro Aguçado (Ext)** — tubarão atroz: detecção de criaturas a 54 m e de sangue na água a 1,5 km.
- **Ferrugem (Ext)** — monstro da ferrugem: corrosão de metal, teste para itens metálicos encantados e imunidade de materiais não metálicos.
- **Flautas (Sob)** — sátiro: escolha entre três efeitos de ação mental, área, CD e imunidade temporária após sucesso.
- **Garras de Fumaça (Ext)** — belker: engolfamento na forma gasosa, teste para evitar inalação, dano interno e tentativas para expelir.
- **Harmonia Destrutiva (Sob)** — destrachan: modos Carne, Nervos e Material, cada qual com dano ou resistência próprios.
- **Implante (Ext)** — comparação entre devorador de aranhas, slaad vermelho e xill, com incubação e remoção específicas.

### Correção de precisão
- A entrada geral **Faro aprimorado** anteriormente informava 60 m para tubarões. O verbete do **tubarão atroz** no PDF indica 54 m para perceber criaturas e 1,5 km para detectar sangue na água. O texto foi corrigido para 54 m e a capacidade foi separada sob o nome específico **Faro Aguçado (Ext)**.
- Faro Aguçado é um exemplo específico do tubarão atroz, não uma nova regra universal para todas as criaturas com Faro.
- Aura Ígnea e Combustão, assim como Aura Degradante e Aversão à Luz do Dia, permanecem entradas distintas por terem gatilhos e efeitos diferentes.
- Implante não recebeu uma CD ou prazo universal: os três exemplos têm diferenças importantes e permanecem separados no texto.
- Harmonia Destrutiva preserva os três modos e suas resistências; não foi reduzida a um ataque sônico genérico.

### Escopo
- Alteração restrita ao texto informativo do glossário e ao log de auditoria. Nenhuma ficha, estatística, cálculo ou dado persistido foi alterado.

### Arquivos
- `RulesGlossaryDialog.kt`: 17 entradas específicas e correção do alcance de Faro Aguçado.
