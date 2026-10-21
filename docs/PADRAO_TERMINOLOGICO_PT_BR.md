# Padrão terminológico — D&D 3.5 em português brasileiro

## Objetivo

Padronizar a interface, os catálogos, as descrições e a ficha PDF do aplicativo **Ficha D&D 3.5** de acordo com a terminologia da edição brasileira de D&D 3.5, evitando misturar traduções de fontes diferentes.

## Hierarquia de fontes

1. **Fonte principal:** *Livro do Jogador* de D&D 3.5, edição brasileira publicada pela Devir, para nomes de regras, classes, raças, perícias, talentos, equipamentos e termos técnicos.
2. **Fonte complementar:** erratas e outros livros brasileiros da mesma edição, quando o termo estiver definido neles.
3. **Fonte auxiliar:** SRD 3.5 em português, somente quando não houver confirmação na fonte principal. O SRD consultado declara expressamente que é uma tradução livre e que pode divergir da Devir; por isso, uma forma encontrada apenas nele deve ser registrada como provisória até a conferência.
4. **Fonte em inglês:** usada para identificar o conceito original quando necessário, nunca como justificativa única para inventar uma tradução portuguesa.

## Regras editoriais

- Não substituir um termo técnico apenas porque uma tradução literal parece mais natural.
- Não misturar termos da 3.5 com os de edições posteriores.
- Preservar a grafia oficial de nomes de classes, raças, perícias, talentos, magias e itens sempre que confirmada.
- Usar português brasileiro natural nos comandos da interface, sem alterar o significado mecânico.
- Expandir siglas na primeira ocorrência relevante quando isso melhorar a compreensão.
- Usar capitalização normal em rótulos e títulos; reservar maiúsculas integrais para cabeçalhos visuais curtos.
- Manter a mesma terminologia na interface, nos dados armazenados, nos testes e no PDF. Mudanças de nomes visíveis devem considerar compatibilidade com fichas já salvas.
- Não renomear identificadores internos, valores persistidos ou chaves de catálogo sem uma migração e testes explícitos.
- Marcar como **PENDENTE DE CONFERÊNCIA** todo termo cuja forma da Devir não tenha sido confirmada diretamente.

## Glossário inicial

| Conceito | Forma adotada | Observações |
|---|---|---|
| Character sheet | Ficha de personagem | Usar em textos da interface e no PDF. |
| Ability score | Habilidade | Termo tradicional da edição 3.5 brasileira; não confundir com perícia. |
| Skill | Perícia | Usar “graduações de perícia” ao se referir a ranks. |
| Rank (skill) | Graduação | Evitar “ponto de habilidade” para esse conceito. |
| Armor Class | Classe de Armadura (CA) | Manter CA depois de apresentar o nome completo. |
| Flat-footed | Surpreso | Forma usada na tradução brasileira da Devir para o Livro do Jogador 3.5. Como o termo também pode ser confundido com a condição de surpresa, o contexto e a regra associada devem deixar claro que se trata da CA sem o bônus de Destreza e da impossibilidade de ataques de oportunidade. |
| Saving throw | Teste de resistência | Tipos: Fortitude, Reflexos e Vontade. |
| Base attack bonus | Bônus Base de Ataque (BBA) | Expandir na primeira ocorrência. |
| Difficulty Class | Classe de Dificuldade (CD) | Expandir na primeira ocorrência. |
| Spell resistance | Resistência à Magia (RM) | Expandir na primeira ocorrência. |
| Spell slot | Espaço de magia | Preferir “espaços de magia por dia” quando o contexto for o limite diário. |
| Spellbook | Grimório | Aplicar conforme a classe e o contexto da ficha. |
| Custom item | Item personalizado | Evitar “custom”. |
| Body slot | Espaço de equipamento | Rótulo de interface; conferir a terminologia específica do Livro do Jogador antes de alterar nomes de regras. |
| Movement in squares | Quadrados | Evitar a abreviação inglesa “sq” na interface e no PDF. |
| Weight | Peso | A unidade deve estar identificada; avaliar exibição complementar em kg sem mudar os cálculos das regras. |

## Termos que exigem conferência direta antes de alterar o catálogo

As formas abaixo não devem ser trocadas automaticamente até serem comparadas com o *Livro do Jogador* brasileiro de D&D 3.5:

- Nomes de perícias, incluindo **Salto**, **Operar Mecanismo**, **Acrobacia** e **Arte da Fuga**.
- Nomes de classes, especialmente **Ranger** e suas possíveis variantes editoriais.
- Nomes de raças e sub-raças, incluindo **Halfling**.
- Nomes e descrições de talentos, magias, armas, armaduras e itens mágicos.
- Traduções de condições de combate e habilidades de classe.

## Alterações iniciais desta etapa

- O rótulo da CA correspondente foi padronizado para “SURPRESO”, seguindo a terminologia identificada na tradução brasileira da Devir.
- O rótulo correspondente no PDF foi padronizado para “Surpreso”, seguindo a terminologia identificada na tradução brasileira da Devir.
- A abreviação inglesa “sq” foi substituída por “quadrados” no PDF.
- “Restaurar Slots” foi substituído por “Recuperar espaços de magia”.
- “Catálogo PHB” foi substituído por “Catálogo do Livro do Jogador”.
- “Item Custom” foi substituído por “Item personalizado”.
- “Body Slot” foi removido do rótulo visível, substituído por “Espaço de equipamento”.

Estas alterações iniciais são uma primeira etapa de padronização, não uma certificação de todo o catálogo. A auditoria dos nomes de regras deve continuar por grupos, com fonte e estado de confirmação documentados.
