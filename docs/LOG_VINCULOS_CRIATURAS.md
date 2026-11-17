# Log de implementação — Vínculos com criaturas

## 2026-11-16 — Progressão automática de familiares

### Fonte de regras
- Livro do Jogador D&D 3.5, edição brasileira, capítulo 3, seção **Familiares**, páginas impressas 34–35 (páginas 36–37 do arquivo PDF da Biblioteca).
- Referência principal: tabela **Descrição das Habilidades dos Familiares** e texto explicativo imediatamente abaixo dela.
- A tabela usa o nível combinado das classes que possuem a característica Familiar. No aplicativo, Mago e Feiticeiro já são somados por `CreatureBondRules.familiarMasterLevel`; essa regra existente foi reaproveitada.
- A tabela determina, para o familiar, aumento cumulativo de armadura natural e valor de Inteligência:
  - níveis 1–2: +1 e Int 6;
  - 3–4: +2 e Int 7;
  - 5–6: +3 e Int 8;
  - 7–8: +4 e Int 9;
  - 9–10: +5 e Int 10;
  - 11–12: +6 e Int 11;
  - 13–14: +7 e Int 12;
  - 15–16: +8 e Int 13;
  - 17–18: +9 e Int 14;
  - 19–20: +10 e Int 15.
- As habilidades são cumulativas: evasão aprimorada, partilhar magias, vínculo empático e Prontidão desde os níveis 1–2; transmitir magias de toque a partir do 3º; falar com o mestre a partir do 5º; falar com animais da própria espécie a partir do 7º; Resistência à Magia igual ao nível do mestre +5 a partir do 11º; vidência no familiar uma vez por dia a partir do 13º.
- O corvo familiar também pode falar um idioma escolhido pelo mestre.
- O Livro do Jogador esclarece que os benefícios especiais do familiar ao mestre somente se aplicam quando a criatura está a até 1,5 km. Prontidão tem condição mais restrita: o familiar precisa estar ao alcance da mão. A partilha de magias exige proximidade de 1,5 m durante a conjuração e, para efeitos não instantâneos, enquanto permanecer ativo.

### Benefícios da espécie ao mestre
- Víbora pequena: +3 em Blefar.
- Gato: +3 em Furtividade.
- Lagarto: +3 em Escalar.
- Rato: +3 em testes de resistência de Fortitude.
- Texugo: +3 em testes de resistência de Reflexos.
- Coruja: +3 em Observar na penumbra.
- Corvo: +3 em Avaliação.
- Falcão: +3 em Observar em locais iluminados.
- Morcego: +3 em Ouvir.
- Sapo: +3 PV.
- Todos os benefícios de espécie acima dependem da distância máxima de 1,5 km entre mestre e familiar.

### Implementação
- Adicionado modelo calculado `FamiliarProgression`, sem persistir valores derivados nem duplicar a ficha original da criatura.
- A progressão é recalculada em tempo de execução a partir dos níveis atuais de Mago e Feiticeiro e do ID do familiar vinculado.
- A ficha agora exibe o nível combinado, o aumento de armadura natural, a CA total recalculada (CA-base da criatura + aumento natural) e a Inteligência do familiar, além das habilidades acumuladas e dos benefícios do mestre, cada qual com sua condição de alcance.
- A interface explicita o alcance geral de 1,5 km, o alcance de 1,5 m para partilhar magias e o contato exigido durante a conjuração para transmitir magias de toque.
- A Resistência à Magia e a vidência aparecem somente nos níveis exigidos.
- Ao selecionar outro familiar, o vínculo anterior é substituído. O Livro do Jogador permite somente um familiar simultaneamente.
- Os bônus condicionais do mestre são apresentados separadamente e não são incorporados permanentemente aos totais de perícias/resistências/PV: a ficha ainda não registra a distância atual entre mestre e familiar. Aplicá-los sempre causaria bônus incorretos quando o familiar estivesse fora do alcance exigido.

### Testes
- Cobertura das faixas de nível da tabela, incluindo níveis 1 a 20.
- Desbloqueio de habilidades nos níveis 3, 5, 7, 11 e 13.
- Verificação de que a Resistência à Magia pertence ao familiar.
- Benefícios de espécie e condições de alcance; idioma especial do corvo.
- Ausência de progressão para personagens sem Mago/Feiticeiro e para criaturas que não sejam familiares.

### Decisões para evitar retrabalho
- Não recalcular nem alterar a ficha-base do bestiário: a progressão é um modificador derivado e específico do vínculo.
- Não aplicar bônus condicionais do mestre como permanentes nos totais automáticos enquanto não existir um controle explícito e persistente de distância/alcance.
- Não duplicar o cadastro de familiares nem armazenar manualmente os valores da tabela; sempre derivar os valores dos níveis de classe.
