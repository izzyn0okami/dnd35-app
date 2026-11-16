# Log — expansão do glossário de magias

Data: 2026-11-15

## Escopo e decisões
- O glossário é uma camada de consulta e não altera regras mecânicas nem os registros de magia das fichas.
- A lista de magias permanece sendo a fonte dos dados técnicos usados pelo app.
- As referências detalhadas ficam em arquivo JSON separado, vinculadas pelo nome canônico da magia.
- Magias sem referência detalhada são identificadas como pendentes; o resumo operacional existente não é apresentado como auditoria completa.
- A pesquisa ignora diferenças de acentuação e procura também em escola, descrição, tempo, componentes, alcance, duração, resistência, classes, domínios e texto ampliado.
- Fonte primária: Livro do Jogador brasileiro. SRD é complementar, não substitui a terminologia da edição brasileira.

## Primeira entrada auditada
- **Mísseis Mágicos**, Livro do Jogador, Capítulo 11, p. 220.
- **Escudo Arcano**, Livro do Jogador, Capítulo 11, p. 200; registra bloqueio de Mísseis Mágicos, bônus de escudo +4, aplicação contra ataques de toque incorpóreos de energia e ausência de penalidade de armadura/falha arcana.
- Registra acerto automático, dano de 1d4+1 por projétil, crescimento de quantidade até cinco, distribuição dos alvos e limites de escolha.
- Mantida a informação de Resistência à Magia conforme a linha técnica do verbete.

## Entrada adicional auditada
- **Mãos Flamejantes**, Livro do Jogador, Capítulo 11, p. 217.
- Registrados cone de 4,5 m, dano de fogo de 1d4 por nível (máximo 5d4), Reflexos para metade, ignição imediata de materiais facilmente inflamáveis, ação de rodada completa para apagar chamas em um item e aplicação de Resistência à Magia.
- A entrada foi conferida diretamente no verbete do PDF; não foram alterados os dados mecânicos do catálogo.

## Lote de magias de toque
- **Toque Chocante**, Livro do Jogador, Capítulo 11, p. 248: ataque de toque corpo a corpo, 1d6 de dano elétrico por nível (máximo 5d6), +3 na jogada de ataque contra oponentes com metal, sem teste de resistência e com Resistência à Magia.
- **Toque da Fadiga**, Livro do Jogador, Capítulo 11, p. 249: ataque de toque corpo a corpo, condição fatigado, Fortitude anula, Resistência à Magia, duração de 1 rodada/nível, não afeta criatura já fatigada e termina ao fim da duração; componente material: gota de suor.
- As duas entradas foram vinculadas aos nomes existentes no catálogo e receberam testes. Nenhum dado mecânico foi alterado.
- A conferência também identificou **Toque Macabro** e **Toque Vampírico** no trecho do livro, mas esses nomes não constam no catálogo atual `spells.json`; não foram inseridos no glossário para evitar referências órfãs. A lacuna fica registrada para auditoria separada do catálogo.

## Continuação do lote — magias da página 250
- **Tranca Dimensional**, Livro do Jogador, p. 250: barreira de 6 m de raio, duração de 1 dia/nível, bloqueio de viagens dimensionais para dentro e para fora, sem teste de resistência ou Resistência à Magia; registradas as exceções descritas no livro.
- **Transformação Momentânea**, Livro do Jogador, p. 250: mudança ilusória da aparência e equipamento aparente, limite de 30 cm em altura/largura, +10 em Disfarces e teste de Vontade para reconhecer a ilusão ao interagir; não altera tato, som nem o equipamento real.
- Ambas já existem no catálogo e foram ligadas por nome canônico. Testes verificam página e interações principais; dados mecânicos permanecem inalterados.

## Lote seguinte — magias das páginas 251–253
- **Voo**, Livro do Jogador, p. 253: deslocamento de voo de 18 m (12 m sob armadura média/pesada ou carga média/pesada), mobilidade boa, limites de movimento e descida segura após o fim/dissipação da magia; campo de antimagia é exceção. Entrada vinculada ao nome canônico existente no catálogo (`Voo`, sem acento).
- **Zona da Verdade**, Livro do Jogador, p. 253: emanação de 6 m, duração de 1 minuto/nível, Vontade anula, Resistência à Magia se aplica; criaturas afetadas sabem do efeito e podem evitar responder, e deixam de ser afetadas ao sair da área.
- As duas entradas têm testes automatizados de página e mecânicas centrais. Nenhum dado mecânico do catálogo foi modificado.
- As demais magias presentes nas páginas 251–253 não foram incluídas neste lote sem antes confirmar correspondência exata no catálogo.

