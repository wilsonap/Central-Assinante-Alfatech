# 30 — selectedUri única (sem carimbo)

## Auditoria
- `stampCopy` existia em `ReceiptImageStamper` mas já não era chamado no source.
- Cópia `original_*` no share mudava a URI (ids diferentes).
- Stamper ainda usado só em `createCameraTarget`.

## Correção
- Estado único: `selectedUri` → preview (`ImageView.setImageURI`) → `EXTRA_STREAM`
- Sem Stamper no fluxo; câmera via `ReceiptShareHelper.createCameraCaptureUri`
- Sem cópia no share
- Logs: `RECEIPT_ORIGINAL_URI` / `RECEIPT_PREVIEW_URI` / `RECEIPT_SHARE_URI` + `sameOriginalPreview` / `sameOriginalShare`
