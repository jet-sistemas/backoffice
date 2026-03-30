# Demanda Frontend - Integracao de Upload de Imagens com R2 (via Signed URL)

## Objetivo

Implementar upload de imagem no frontend sem base64, usando fluxo de signed URL emitida pelo backend, com persistencia final por endpoint de confirmacao.

## Escopo Funcional

- Selecionar imagem via input (`File`).
- Validar no cliente (tipo e tamanho) antes de iniciar upload.
- Chamar API de `init`.
- Fazer `PUT` direto para `uploadUrl`.
- Chamar API de `confirm`.
- Exibir preview e estados de loading/erro.
- Permitir remocao da imagem atual via API de `delete`.

---

## Fluxo de UI/UX

1. Usuario seleciona arquivo.
2. Front valida:
   - tipos permitidos (`image/png`, `image/jpeg`, `image/webp`)
   - tamanho maximo (ex.: 5MB)
3. Front chama `POST /admin/uploads/init`.
4. Front faz upload binario para `uploadUrl` com `PUT`.
5. Front chama `POST /admin/uploads/confirm` com `objectKey`.
6. Front atualiza estado e invalida queries da entidade (ex.: sponsor).
7. Em remocao, chama `DELETE /admin/uploads` e atualiza cache/UI.

---

## Regras Tecnicas

- Nao converter para base64.
- Nao enviar arquivo para backend no fluxo principal.
- Enviar `Content-Type` correto no `PUT`.
- Tratar timeout/expiracao de signed URL:
  - se expirar, reiniciar fluxo com novo `init`.
- Persistir no estado apenas dados necessarios (`objectKey`, `url`).
- Em edicao/substituicao:
  - subir nova imagem
  - confirmar
  - depois remover antiga (ou delegar estrategia ao backend)

---

## Estrutura Sugerida (React + TanStack Query)

### API layer (transport only)

- `uploadsApi.initUpload(payload)`
- `uploadsApi.confirmUpload(payload)`
- `uploadsApi.deleteUpload(payload)`

### Hooks (single source of truth)

- `useUploadInitMutation`
- `useUploadConfirmMutation`
- `useUploadDeleteMutation`
- (opcional) `useDirectUpload` para encapsular `PUT` no `uploadUrl`

### Componente de UI

- `image-upload-field` reutilizavel com props:
  - `entity`, `entityId`
  - `value` (url atual)
  - `onChange`
  - `onRemove`
  - `disabled`

---

## Contratos consumidos pelo frontend

### `POST /admin/uploads/init`
Request:
```json
{
  "entity": "sponsor",
  "entityId": "uuid-do-sponsor",
  "fileName": "logo.png",
  "contentType": "image/png",
  "size": 245312
}
```

Response:
```json
{
  "objectKey": "sponsors/uuid-do-sponsor/2026/03/1711800123_a1b2c3.png",
  "uploadUrl": "https://...signed-put-url...",
  "publicUrl": "https://cdn.seudominio.com/sponsors/uuid-do-sponsor/2026/03/1711800123_a1b2c3.png",
  "expiresIn": 300
}
```

### `POST /admin/uploads/confirm`
Request:
```json
{
  "entity": "sponsor",
  "entityId": "uuid-do-sponsor",
  "objectKey": "sponsors/uuid-do-sponsor/2026/03/1711800123_a1b2c3.png"
}
```

### `DELETE /admin/uploads`
Request:
```json
{
  "entity": "sponsor",
  "entityId": "uuid-do-sponsor",
  "objectKey": "sponsors/uuid-do-sponsor/2026/03/1711800123_a1b2c3.png"
}
```

---

## Estados de Interface

- loading inicial do upload
- progresso de envio (opcional)
- sucesso de upload/confirm
- erro de validacao local (tipo/tamanho)
- erro de `init`
- erro de `PUT` (rede, CORS, URL expirada)
- erro de `confirm`
- removendo imagem

---

## Tratamento de Erros (mensagens amigaveis)

- Arquivo invalido: "Formato nao suportado. Use PNG, JPG ou WEBP."
- Arquivo grande: "A imagem excede o tamanho maximo permitido."
- URL expirada: "O upload expirou. Tente novamente."
- Falha de rede: "Nao foi possivel enviar a imagem. Verifique sua conexao."
- Falha ao confirmar: "Imagem enviada, mas nao confirmada. Tente finalizar novamente."

---

## Criterios de Aceite

- [ ] Upload sem base64.
- [ ] Upload direto para R2 via signed URL.
- [ ] Confirmacao salva vinculo da imagem com entidade.
- [ ] Exclusao funcional da imagem.
- [ ] Queries da entidade sao invalidadas apos sucesso.
- [ ] UX cobre loading, sucesso e erros.
- [ ] Nenhuma credencial de storage no frontend.
