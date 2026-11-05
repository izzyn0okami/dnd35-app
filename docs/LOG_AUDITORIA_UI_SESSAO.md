# Log de auditoria — navegação da ficha e sessão multiplayer

## 2026-11-05 — Separação entre ficha e mesa

### Decisões aprovadas
- A antiga aba principal **Combate** passa a se chamar **Personagem**, pois apresenta a ficha individual e seu resumo de combate, não o combate coletivo multiplayer.
- A ficha continua sendo a área central do aplicativo, preservando as cinco seções existentes: Personagem, Perícias, Carga & Itens, Talentos e Grimório.
- A Sessão de Mesa passa a ter navegação contextual própria com três áreas: **Sessão**, **Combate** e **Recursos**.
- O Mestre e os jogadores compartilham a mesma organização de áreas, respeitando suas permissões e ações disponíveis.
- O usuário pode sair temporariamente da tela da sessão e consultar qualquer aba da ficha. Isso apenas oculta a interface da sessão; não encerra hospedagem nem desconecta o jogador. O botão de Sessão de Mesa na barra superior permite retornar.
- A tela da sessão deve permitir rolagem vertical completa, sem esconder participantes, controles ou seções abaixo da área visível.

### Implementação nesta alteração
- Renomeada a primeira aba da navegação principal para **Personagem**.
- Ao selecionar qualquer aba da ficha, a tela da Sessão de Mesa é ocultada sem chamar stopHosting() ou leaveSession().
- Adicionadas as áreas contextuais Sessão / Combate / Recursos para Mestre e Jogador.
- Organizados QR Code, participantes, pedidos, chat e administração de NPCs na área Sessão.
- Organizados iniciativa, rodadas e alterações de PV na área Combate.
- Organizados baú de loot e loja na área Recursos.
- Adicionada rolagem vertical à tela da Sessão de Mesa.
- Aumentada a altura calculada da lista de participantes para acomodar cartões com duas linhas de informação.

### Restrições de escopo
- Não alterar regras de D&D 3.5, cálculos de ficha, regras de combate ou protocolo de rede.
- Não encerrar ou reiniciar uma sessão ao alternar entre a ficha e a mesa.
- Não duplicar dados da ficha: as telas existentes continuam sendo a fonte dos dados individuais.

### Validação pendente
- Executar testDebugUnitTest e assembleDebug na CI.
- Testar manualmente Mestre e pelo menos um jogador conectado.
- Confirmar alternância repetida entre ficha e sessão sem desconexão, perda de estado ou perda de sincronização.
- Verificar rolagem em aparelhos com alturas reduzidas e lista grande de participantes.