## Lote seguinte — magias das páginas 252–253
- **Visão no Escuro**, Livro do Jogador, p. 252: visão em até 18 m, em preto e branco; não funciona através de escuridão mágica; pode ser tornada permanente. Registrados duração, resistência e componente material.
- **Visão da Verdade**, Livro do Jogador, p. 253: alcance de 36 m e capacidades de percepção descritas no verbete, incluindo formas verdadeiras e visão no Plano Etéreo; registradas as limitações contra objetos sólidos, camuflagem, esconderijos mundanos e espaços extradimensionais.
- Ambas já existem no catálogo e receberam testes automatizados. Nenhuma regra mecânica foi alterada.

## Lote seguinte — magias da página 251
- **Ventriloquismo**, Livro do Jogador, p. 251: desloca a origem aparente da voz ou som vocal, alcance curto, duração de 1 minuto/nível, Vontade desacredita em caso de interação e não admite Resistência à Magia; foco de pergaminho em cone.
- **Viagem Planar**, Livro do Jogador, p. 251: transporte instantâneo para outro plano, até oito criaturas voluntárias de mãos dadas, chegada imprecisa entre 7,5 e 750 km do destino pretendido, Vontade anula e Resistência à Magia se aplica; retorno não é automático e exige outro meio.
- Ambas já existem no catálogo, receberam referências e testes automatizados. Nenhum dado mecânico foi alterado.

## Lote adicional — magias da página 252
- **Vidência Maior**: amplia vidência, com duração de 1 hora/nível e possibilidade de conjurar magias específicas através do sensor.
- **Visão Arcana Maior**: revela automaticamente magias e efeitos ativos em criaturas/objetos vistos; não identifica itens mágicos e não pode ser tornada permanente.
- **Visão da Morte**: cone de 9 m, duração de 10 min./nível, identifica estados de vida e anula efeitos que simulam morte; sem teste de resistência ou Resistência à Magia.
- **Vigor do Urso**: +4 de melhoria em Constituição; registrado que PV adicionais por Constituição temporária não são PV temporários e desaparecem quando o atributo retorna ao normal.
- **Virtude**: concede 1 PV temporário por 1 minuto; Fortitude anula (inofensiva), Resistência à Magia se aplica (inofensiva).
- **Visão Falsa**: emanação de 12 m contra adivinhações de vidência, duração de 1 hora/nível, sem teste de resistência ou Resistência à Magia; registrada a alteração da imagem e o componente material de pó de jade (250 PO).
- As seis magias já existem no catálogo e foram vinculadas sem alterar seus dados mecânicos. Testes cobrem seus efeitos centrais.

## Complemento — magias das páginas 250 e 252
- **Velocidade** (entrada do catálogo `Velocidade (Haste)`), Livro do Jogador, p. 250: ataque adicional durante ataque total, +1 nas jogadas de ataque, +1 de esquiva na CA e Reflexos, aumento de 9 m nos deslocamentos até o dobro, limites de acumulação e interação com lentidão; componente material registrado.
- **Visão**, Livro do Jogador, p. 252: visão obtida por pergunta sobre pessoa, lugar ou objeto, com teste de execução e CDs 20/25/30 conforme proximidade e qualidade das informações; custo de 100 XP.
- Ambas já existem no catálogo e receberam testes automatizados. Nenhuma regra mecânica foi alterada.

## Correção de testes locais
- Os testes JVM estavam executando contra o `org.json` de fachada fornecido pelo Android, que lança exceções fora do ambiente Android.
- Adicionada dependência de teste `org.json:json` para fornecer implementação real ao ambiente JVM; a dependência é apenas de teste e não é empacotada no APK.

## Implementação inicial
- Adicionado modelo SpellReference e carregamento de spell_references.json.
- Adicionada pesquisa textual SpellGlossarySearch, sem alterar o catálogo mecânico.
- A tela de detalhes da magia passa a mostrar a seção Glossário, referência e status; itens sem auditoria recebem aviso explícito.
- Testes cobrem busca sem acentos, preservação dos dados mecânicos e vínculo das referências auditadas.

