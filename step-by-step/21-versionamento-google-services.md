# 21 — Versionamento de app/google-services.json

## Auditoria
- `.gitignore`: sem regra para `google-services.json`
- `.git/info/exclude`: sem regra
- `git check-ignore`: vazio (não ignorado)
- Estado anterior: arquivo **untracked** (`??`) após ter sido **apagado do Git** no commit `9332f65` (e em commits anteriores)
- Nenhum script/workflow no repo apaga o arquivo

## Ação
- `git add app/google-services.json` (conteúdo intacto)
- Sem alteração em `.gitignore`, Firebase, Gradle, Manifest ou Kotlin

## Resultado
| Item | Valor |
|------|--------|
| Ignorado | NÃO |
| Regra responsável | nenhuma |
| Rastreado (staged) | SIM |
| Caminho | `app/google-services.json` |
