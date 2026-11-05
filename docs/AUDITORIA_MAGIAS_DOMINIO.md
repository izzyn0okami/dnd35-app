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
- Lote 2 (círculos 1-3, mais Círculo Mágico Contra o Bem/a Ordem e Dissipar o Bem/a Ordem): 22 magias
  novas. Domínio acrescentado sem renomear em Proteção contra a Lei (Caos 1) e Névoa Obscurecente
  (Água 1, Ar 1). Amolecer Terra e Pedra foi conferida no SRD oficial, pois o PDF misturava seus campos.
- Lote 3 (círculos 4-6): 29 magias novas. Cólera da Ordem, Destruição Sagrada, Martelo do Caos e Nuvem
  Profana são exclusivas de domínio no livro (`classes` vazio; o nível é o do domínio). Vários verbetes do
  PDF vinham com campos misturados de outras magias (Adivinhação, Poder Divino, Força dos Justos, Animar
  Objetos, Doença Plena, Imobilizar Monstro, etc.); nesses casos valeram o SRD oficial e o conhecimento
  das regras, e as descrições são resumos próprios.
- `domainLevels` corrigido e completado nas magias que já existiam (PR #305), incluindo o domínio da Força.

## Pendente
Nenhuma magia de domínio do Livro do Jogador fica fora do catálogo. Falta apenas implementar o espaço
de domínio na ficha (dois domínios, uma magia de domínio por círculo).

## Níveis de classe corrigidos fora do escopo dos domínios
Coluna de Chamas (Druida 4), Dissipar Magia (Druida 4), Pele Rochosa (Druida 5), Arma Mágica Maior
(Clérigo 4), Proteção Contra a Morte (+Druida 5) e Cancelar Encantamento (sem Druida; Paladino 4 pelo SRD,
pois o texto do PDF extraído traz 5). Uma auditoria completa de `classLevels` por classe, verbete a verbete,
continua pendente: a extração automática do PDF é ruidosa demais para corrigir tudo em massa.

## Auditoria de classLevels pelas listas de classe
Comparação do catálogo com as listas "Magias de <classe>" do Livro do Jogador (extraídas do PDF). Corrigidos:
Curar Ferimentos Críticos (Druida 5), Localizar Objetos (Clérigo 3), Visão no Escuro (Ranger 3), Falar com Animais
(+Bardo 3), Luz do Dia (+Bardo 3, sem Ranger), Projetar Imagem (+Bardo 6), Viagem Planar (+Clérigo 5),
Movimentação Livre (+Clérigo 4), Curar Ferimentos Moderados em Massa (+Bardo 6); removidos classes que o livro não
lista: Bardo em Suportar Elementos, Salto, Reduzir Pessoa, Remover Paralisia, Zona da Verdade e Discernir Mentiras;
Druida em Esplendor da Águia; Mago/Feiticeiro em Proteger Outro. As seções de Feiticeiro/Mago dos círculos 4/5 e 7/8
vêm trocadas na extração do PDF e foram ignoradas na comparação (Ondas da Exaustão e outras do 8º círculo seguem
como estavam, conferidas na auditoria anterior). Níveis do catálogo (campo `level`) não seguem sempre o menor
`classLevels`; isso não foi alterado.
