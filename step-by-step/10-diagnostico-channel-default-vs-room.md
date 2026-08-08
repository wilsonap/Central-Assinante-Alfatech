# Step-by-step — Diagnóstico Logcat channel `default` vs Room

**Data:** 2026-08-08  
**Status:** Somente leitura — sem implementação

## Logcat
- `Notification Channel requested (default) has not been created`
- App fechado: bandeja OK, sem `onMessageReceived`, sem Room

## Conclusão
1. Confirma payload com bloco `notification` (sistema exibe; Service não roda).
2. Backend/payload pede channel_id literal `default` (não `central_alfatech_channel`).
3. Canal app: criado em `MainActivity.onCreate` e no Service só se `onMessageReceived` rodar.
4. Criar canal `default` → corrige warning; **não** resolve persistência Room.
5. Persistência com app fechado → envio **data-only** (title, body, type, url, message_id) + prioridade alta Android.
