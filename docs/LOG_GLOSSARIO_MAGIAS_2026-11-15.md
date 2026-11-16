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
