# 45 — Ajustes mínimos de apresentação das notificações

## Escopo

Somente apresentação (lock screen, category, badge, diagnóstico, Xiaomi tip, POST_NOTIFICATIONS negada).
**Não** alterou AlarmManager / Receiver / WorkManager / dedupe / markFired / histórico.

## Alterações

| Arquivo | Função |
|---------|--------|
| `NotificationChannels.kt` | `lockscreenVisibility=PUBLIC` (central + faturas); `setShowBadge(true)` na Central; `logPresentationDiagnostics` |
| `MyFirebaseMessagingService.kt` | `CATEGORY_MESSAGE` + `VISIBILITY_PUBLIC` |
| `InvoiceReminderChecker.kt` | `VISIBILITY_PUBLIC` (mantém `CATEGORY_REMINDER`) |
| `NotificationPermissionAssistant.kt` | request uma vez; orientação se negar; `ACTION_APP_NOTIFICATION_SETTINGS` |
| `BatteryOptimizationAssistant.kt` | `isXiaomiFamily()` |
| `BatteryOptimizationPromptDialog.kt` | tip MIUI + botão "Configurar notificações" |
| `NotificationsDisabledPromptDialog.kt` | orientação discreta POST_NOTIFICATIONS |
| `MainActivity.kt` | diagnóstico + fluxos de UI |
| `NotificationPresentationTest.kt` | canais HIGH/PUBLIC/badge; builders; canal baixado não sobe |

## Logs

`NOTIFICATION_CHANNEL id/importance/vibration/lockscreenVisibility/showBadge`
`headsUpPossible=false reason=importance_lowered|channel_blocked|channel_missing`
