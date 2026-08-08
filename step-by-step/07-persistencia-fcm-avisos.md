# Step-by-step — Persistência FCM → Avisos

**Data:** 2026-08-08

## Alterações
- `PushNotificationRepository.persistFromPush(...)` — helper único
- Room v2: `messageId` (unique), `contentHash`, `targetUrl`
- Service e MainActivity usam o helper
- PendingIntent foreground leva title/body/type/messageId/target_url
- Abrir Avisos → `markAllAsRead()`

## Dedupe
1. `messageId` único quando presente
2. Senão: hash SHA-256(title|body|type) + janela 2 min

## Limitação servidor
Se background for **notification-only** sem `data`, o tap não traz title/body — ajustar servidor para enviar data com title, body, type, url, message_id.
