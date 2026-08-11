# 26 — Correção Enviar comprovante (WhatsApp + nome completo)

## Causa do “Enviar para…”
`ACTION_SEND` + extra `jid` (não oficial) — WhatsApp ignora e abre seletor genérico.

## Correção
- Opção A: **Enviar arquivo** (`ACTION_SEND` + stream + `EXTRA_TEXT`)
- Opção B: **Abrir conversa da Alfatech** (`wa.me` + número dinâmico)
- Nome: só `clientFullName` de `dados.razao` / `nome_completo` (Meus Dados)
- Rodapé/mensagem/prévia: sem Código/Contrato
- Logs: tag `RECEIPT_SHARE` (número mascarado)
