# Log de auditoria — Classes básicas: Bárbaro, Bardo, Druida, Guerreiro, Ladino, Monge, Paladino e Ranger

Data: 15/11/2026

## Referência primária
- Livro do Jogador — edição brasileira, Capítulo 3: Bárbaro (pp. 21–22), Bardo (pp. 23–25) e Druida (pp. 29–31), PDF disponível na biblioteca do projeto.

## Correções e esclarecimentos
- Bárbaro: Movimento Rápido foi corrigido no cálculo. O bônus de +3 m é aplicado antes da redução por armadura ou carga e continua válido com armadura média e carga média. Armadura pesada e carga pesada/sobrecarga impedem o benefício. O teste que documentava a regra incorreta foi corrigido e um teste para carga média foi adicionado.
- Bárbaro: descrição da Fúria ampliada; Analfabetismo esclarecido; nome de Vontade Inabalável alinhado à edição brasileira.
- Bardo: textos das habilidades musicais ampliados com requisitos e efeitos que estavam ausentes ou resumidos em excesso. Em particular, foram registrados alcance e requisitos de Atuação, alvos, duração, bônus e limitações importantes.
- Druida: descrições ampliadas para Companheiro Animal, Empatia Selvagem, Caminho da Floresta, Rastro Invisível e as formas animal, vegetal e elemental; incluídos limites de familiaridade, tamanho e Dados de Vida.

## Limites
- As descrições continuam sendo resumos de consulta, não substituem o texto integral das regras nem as decisões do Mestre.
- A auditoria não altera tabelas de progressão de magias ou demais dados de classe.
- A correção de Movimento Rápido é mecânica e deve ser validada pela CI e pelo teste automatizado; os demais ajustes são textuais.

## Referências complementares
- d20 SRD — Barbarian: https://www.d20srd.org/srd/classes/barbarian.htm
- d20 SRD — Bard: https://www.d20srd.org/srd/classes/bard.htm
- d20 SRD — Druid: https://www.d20srd.org/srd/classes/druid.htm


## Ajuste terminológico da Fúria
- A expressão da edição brasileira para a condição ao término da Fúria é **exausto**; a descrição e a habilidade Fúria Incansável foram alinhadas a essa terminologia.


## Complemento de auditoria — Ladino e Monge (15/11/2026)

- **Ladino:** Ataque Furtivo detalhado com alcance máximo de 9 m para ataques à distância, condições de vulnerabilidade, imunidades e regra de não multiplicar o dano em acerto decisivo. Encontrar Armadilhas e Evasão receberam limitações e CDs relevantes.
- **Monge:** Bônus de CA sem Armadura esclarecido; Rajada de Golpes agora registra ação de ataque total, restrição de armadura/armas, penalidades por faixa de nível e segundo ataque no 11º nível. Mão Vibrante recebeu frequência, alvos imunes e CD; Corpo Atemporal esclarece que penalidades de idade já adquiridas permanecem e que o monge ainda morre de velhice.
- Testes automatizados adicionados para conferir a presença dessas limitações nas descrições.

## Complemento — Ladino e Monge
- Ladino: Ataque Furtivo agora explicita alcance de 9 m para ataques à distância, condições de vulnerabilidade, imunidades e ausência de multiplicação do dano em acerto decisivo; Encontrar Armadilhas e Evasão incluem limitações importantes.
- Monge: Bônus de CA sem Armadura, Rajada de Golpes, Mão Vibrante e Corpo Atemporal receberam descrições mais completas, com requisitos, penalidades, armas permitidas, CDs e exceções do Livro do Jogador.
- Teste automatizado adicional cobre as principais limitações textuais de Ladino e Monge.


## Complemento — Paladino e Ranger
- Paladino: Remover Doença corrigida para a progressão oficial de 6º, 9º, 12º, 15º e 18º níveis (1 a 5 usos semanais); removida a entrada incorreta de 6 usos. Imposição de Mãos e Aura de Coragem esclarecidas.
- Ranger: corrigidos os marcos de Camuflagem (13º) e Mimetismo (17º), removendo a habilidade incorreta Esconder-se à Plena Vista no 18º. Rastreador Eficaz agora explicita a penalidade -10 ao rastrear com deslocamento dobrado.
- Testes automatizados cobrem as progressões corrigidas.

## Complemento — Paladino e Ranger
- Paladino: corrigida a progressão de Remover Doença para 6º/9º/12º/15º/18º níveis, com 1 a 5 usos semanais. Imposição de Mãos e Aura de Coragem receberam requisitos e alcance.
- Ranger: Camuflagem corrigida para o 13º nível e Mimetismo para o 17º; removida a entrada indevida Esconder-se à Plena Vista no 18º. Rastreador Eficaz agora registra a penalidade de -10 ao rastrear com o dobro do deslocamento.
- Os testes automatizados cobrem esses marcos de classe.

## Terminologia oficial
- Padronizados os nomes de habilidade **Empatia com a Natureza** (Druida e Ranger) e **Cura pelas Mãos** (Paladino), conforme a edição brasileira.

## Conferência do Guerreiro
- A progressão de talentos adicionais do Guerreiro já correspondia à tabela oficial (1º, 2º e níveis pares de 4º a 20º). As descrições foram uniformizadas para deixar explícita a exigência de pré-requisitos em todos os marcos; nenhuma mecânica ou nível foi alterado.

## Complemento — Guerreiro
- A progressão de talentos adicionais (1º, 2º e níveis pares de 4º a 20º) já estava correta. As descrições foram padronizadas para explicitar a exigência de pré-requisitos em todos os níveis.
- Teste automatizado verifica os onze marcos e a menção aos pré-requisitos.


## Revisão dos testes automatizados — 15/11/2026
- A CI revelou expectativas antigas que contradiziam as progressões já corrigidas: Remover Doença do Paladino começava indevidamente no 3º nível nos testes; o Ranger ainda era testado com Camuflagem no 17º e Esconder-se à Plena Vista no 18º; o teste de carga média do Bárbaro esperava incorretamente a perda do Movimento Rápido.
- Testes atualizados para os marcos corretos: Paladino 6º/9º/12º/15º/18º (1 a 5 usos semanais), Ranger Camuflagem 13º e Mimetismo 17º, e Movimento Rápido do Bárbaro aplicado antes da redução de deslocamento por carga média.
- Inspirar Heroísmo explicita o alvo adicional no 18º nível. As descrições dos talentos bônus do Guerreiro foram uniformizadas para registrar os pré-requisitos.
- Nenhuma estrutura de ficha ou dado persistido foi alterado nesta rodada.