## Próximas etapas
- Expandir sistematicamente as referências para as magias do Livro do Jogador.
- Conferir cada verbete no PDF, registrar a página impressa e anotar exceções/interações.
- Validar divergências entre catálogo e livro sem corrigi-las silenciosamente; alterações mecânicas exigem auditoria separada.

## Correção de validação — Visão no Escuro
- O CI identificou que o teste verificava a limitação contra escuridão mágica no campo `interactions`, mas a referência a registra corretamente em `tacticalNotes`. O teste foi ajustado para conferir o campo correspondente, sem alterar o texto ou a regra da magia.


## Correção de validação — Viagem Planar
- O CI identificou uma asserção que procurava o limite de 750 km no campo `interactions`, embora a referência registre corretamente essa distância em `explanation`.
- O teste foi ajustado para verificar o campo correto. Nenhum texto de referência, regra ou dado mecânico foi alterado.


## Correção de validação — Visão Falsa
- O CI identificou que o teste procurava o alcance de 12 m no campo `tacticalNotes`, embora a referência o registre corretamente em `explanation`.
- A asserção foi ajustada para conferir o campo correspondente. O texto da referência e a regra da magia permanecem inalterados.
- Um teste preexistente de concorrência de mensagens privadas também falhou em uma das duas execuções, mas passou na outra. Como não houve alteração nesse teste nesta PR, será acompanhado em nova execução para distinguir instabilidade intermitente de regressão.

 
## Auditoria do lote de magias de toque — páginas 248–249
- Fonte primária consultada diretamente: Livro do Jogador brasileiro, Capítulo 11, páginas impressas 248–249 (PDF, páginas 252–253).
- **Toque do Carniçal**, p. 248: Fet/Mag 2; toque paralisa um humanoide vivo por 1d6+2 rodadas. A criatura paralisada exala cheiro de carniça em 3 m e causa náusea às demais criaturas na área; Fortitude anula, Resistência à Magia se aplica. Neutralizar venenos remove a náusea e criaturas imunes a veneno não são afetadas.
- **Toque Macabro**, p. 249: Fet/Mag 1; cada toque causa 1d6 de dano por energia negativa e pode causar 1 ponto de dano temporário de Força, anulado por Fortitude. O conjurador pode realizar um toque por nível. Mortos-vivos não sofrem dano nem redução de Força; Vontade anula o efeito especial de fuga em pânico por 1d4+1 rodadas por nível. O campo de resistência foi registrado como **Fortitude parcial ou Vontade anula (veja texto)** para preservar os dois casos do verbete.
- **Toque Vampírico**, p. 249: Fet/Mag 3; ataque de toque causa 1d6 para cada dois níveis de conjurador (máximo 10d6) e concede ao conjurador PV temporários iguais ao dano, limitados ao total de PV atuais do alvo +10; desaparecem após 1 hora. Não permite teste de resistência; Resistência à Magia se aplica.
- O registro anteriormente chamado **Toque Gélido** tinha nível, classe, componentes, alcance e estrutura de resistência correspondentes a Toque Macabro, mas seu nome não aparece no Livro do Jogador brasileiro consultado. Foi renomeado para **Toque Macabro** e sua descrição foi completada conforme o verbete da edição brasileira; não foi criada uma quarta magia duplicada.
- As três entradas foram adicionadas/vinculadas ao catálogo e ao glossário. Testes de catálogo e busca verificam nomes canônicos, níveis, classes e efeitos centrais. Nenhuma regra fora desses três verbetes foi alterada.

## Continuação da auditoria de magias de toque — página 249
- **Toque Enferrujante**, p. 249: Druida 4; corrói e destrói ferro não mágico, com imunidade para itens mágicos de metal. Em combate, pode reduzir o bônus de CA de armadura metálica, destruir arma metálica ou causar 3d6 +1/nível (máximo +15) contra criaturas de ferro. Não permite teste de resistência nem admite Resistência à Magia; foco divino.
- **Toque da Idiotice**, p. 249: Feiticeiro/Mago 2; ataque de toque reduz temporariamente Inteligência, Sabedoria e Carisma em 1d6 cada, sem reduzir qualquer atributo abaixo de 1. Não permite teste de resistência; Resistência à Magia se aplica; duração de 10 minutos/nível.
- Ambas foram incluídas no catálogo e glossário com testes específicos. Este complemento amplia o mesmo lote de auditoria das magias de toque das páginas 248–249 para evitar uma PR fragmentada.


