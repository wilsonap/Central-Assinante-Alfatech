# 37 — Vibração canal invoice_reminders

- Canal: `enableVibration(true)` + `vibrationPattern = [0,300,200,300]`
- Builder: `setVibrate` mesmo padrão; PRIORITY_HIGH + CATEGORY_REMINDER
- Logs DEBUG: `channelVibrationEnabled` / `channelImportance`
- Canal já existente no aparelho **não** é recriado; limpar dados ou reinstalar para aplicar
