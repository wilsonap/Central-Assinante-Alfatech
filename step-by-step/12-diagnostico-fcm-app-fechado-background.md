# Diagnóstico — FCM com aplicativo fechado/background

**Data:** 08/08/2026  
**Projeto:** Central Assinante Alfatech  
**Status:** Diagnóstico confirmado em dispositivo real  
**Implementação do backend:** NÃO realizada

---

## 1. Objetivo

Investigar por que as notificações push recebidas pelo aplicativo são:

- exibidas normalmente na bandeja do Android;
- salvas no Room quando o aplicativo está aberto;
- mas NÃO são salvas em "Avisos e Comunicados" quando o aplicativo está fechado/background.

Este documento registra o estado atual antes de qualquer alteração no sistema de envio das notificações.

---

## 2. Estado atual do FCM

O Firebase Cloud Messaging está funcionando.

Já foi confirmado que:

- o aplicativo gera token FCM;
- o token é registrado na Central;
- a Central consegue enviar notificações;
- o Android recebe as notificações;
- o aplicativo recebe notificações em foreground;
- o Room funciona;
- o badge de avisos funciona;
- os Notification Channels estão funcionando.

Portanto, o problema atual NÃO é registro do token nem entrega básica do FCM.

---

## 3. Teste com aplicativo aberto

Foi enviada uma mensagem com o aplicativo aberto.

Logcat:

```text
AlfatechFCM:
FCM MESSAGE RECEIVED
payloadKind=notification

notification.title: teste
notification.body: gfgfdgfd

remoteMessage.data: {}
```

### Interpretação

| Campo | Valor observado |
|-------|-----------------|
| `payloadKind` | `notification` |
| `notification.title` / `body` | Preenchidos |
| `remoteMessage.data` | **vazio** `{}` |

Com o app em **foreground**:

1. FCM chama `onMessageReceived()`
2. O app lê `notification.title` / `notification.body`
3. `PushNotificationRepository.persistFromPush(...)` grava no Room
4. `showNotification()` exibe na bandeja
5. Avisos + badge atualizam

**Conclusão do teste foreground:** o app consegue persistir quando o Service é invocado. O payload atual é **somente bloco `notification`**, sem `data`.

---

## 4. Teste com aplicativo fechado / background

Com o mesmo tipo de envio da Central e o app fechado (ou em background):

- a notificação **aparece na bandeja** do Android;
- **não** aparece log `FCM MESSAGE RECEIVED` / `onMessageReceived`;
- **não** há insert no Room;
- "Avisos e Comunicados" permanece sem a mensagem (até eventual toque — e mesmo no toque, sem `data`, não há title/body nos extras).

### Logcat relacionado (canal)

```text
FirebaseMessaging W Unable to log event: analytics library is missing
FirebaseMessaging W Notification Channel requested (default) has not been created by the app. Manifest configuration, or default, value will be used.
```

Observação: o warning de canal `default` foi tratado no app com criação do canal literal `"default"` (compatibilidade). Isso **não** altera a persistência no Room.

---

## 5. Comportamento oficial do FCM (aplicável)

| Estado do app | Payload | `onMessageReceived` | Bandeja | Room (app atual) |
|---------------|---------|---------------------|---------|------------------|
| Foreground | `notification` | Sim | App (`showNotification`) | Sim |
| Background / fechado | `notification` (sem ou com data) | **Não** (sistema trata a UI) | Sistema | **Não** (Service não roda) |
| Background / fechado | `data-only` + prioridade alta | Sim | App (`showNotification`) | Sim |

O app atual só persiste de forma confiável em:

1. `onMessageReceived` → Room  
2. Toque na notificação → extras do Intent → Room (**exige** `title`/`body` no `data`)

Com `remoteMessage.data: {}`, o caminho (2) também não popula Avisos.

---

## 6. Causa raiz confirmada

O sistema de envio da Central está mandando mensagens no formato **notification-only** (bloco `notification`, `data` vazio).

Isso explica todos os sintomas:

1. Foreground → Service recebe → Room OK  
2. Background/fechado → Android mostra bandeja → Service **não** recebe → Room vazio  
3. Token, canal e Firebase em si estão OK  

**Canal de notificação** e **persistência no Room** são problemas separados. O canal afeta apenas warning/UX da bandeja; o Room depende do tipo de payload.

---

## 7. O que já foi feito no app (sem mudar o backend)

- Bridge FCM / registro de token na Central (WebView pós-auth)  
- Persistência unificada `PushNotificationRepository`  
- Persistência no tap da bandeja (quando há extras de `data`)  
- Dedupe por `messageId` / hash  
- Migration Room 1→2 segura  
- Canais `central_alfatech_channel` + `default`  
- Badge / marcar como lidas em Avisos  

**Pendente:** alteração no **sistema que envia** as mensagens.

---

## 8. Próximo passo necessário (backend / envio) — NÃO implementado

Migrar o envio para **data-only** com prioridade alta no Android, **sem** bloco `notification`:

```json
{
  "message": {
    "token": "TOKEN_DO_CLIENTE",
    "data": {
      "title": "Título",
      "body": "Mensagem",
      "type": "general",
      "url": "https://...",
      "message_id": "ID_UNICO"
    },
    "android": {
      "priority": "HIGH"
    }
  }
}
```

### Fluxo esperado após a mudança no envio

```text
FCM data-only + HIGH
  → onMessageReceived()
  → Room (persistFromPush)
  → showNotification()
  → Avisos + badge
```

Funciona com app aberto, em background ou fechado (desde que o sistema entregue a data message ao Service).

---

## 9. Alternativa inferior (não recomendada como solução principal)

Manter `notification` **e** espelhar os mesmos campos em `data`:

- bandeja continua pelo sistema em background;
- Room só no **toque** do usuário;
- se o usuário dispensar sem abrir, Avisos continuam vazios.

---

## 10. Conclusão

| Item | Status |
|------|--------|
| FCM / token / entrega | OK |
| Room / Avisos em foreground | OK |
| Room com app fechado | Falha por payload `notification` + `data: {}` |
| Correção no app para esse sintoma | Insuficiente sozinha |
| Correção necessária | Envio **data-only** (`title`, `body`, `type`, `url`, `message_id`) + `android.priority = HIGH` |

**Implementação do backend: NÃO realizada** na data deste diagnóstico.