## Correções de CI — PR #391
- A primeira execução dos dois fluxos encontrou quatro falhas de teste, todas na suíte JVM; a compilação Kotlin foi concluída.
- O teste do catálogo comparava o termo "mortos-vivos" em minúsculas, enquanto a descrição canônica inicia o trecho com "Mortos-vivos". Corrigida apenas a comparação de capitalização.
- Um teste preexistente ainda procurava a magia pelo nome antigo "Toque Gélido". Atualizado para o nome brasileiro auditado "Toque Macabro"; níveis, alcance, classes e Resistência à Magia continuam sendo verificados.
- O mapa visual ainda continha a associação órfã "toque gélido". Renomeada para "toque macabro" e acrescentadas associações inequívocas para Toque do Carniçal, Toque Vampírico e Toque da Idiotice.
- Toque Enferrujante não recebeu símbolo específico: permanece com o ícone de sua escola (Transmutação), conforme o fallback visual existente. O teste explicita essa exceção em vez de exigir um símbolo artificial.
- Nenhuma regra mecânica foi alterada por essas correções; os ajustes tratam de testes e consistência do nome/ícones.


## Expansão alfabética — lote A (páginas 168–169)
- Referências detalhadas adicionadas para **Abençoar Água**, **Abençoar Arma**, **Abrir/Fechar**, **Acalmar Animais**, **Acalmar Emoções**, **Adivinhação**, **Agilidade do Gato**, **Ajuda** e **Alarme**.
- **Abençoar Água**, p. 168: transformação de 0,5 litro em água benta; componente material de 2,5 kg de pó de prata (25 PO); Vontade anula (objeto) e Resistência à Magia se aplica a objetos.
- **Abençoar Arma**, p. 168: +1 efetivo para superar Redução de Dano de criaturas malignas e atingir incorpóreos malignos, arma considerada sagrada e confirmação automática de ameaças de crítico contra inimigos malignos, com a exceção de armas com efeitos mágicos ligados a críticos.
- **Abrir/Fechar**, p. 168: limite de 15 kg, falha se houver resistência física e foco de chave de bronze. Corrigida também a progressão de alcance do registro do catálogo, que omitia o aumento de 1,5 m a cada dois níveis.
- **Acalmar Animais**, p. 168: limite de Inteligência 1 ou 2, mesma espécie, distância máxima de 9 m, total de 2d4 + nível em DV e exceções de teste de Vontade para animais atrozes ou treinados; ameaças dissipam a magia.
- **Acalmar Emoções**, p. 168: área de 6 m, impede ações violentas/destrutivas, suprime bônus de moral e efeitos de medo e remove confusão; efeitos suprimidos retornam se ainda durarem quando a magia terminar.
- **Adivinhação**, p. 169: resposta sobre evento/atividade em até uma semana, chance de 70% +1%/nível (máximo 90%) e custo material mínimo de 25 PO.
- **Agilidade do Gato**, p. 169: +4 de aprimoramento na Destreza, com aplicação normal a CA, Reflexos e demais usos do atributo; componente de pelo de gato.
- **Ajuda**, p. 169: +1 de moral em ataques e testes contra medo, além de 1d8 + nível em PV temporários (máximo +10).
- **Alarme**, p. 169: emanação de 6 m, alarme mental ou audível, senha, limitações de alcance do som, exceção para criaturas etéreas/astrais e possibilidade de permanência.
- O SRD foi usado como referência rápida comparativa. A edição brasileira prevaleceu nos valores convertidos e na terminologia: em especial, Abrir/Fechar mantém o limite de **15 kg** do Livro do Jogador, em vez da conversão em libras exibida no SRD.
- Correções verificadas no catálogo: Abençoar Água passa a registrar o descritor [Bem]; Acalmar Animais registra Encantamento (Compulsão) [Ação Mental]; Abrir/Fechar passa a registrar o alcance completo de 7,5 m + 1,5 m/2 níveis. Testes cobrem as nove referências e a progressão de alcance.
- Nenhuma outra mecânica foi alterada.


