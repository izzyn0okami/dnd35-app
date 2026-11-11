# Índice de ilustrações — Livro dos Monstros 3.5

**Estado:** levantamento inicial em andamento  
**Fonte primária:** PDF brasileiro do Livro dos Monstros 3.5 fornecido para o projeto  
**Catálogo associado:** `app/src/main/resources/dnd35/creatures.json` (230 registros; IDs preservados)

## Método e convenções

- A coluna **página impressa** usa o número que aparece no rodapé do livro. **Página PDF** é a posição física, começando em 1.
- A página foi associada à criatura pela ocorrência do nome e pela leitura do bloco de estatísticas; a ocorrência no sumário ou no índice remissivo não conta.
- “Arte confirmada” significa que a ilustração correspondente foi visualmente identificada na página. “Revisar arte” significa apenas que a ficha foi localizada; não se deve assumir que a imagem na página representa aquela criatura.
- O PDF contém imagens segmentadas em vários objetos internos. A extração direta desses objetos não corresponde necessariamente a uma ilustração completa. O método de produção deve renderizar a página e recortar a região da arte, conferindo visualmente o resultado.
- Nenhuma imagem extraída foi adicionada ao repositório nesta etapa. A arte do livro tem direitos autorais; antes de embutir imagens no APK distribuído publicamente, é necessário resolver a autorização de redistribuição. O índice de páginas não redistribui a arte.

## Primeira faixa do catálogo

| ID do catálogo | Nome no catálogo | Página impressa | Página PDF | Situação da arte |
|---|---|---:|---:|---|
| `abocanhador-matraqueante` | Abocanhador Matraqueante | 8 | 12 | **Confirmada** — ilustração na metade inferior |
| `achaierai` | Achaierai | 10 | 14 | **Confirmada** — ilustração na metade inferior esquerda |
| `aguia-gigante` | Águia Gigante | 11 | 15 | **Confirmada** — ilustração na metade inferior esquerda |
| `allip` | Allip | 11 | 15 | Revisar arte — bloco localizado; não foi identificada arte própria nesta página |
| `arminho-atroz` | Arminho Atroz | 14 | 18 | Revisar arte |
| `carcaju-atroz` | Carcajú Atroz | 14 | 18 | Revisar arte |
| `gorila-atroz` | Gorila Atroz | 15 | 19 | Revisar arte |
| `javali-atroz` | Javali Atroz | 15 | 19 | Revisar arte |
| `leao-atroz` | Leão Atroz | 15 | 19 | Revisar arte |
| `lobo-atroz` | Lobo Atroz | 16 | 20 | Revisar arte |
| `morcego-atroz` | Morcego Atroz | 16 | 20 | Revisar arte |
| `rato-atroz` | Rato Atroz | 16 | 20 | Revisar arte |
| `tigre-atroz` | Tigre Atroz | 17 | 21 | Revisar arte |
| `tubarao-atroz` | Tubarão Atroz | 17 | 21 | Revisar arte |
| `urso-atroz` | Urso Atroz | 18 | 22 | Revisar arte |
| `anjo-deva-astral` | Anjo, Deva Astral | 19 | 23 | Revisar arte |
| `anjo-planetario` | Anjo, Planetário | 19 | 23 | Revisar arte |
| `anjo-solar` | Anjo, Solar | 20 | 24 | Revisar arte |
| `aranea` | Aranea | 23 | 27 | **Confirmada** — ilustração na região superior esquerda |
| `aranha-interplanar` | Aranha Interplanar | 23 | 27 | **Confirmada** — ilustração na metade inferior |
| `arbusto-errante` | Arbusto Errante | 24 | 28 | Revisar arte |
| `arconte-luminar` | Arconte Luminar | 25 | 29 | Revisar arte |
| `arconte-mensageiro` | Arconte Mensageiro | 27 | 31 | Revisar arte |
| `azer` | Azer | 28 | 32 | Revisar arte |
| `beholder` | Beholder | 32 | 36 | Revisar arte |
| `belker` | Belker | 34 | 38 | Revisar arte |
| `anis` | Anis | 35 | 39 | Revisar arte |
| `bruxa-verde` | Bruxa Verde | 36 | 40 | Revisar arte |
| `bruxa-do-mar` | Bruxa do Mar | 36 | 40 | Revisar arte |
| `bugbear` | Bugbear | 38 | 42 | Revisar arte |
| `bulette` | Bulette | 39 | 43 | Revisar arte |

## Decisões técnicas registradas

1. O catálogo de criaturas permanece como fonte de dados de jogo. O vínculo visual será feito por `CreatureDefinition.id`, sem alterar os nomes, IDs ou estatísticas.
2. O manifesto de imagens deve ser independente do JSON de estatísticas, para não acoplar regras a caminhos de arquivos.
3. Cada imagem futura precisa registrar: ID da criatura, página impressa, página PDF, região de recorte, arquivo de destino, origem e situação de licença.
4. Não usar uma imagem genérica ou de outra criatura para preencher lacunas. Se não houver arte adequada, a interface poderá usar a silhueta provisória já planejada.
5. A cobertura do catálogo não deve ser anunciada como concluída enquanto os 230 IDs não tiverem sido verificados individualmente.

## Log de progresso — 2026-11-10

- Confirmada a existência de 230 entradas no catálogo atual.
- Conferidas visualmente as páginas PDF 12, 14, 15 e 27; cinco associações de arte foram identificadas: Abocanhador Matraqueante, Achaierai, Águia Gigante, Aranea e Aranha Interplanar.
- Localizadas as fichas da primeira faixa de 30 criaturas, da página impressa 8 à 39; 25 linhas ainda aguardam conferência visual individual da arte.
- Verificado que a contagem bruta de objetos de imagem do PDF não equivale à quantidade de ilustrações: várias imagens estão divididas em blocos. Para os recortes, usar renderização da página, não assumir que cada objeto interno seja uma figura.
- Próxima etapa: continuar a associação sequencial dos IDs restantes, confirmar a presença e a região de cada ilustração e só então preparar o manifesto de recursos locais.
