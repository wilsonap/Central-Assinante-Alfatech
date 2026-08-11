# 32 — Histórico local de comprovantes

## Auditoria Room
- Versão anterior: 2 (`NotificationEntity`)
- Nova versão: 3 + `MIGRATION_2_3` (CREATE `receipt_history`)
- Sem `fallbackToDestructiveMigration`

## Fluxo
1. Usuário inicia share → cópia byte-a-byte em `filesDir/receipts/receipt_<ts>.<ext>`
2. Room status=`shared` (sem confirmação WhatsApp)
3. Home → **Comprovantes** → lista/detalhe/reenviar/excluir

## Arquivos
Entity/Dao/Migration, `ReceiptHistoryStore`, `ReceiptHistoryScreen`, Home, ViewModel, MainActivity, `file_paths.xml`