## Expansão alfabética — lote Am (páginas 170–171)
- Referências detalhadas adicionadas para **Amaldiçoar Água**, **Amolecer Terra e Pedra**, **Ampliar Animais** e **Ampliar Plantas**.
- **Amaldiçoar Água**, p. 170: 500 ml de água profana, energia negativa, dano a seres planares Bons, 2,5 kg de prata em pó (25 PO), Vontade anula (objeto) e RM sim (objeto). O cabeçalho do verbete classifica como **Necromancia [Mal]**, enquanto o corpo diz “esta transmutação”. A classificação do catálogo segue o cabeçalho e a divergência textual é explicitada no glossário, sem conciliação silenciosa.
- **Amolecer Terra e Pedra**, p. 170: quadrado de 3 m/nível, profundidade de 30 a 120 cm; lama pode prender por 1d2 rodadas com Reflexos; terra solta reduz deslocamento; argila facilita escavação. Não afeta pedra trabalhada/mágica ou criaturas de terra/pedra. O cabeçalho informa nenhum teste de resistência, mas o texto descreve Reflexos específico para criaturas na lama. RM não.
- **Ampliar Animais**, p. 170: um animal por dois níveis, até Imenso, tamanho dobrado e peso multiplicado por oito; bônus de tamanho, RD 10/mágica, +4 em testes de resistência, equipamento aumenta junto e efeitos de aumento de tamanho não se acumulam. A magia não comanda animais.
- **Ampliar Plantas**, p. 171: efeitos Crescimento e Enriquecer; alcance longo de 120 m + 12 m/nível para crescimento, áreas opcionais de 30 m de raio/45 m hemisfério/60 m quarto de círculo; Enriquecer afeta plantas em até 750 km segundo o texto impresso e aumenta produtividade em um terço no ano seguinte. Dissipa/contramágica Enfraquecer Plantas e não afeta criaturas do tipo planta.
- Comparação rápida com SRD: confirma os efeitos centrais de Amolecer Terra e Pedra e Ampliar Plantas; valores imperiais do SRD não substituem as medidas métricas da edição brasileira. Referências consultadas: SRD 3.5, Soften Earth and Stone e Plant Growth.
- Correções de qualidade de testes: duas asserções anteriores verificavam o campo errado ou uma expressão literal divergente; corrigidas para conferir o campo e o texto efetivamente registrados. Não houve redução de cobertura.
- Correção mecânica no catálogo: Amaldiçoar Água passa de Transmutação para Necromancia [Mal], conforme o cabeçalho do Livro do Jogador; a divergência com a expressão “esta transmutação” fica documentada.


## Expansão alfabética — lote An (páginas 172–173)
- Referências detalhadas adicionadas para **Andar no Ar**, **Animar Corda**, **Animar Objetos** e **Animar Plantas**.
- **Andar no Ar**, p. 172: criatura tocada até Imensa, ângulo de 45° e metade do deslocamento; ventos de 30 km/h ou mais podem empurrar; ao terminar no ar, descida gradual de 18 m/rodada por 1d6 rodadas, com queda se não alcançar o solo. RM sim (inofensiva).
- **Animar Corda**, p. 172: nome corrigido de “Animar Cordas” para o singular usado pelo Livro do Jogador brasileiro. Comprimento 15 m + 1,5 m/nível para corda de 2,5 cm, comandos por ação de movimento, enredamento apenas a 30 cm, ataque de toque à distância, incremento 3 m, 2 PV, CA 10, Força CD 23, Arte da Fuga CD 20 e bônus +2 em Usar Cordas. RM não.
- **Animar Objetos**, p. 172: anima objetos mundanos, um Pequeno por nível ou equivalentes maiores; não anima objetos vestidos/carregados; pode ser permanente por Permanência.
- **Animar Plantas**, pp. 172–173: uma planta Grande por três níveis ou equivalentes maiores; alcance curto de 7,5 m + 1,5 m/2 níveis; duração 1 rodada/nível ou 1 hora/nível. Alternativa entrelaça plantas e duplica Constrição. Não afeta criaturas do tipo planta nem material vegetal morto. Corrigidos no catálogo os componentes (V), alcance e duração, antes incompletos/divergentes.
- Comparação rápida com SRD 3.5: confirmou a estrutura dos efeitos de Air Walk, Animate Rope, Animate Objects e Animate Plants. Valores e termos métricos/portugueses seguem o Livro do Jogador brasileiro.
- Corrigido teste anterior do catálogo de Toque Vampírico, que esperava a expressão “máximo de 10d6”, embora o texto correto registre “máximo 10d6”. A asserção agora acompanha o dado efetivamente cadastrado, sem alterar a regra.


