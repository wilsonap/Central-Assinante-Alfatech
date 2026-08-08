# Step-by-step — Diagnóstico persistência FCM → Avisos

**Data:** 2026-08-08  
**Status:** Diagnóstico only — sem implementação

## Sintoma
- Foreground: Room + Avisos OK
- Background/fechado: bandeja OK, Room/Avisos NÃO

## Causa alinhada ao FCM oficial
Payload com `notification` (ou `notification+data`): em background o sistema exibe a bandeja e **não** chama `onMessageReceived`. O app só salva Room dentro de `onMessageReceived`.

## Lacunas no app
- `MainActivity` só lê `target_url` dos extras — não persiste título/corpo
- Sem dedupe por `messageId`
- `markAsRead` existe no DAO e não é usado na UI
