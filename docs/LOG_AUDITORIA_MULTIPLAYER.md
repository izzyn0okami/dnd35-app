# Log de auditoria — comunicação multiplayer

Data: 2026-11-05

Diagnóstico: chamadas de broadcast e sendTo podiam executar escrita TCP na thread chamadora. SessionClient.disconnect também escrevia BYE sincronamente.

Planejamento: usar fila limitada de saída por conexão e um escritor único em Dispatchers.IO; enviar BYE do cliente em coroutine de I/O; preservar o protocolo e a ordem FIFO; validar com StrictMode, testes de integração, CI e aparelhos reais.

Validação pendente: testes unitários, assembleDebug e teste com Mestre e pelo menos dois jogadores.