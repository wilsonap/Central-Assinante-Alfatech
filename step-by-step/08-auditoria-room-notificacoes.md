# Step-by-step — Auditoria Room (somente leitura)

**Data:** 2026-08-08  
**Escopo:** diagnóstico do armazenamento local de Avisos — sem alterações de código.

## Resumo executivo
- Room/SQLite `alfatech_database` v2, tabela `notifications`
- Inserts só via `PushNotificationRepository` (Service + MainActivity)
- Sem Migration 1→2; `fallbackToDestructiveMigration` pode apagar tudo em upgrade de schema
- Sem limite/limpeza automática; lixeira = DELETE ALL
- `allowBackup=true` + rules vazias → DB elegível a backup padrão
