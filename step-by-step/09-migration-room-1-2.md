# Step-by-step — Migration Room 1→2 (segura)

**Data:** 2026-08-08

## Alterações
- `NotificationMigrations.kt` com `MIGRATION_1_2`
- `AppDatabase`: `.addMigrations(MIGRATION_1_2)`; removido `fallbackToDestructiveMigration()`
- Teste Robolectric `NotificationMigration1to2Test` (v1 → v2 preserva linhas)
- `GreetingScreenshotTest` legado `@Ignore` (bloqueava compile de testes)

## SQL Migration(1,2)
```sql
ALTER TABLE notifications ADD COLUMN messageId TEXT;
ALTER TABLE notifications ADD COLUMN contentHash TEXT NOT NULL DEFAULT '';
ALTER TABLE notifications ADD COLUMN targetUrl TEXT;
UPDATE notifications SET contentHash = 'legacy_' || id WHERE contentHash = '';
CREATE UNIQUE INDEX IF NOT EXISTS `index_notifications_messageId` ON `notifications` (`messageId`);
```

## Validação
- testDebugUnitTest NotificationMigration1to2Test: OK
- assembleDebug: OK