## Expansão alfabética — lote Ap (página 173)
- Referências detalhadas adicionadas para **Antipatia**, **Apagar**, **Aprisionamento** e **Aprisionar a Alma**.
- **Antipatia**: repulsão de tipo específico de criatura ou tendência; sucesso em Vontade permite permanecer, mas reduz Destreza em 4; serve de contramágica e dissipa Simpatia.
- **Apagar**: remove escrita mundana ou mágica de pergaminho ou até duas páginas; inclui regras para runas explosivas, símbolo de proteção, selo da serpente sépia, marca arcana, teste de conjurador CD 15 e ativação acidental em falha.
- **Aprisionamento**: enterra o alvo em animação suspensa; Libertação no local desfaz o efeito; desejo/milagre e adivinhações têm limitações específicas; penalidade de –4 no teste de resistência quando o conjurador conhece nome e detalhes da vítima.
- **Aprisionar a Alma**: documentadas as duas ativações, magia completa e Objeto Armadilha; distinções de teste de resistência/RM, gema de pelo menos 1.000 PO por DV e foco especial.
- Os textos foram conferidos no Livro do Jogador brasileiro, p. 173. Nenhum parâmetro mecânico do catálogo foi alterado neste lote.


## Divergência de fonte — Ampliar Plantas (página 171)
- Conferência visual da página impressa 171 confirmou que o Livro do Jogador brasileiro realmente imprime **750 km** para o alcance do efeito Enriquecer.
- O SRD 3.5 registra **half-mile** (meia milha, aproximadamente 800 m) para o mesmo efeito.
- A diferença é muito grande e provavelmente representa erro editorial ou tipográfico na edição brasileira. Como a edição brasileira é a fonte primária definida para o projeto, o glossário mantém fielmente os **750 km impressos**, mas explicita a divergência com o SRD. Não corrigir para 750 m/800 m sem decisão editorial explícita.


