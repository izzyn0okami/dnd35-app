# Auditoria das magias de domínio (Livro do Jogador)

Fonte: PDF brasileiro do *Livro do Jogador 3.5*, cap. 11, "Domínios de Clérigo" (listas de
1º ao 9º círculo) e os verbetes de magias. Esta auditoria completa o catálogo `spells.json` com as
magias que o espaço de domínio do Clérigo precisa oferecer.

## Regras adotadas ao adicionar magias
- `name`: título do verbete. Onde a lista de domínios usa outra grafia, vale o título do verbete
  (ex.: lista "Escudo do Fogo" e "Semente de Fogo" -> verbetes "Escudo de Fogo" e "Sementes de Fogo").
  Magias que já existem no catálogo **não são renomeadas**, para não orfanar fichas salvas.
- `level`: o menor nível entre as classes (convenção já usada na maior parte do catálogo).
- `classLevels` e `domainLevels`: da linha `Nível:` do verbete, conferida com a lista do domínio.
- `spellResistance`: `true` somente quando o verbete começa com "Sim"; "Não ou Sim (inofensiva)" vira `false`.
- `description`: resumo próprio do efeito, sem transcrever o texto do livro. Os campos de regra
  (escola, componentes, alcance, duração, teste de resistência) seguem o verbete.
- O verbete de **Escudo de Fogo** não lista teste de resistência nem RM (magia pessoal); foram
  registrados "Nenhum" e `false`.

## Concluído
- Lote 1 (11 magias que aparecem em mais de um domínio): Ajuda, Barreira de Lâminas, Criar
  Mortos-Vivos, Despedaçar, Despistar, Encontrar o Caminho, Imunidade à Magia, Resistência à
  Magia, Roupa Encantada, Escudo de Fogo, Sementes de Fogo.
- `domainLevels` corrigido e completado nas magias que já existiam (PR #305), incluindo o domínio da Força.

## Pendente: 53 magias de domínio ainda fora do catálogo
Observação: o PDF tem erros de OCR e grafias inconsistentes ("Animal", "Viajem"); cada verbete deve
ser lido antes de entrar. "Névoa Obscurescente" (Água 1, Ar 1) provavelmente já existe como
"Névoa Obscurecente"; confirmar e, se for a mesma magia, apenas acrescentar o domínio.

| Magia | Domínio e círculo | Situação |
| --- | --- | --- |
| Névoa Obscurescente | Água 1, Ar 1 | verbete localizado |
| Proteção Contra a Ordem | Caos 1 | conferir nome do verbete |
| Acalmar Emoções | Ordem 2 | verbete localizado |
| Amolecer Terra e Pedra | Terra 2 | verbete localizado |
| Arma Espiritual | Guerra 2 | verbete localizado |
| Criar Chamas | Fogo 2 | verbete localizado |
| Detectar Pensamentos | Conhecimento 2 | verbete localizado |
| Drenar Força Vital | Morte 2 | verbete localizado |
| Esquentar Metal | Sol 2 | verbete localizado |
| Localizar Objetos | Viagem 2 | verbete localizado |
| Névoa | Água 2 | verbete localizado |
| Profanar | Mal 2 | verbete localizado |
| Circulo Mágico Contra a Ordem | Caos 3 | conferir nome do verbete |
| Circulo Mágico Contra o Bem | Mal 3 | conferir nome do verbete |
| Clarividência/Clariaudiência | Conhecimento 3 | conferir nome do verbete |
| Criar Mortos-Vivos Menor | Morte 3 | verbete localizado |
| Dominar Animal | Animais 3 | conferir nome do verbete |
| Forma Gasosa | Ar 3 | verbete localizado |
| Luz Cegante | Sol 3 | verbete localizado |
| Moldar Rochas | Terra 3 | verbete localizado |
| Praga | Destruição 3 | verbete localizado |
| Respirar na Água | Água 3 | verbete localizado |
| Adivinhação | Conhecimento 4 | verbete localizado |
| Andar no Ar | Ar 4 | verbete localizado |
| Confusão | Enganação 4 | verbete localizado |
| Controlar Água | Água 4 | conferir nome do verbete |
| Cólera da Ordem | Ordem 4 | verbete localizado |
| Destruição Sagrada | Bem 4 | verbete localizado |
| Infligir Ferimentos Críticos | Destruição 4 | conferir nome do verbete |
| Martelo do Caos | Caos 4 | verbete localizado |
| Muralha de Fogo | Fogo 4 | verbete localizado |
| Nuvem Profana | Mal 4 | verbete localizado |
| Pedras Afiadas | Terra 4 | verbete localizado |
| Poder Divino | Guerra 4 | verbete localizado |
| Transferência de Poder Divino | Magia 4 | verbete localizado |
| Controlar os Ventos | Ar 5 | verbete localizado |
| Curar Ferimentos Leves em Massa | Cura 5 | conferir nome do verbete |
| Dissipar a Ordem | Caos 5 | conferir nome do verbete |
| Dissipar o Bem | Mal 5 | conferir nome do verbete |
| Força dos Justos | Força 5 | verbete localizado |
| Infligir Ferimentos Leves em Massa | Destruição 5 | conferir nome do verbete |
| Matar | Morte 5 | verbete localizado |
| Muralha de Espinhos | Planta 5 | verbete localizado |
| Muralha de Pedra | Terra 5 | verbete localizado |
| Tempestade Glacial | Água 5 | verbete localizado |
| Visão Falsa | Enganação 5 | verbete localizado |
| Animar Objetos | Caos 6 | verbete localizado |
| Cone Glacial | Água 6 | verbete localizado |
| Corrente de Relâmpagos | Ar 6 | verbete localizado |
| Cúpula de Proteção Contra a Vida | Animais 6 | conferir nome do verbete |
| Doença Plena | Destruição 6 | verbete localizado |
| Imobilizar Monstro | Ordem 6 | verbete localizado |
| Repelir Madeira | Planta 6 | verbete localizado |

## Fora do escopo deste documento
Divergências de `classLevels` em magias que já existiam (ex.: Coluna de Chamas, Dissipar Magia,
Pele Rochosa, Cancelar Encantamento parecem diferir do livro) precisam de auditoria própria,
verbete a verbete; a extração automática do PDF é ruidosa demais para corrigi-las em massa.
