# Relatório — Localização do envio FCM (Etapa 1+2, sem implementação)

**Data:** 08/08/2026  
**Conclusão:** O código responsável pelo **envio** FCM **não está neste repositório**.

## A. Onde está o envio?
Não encontrado. Neste repo só há **recepção** Android + registro de token na Central (`updateFirebaseToken`).

## B. Payload atual (evidência dispositivo)
`payloadKind=notification`, `remoteMessage.data={}` → notification-only.

## C. Payload novo (proposta HTTP v1 data-only)
Ver relatório no chat — sem bloco `notification`, `data` com title/body/type/url/message_id, `android.priority=HIGH`.

## D–G
Arquivos a modificar: **fora deste repo** (backend IXC/Central).  
Android já compatível — não alterar o app para contornar.
