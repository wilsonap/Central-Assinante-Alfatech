# 29 — Comprovante original + dados na mensagem

## Auditoria (antes)
- EXTRA_STREAM usava `stamped_*.jpg` gerado por `ReceiptImageStamper.stampCopy`
- EXTRA_TEXT só tinha nome completo

## Depois
- EXTRA_STREAM = arquivo original (galeria/câmera/PDF; cópia byte-a-byte se FileProvider)
- EXTRA_TEXT = Cliente / Código / Contrato / Enviado em (linhas omitidas se vazias)
- Prévia só visual da URI original; Stamper fora do fluxo (classe mantida)
- Captura dinâmica: nome + código + contrato no storage IXC; `supportWhatsAppNumber` intacto