## Correção de CI — importação dos testes do glossário (16/11/2026)
- As execuções dos fluxos Android CI e Android build para o commit anterior falharam durante a compilação dos testes JVM, não durante a compilação do aplicativo.
- A causa foi o uso de `assertEquals` nos testes dos lotes Am, An e Ap sem a importação de `org.junit.Assert.assertEquals` em `SpellGlossarySearchTest.kt`.
- Incluída a importação explícita. Nenhuma asserção foi removida ou enfraquecida e nenhum dado de magia foi alterado.
- Aguardar e conferir novamente os dois fluxos para o novo commit antes do merge.

 
## Expansão alfabética — lote Ar (páginas 174–175)
- Referências detalhadas adicionadas para **Arma Espiritual**, **Arma Mágica**, **Arma Mágica Maior**, **Armadilha** e **Armadura Arcana**. Todas já existiam no catálogo; o lote apenas complementa o glossário, sem alterar níveis, classes ou parâmetros mecânicos do catálogo.
- **Arma Espiritual**, p. 174: arma de energia que causa 1d8 +1/3 níveis (máximo +5), usa bônus base de ataque e modificador de Sabedoria, pode realizar ataques múltiplos conforme o BBA, muda de alvo com ação padrão, atinge incorpóreos e interage com RM no primeiro golpe contra cada criatura.
- **Arma Mágica**, p. 174: +1 de melhoria em ataque e dano por 1 minuto/nível; não se acumula com bônus de obra-prima no ataque; não afeta armas naturais, com a exceção expressa do ataque desarmado de monge.
- **Arma Mágica Maior**, pp. 174–175: +1 por quatro níveis (máximo +5), uma arma ou até cinquenta projéteis do mesmo tipo reunidos; projéteis perdem a transmutação quando disparados, enquanto armas de arremesso não; shuriken são tratados como projéteis.
- **Armadilha**, p. 175: laço mágico de 60 cm +60 cm/nível, pode ser ligado a árvore flexível para causar 1d6 e erguer a vítima; localização por Procurar CD 23 para quem possui Encontrar Armadilhas; fuga por Arte da Fuga CD 23 ou Força CD 23; CA 7 e 5 PV.
- **Armadura Arcana**, p. 175: campo de força concede +4 de bônus de armadura na CA, sem penalidade de armadura, chance de falha arcana ou redução de deslocamento; criaturas incorpóreas não atravessam a proteção; foco de couro endurecido.
- Conferência rápida com o SRD 3.5: Arma Mágica e Arma Mágica Maior mantêm os mesmos bônus, restrições e regra de projéteis; Armadura Arcana mantém +4, duração de 1 hora/nível e ausência de RM. O glossário segue a terminologia e medidas do Livro do Jogador brasileiro. Arma Espiritual foi comparada com a descrição SRD de arma de energia, ataques, redirecionamento e interação com RM.
- Testes adicionados para as cinco referências, páginas e mecânicas centrais. Nenhum dado mecânico do catálogo foi alterado neste lote.

 
## Expansão alfabética — complemento do lote A (página 176)
- Referências detalhadas adicionadas para **Aumentar Pessoa**, **Ataque Certeiro** e **Aura Mágica de Nystul**, já presentes no catálogo.
- **Aumentar Pessoa**: dobra altura, multiplica peso por oito e aumenta uma categoria de tamanho; +2 Força, –2 Destreza (mínimo 1), –1 ataque e CA; equipamento aumenta junto, mas itens que deixam a posse do alvo retornam ao tamanho normal; efeitos de aumento de tamanho não se acumulam; contramágica/dissipa Reduzir Pessoa.
- **Ataque Certeiro**: +20 de intuição na próxima jogada de ataque antes do final da rodada seguinte e ignora chance de falha contra alvos camuflados; foco de pequena réplica de alvo para arqueiros.
- **Aura Mágica de Nystul**: altera a aura percebida de um objeto; Identificação ou exame semelhante pode permitir Vontade para perceber a falsidade; a magia não funciona em auras excepcionalmente poderosas; armas, escudos e armaduras precisam ser obras-primas para parecerem plausivelmente mágicos.
- Conferência rápida com SRD 3.5: Aumentar Pessoa preserva tamanho, bônus/penalidades, equipamento, contramágica e não acumulação; Aura Mágica preserva a alteração da aura e a possibilidade de identificar a falsidade; Ataque Certeiro preserva o bônus de +20 na próxima jogada. A edição brasileira continua como fonte primária.
- Testes adicionados para as três referências, página e efeitos centrais. Nenhum dado mecânico do catálogo foi alterado.

 
## Expansão alfabética — complemento A (páginas 173 e 177)
- Referências detalhadas adicionadas para **Área Escorregadia**, **Auxílio Divino**, **Aura Profana** e **Aura Sagrada**.
- **Área Escorregadia**, p. 173: cobre superfície ou objeto com substância escorregadia; Reflexos para evitar cair e repetição por rodada; deslocamento reduzido e Equilíbrio CD 10 para atravessar; testes adicionais para objetos em uso; roupa/armadura escorregadia concede +10 em Arte da Fuga e Agarrar em situações descritas no verbete. Não admite RM.
- **Auxílio Divino**, p. 177: bônus de sorte de +1 em ataque e dano a cada três níveis de conjurador (mínimo +1, máximo +6), por 1 minuto; não aumenta dano de magias.
- **Aura Profana**, p. 177: +4 de deflexão na CA e +4 de resistência nos testes de resistência; RM 25 contra magias [Bem] ou conjuradas por criaturas Boas; bloqueia possessão e influências mentais; criatura Boa que acerte o protegido em corpo a corpo sofre 1d6 de dano temporário de Força, Fortitude anula.
- **Aura Sagrada**, p. 177: mesma estrutura de bônus defensivos; RM 25 contra magias [Mal] ou conjuradas por criaturas Más; bloqueia possessão e influências mentais; criatura Má que acerte o protegido em corpo a corpo fica cega, Fortitude anula.
- A fonte primária foi consultada diretamente nas páginas impressas indicadas. As quatro magias já constavam do catálogo; nenhum parâmetro mecânico foi alterado. Testes adicionados para cada verbete.
- O SRD 3.5 foi usado apenas como apoio de comparação quando havia equivalência direta; em caso de terminologia ou valores específicos, prevalece o Livro do Jogador brasileiro.
