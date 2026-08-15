# 38 — Backfill histórico do sino (fired sem Room)

## Problema

`wasFired=true` fazia skip total → sino vazio para lembretes antigos.

## Correção

Se `wasFired` e `messageId`/key ausente no Room → **backfill** histórico sem `notify()`.

Logs: `NOTIFICATION_HISTORY key/exists/backfill/inserted`
