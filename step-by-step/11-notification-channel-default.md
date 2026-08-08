# Step-by-step — Canal `default` + `central_alfatech_channel`

**Data:** 2026-08-08

## Alterações
- `NotificationChannels.ensureCreated()` — cria os dois canais (HIGH)
- `AlfatechApp` — cria canais no `Application.onCreate`
- Manifest: `android:name=".AlfatechApp"`
- MainActivity / Service reutilizam o helper

## Escopo
Resolve warning de channel `default`. **Não** resolve persistência Room (próximo passo: data-only no backend).
