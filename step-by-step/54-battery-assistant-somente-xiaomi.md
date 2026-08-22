# 54 — BatteryOptimizationAssistant só Xiaomi/Redmi/POCO

## Mudança

- `shouldShowPrompt` → somente `isXiaomiFamily()` + uma vez
- Dialog: recomendação Autostart / notificações; botões Configurar / Agora não
- Outros OEMs: sem dialog
- Intents focados em MIUI (Autostart → powerkeeper → bateria → notificações → detalhes)

Sem alteração AlarmManager / Manifest / FCM.
