# 53 — Gate API 26+ em NotificationChannels (NewApi)

## Correção

APIs de `NotificationChannel` só em métodos `@RequiresApi(O)` chamados após:

`if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return`

API 24/25: `ensureCreated` / diagnóstico no-op; builders usam NotificationCompat.

## Validação

`:app:lintRelease` → **BUILD SUCCESSFUL** (0 fatal NewApi deste arquivo).

Próximo bloqueio de release: `validateSigningRelease` (keystore).
