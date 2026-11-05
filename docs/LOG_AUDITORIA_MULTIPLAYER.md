# Log de auditoria — comunicação multiplayer

Data: 2026-11-05.

- Servidor: broadcasts, mensagens individuais e estado inicial passam por uma fila limitada por conexão. Um escritor por conexão executa as transmissões em I/O e mantém a ordem FIFO.
- Cliente: o BYE é enviado em coroutine de I/O; o socket é fechado depois da tentativa de envio.
- Hospedagem: SessionViewModel.hostSession inicia o ServerSocket e consulta o IP local em Dispatchers.IO, evitando I/O de rede na thread principal.
- Protocolo JSON, framing TCP, tokens de reconexão e sincronização de frames foram preservados.
- Teste de regressão com StrictMode cobre o broadcast iniciado pela thread chamadora; testes existentes continuam cobrindo conexão, reconexão, combate, pedidos, loot, loja e mensagens privadas.
- Pendente: CI completa e teste manual em aparelhos reais com Mestre e pelo menos dois jogadores. Não fazer merge antes da CI verde e revisão final.
