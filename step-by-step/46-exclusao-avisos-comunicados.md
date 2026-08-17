# 46 — Exclusão com confirmação em Avisos e Comunicados

## Antes (auditoria)

Exclusão global imediata:

`NotificationsScreen` (lixeira) → `MainViewModel.clearAllNotifications()` → `NotificationDao.clearAll()`  
(`DELETE FROM notifications`)

Não existia delete por id.

## Depois

| Item | Implementação |
|------|----------------|
| Excluir todas | AlertDialog → só então `clearAll()` + Snackbar |
| Excluir uma | lixeira discreta no card → AlertDialog → `deleteById(id)` |
| DAO | `deleteById(id: Int)` — sem mudança de schema |
| Faturas / fired / AlarmManager | intocados (só remove linha do histórico Room) |

## Arquivos

- `NotificationDao.kt`
- `MainViewModel.kt` (`deleteNotification`)
- `NotificationsScreen.kt`
- `MainActivity.kt` (wiring)
