# 44 — Auditoria completa de notificações (somente leitura)

## Escopo

Auditoria sem alteração de código. Fontes: `NotificationChannels.kt`, `MyFirebaseMessagingService.kt`, `InvoiceReminderChecker.kt`, `MainActivity.kt`, `AndroidManifest.xml`, `BatteryOptimizationAssistant.kt`.

## Canais (criação em código)

| channelId | importance | visibility (canal) | enableVibration | enableLights | sound | showBadge |
|-----------|------------|--------------------|-----------------|--------------|-------|-----------|
| `central_alfatech_channel` | `IMPORTANCE_HIGH` | **não definido** → default Android `VISIBILITY_PUBLIC` | `true` | `true` | não setado → som padrão do sistema | não setado → default `true` |
| `default` | `IMPORTANCE_HIGH` | idem (default PUBLIC) | `true` | `true` | som padrão | default `true` |
| `invoice_reminders_channel` | `IMPORTANCE_HIGH` | idem (default PUBLIC) | `true` + pattern `[0,300,200,300]` | `true` | som padrão | **`true` explícito** |
| `app_updates_channel` | `IMPORTANCE_HIGH` | idem (default PUBLIC) | `true` | **não** | som padrão | default `true` |

**Nota:** `createNotificationChannel` não altera importância/vibração de canal já existente no aparelho.

## Builders

| Origem | Canal | priority | category | setVisibility | setDefaults / vibrate |
|--------|-------|----------|----------|---------------|------------------------|
| FCM Central (`MyFirebaseMessagingService`) | `central_alfatech_channel` | `PRIORITY_HIGH` | **não** | **não** (default PRIVATE no Builder) | `DEFAULT_ALL` |
| Faturas (`InvoiceReminderChecker`) | `invoice_reminders_channel` | `PRIORITY_HIGH` | `CATEGORY_REMINDER` | **não** | `setVibrate(pattern)`; sem `setDefaults` |
| App updates | — | — | — | — | canal criado; **nenhum `notify` usa o canal ainda** |

Ausentes no código: `IMPORTANCE_MAX`, `PRIORITY_MAX`, `CATEGORY_MESSAGE`, qualquer `VISIBILITY_*` explícito.

## POST_NOTIFICATIONS

- Manifest: declarado
- `MainActivity`: request runtime Android 13+ via `RequestPermission`
- Faturas: gate em `validateNotificationGate` bloqueia se negada

## Xiaomi / MIUI

`BatteryOptimizationAssistant` abre Autostart + HiddenAppsConfig (bateria/background). **Não** há orientação para “mostrar na tela de bloqueio”, “pop-up” ou “notificações flutuantes” do MIUI.
