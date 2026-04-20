# Demanda Backend - Upload de Imagens com R2 (Fluxo Seguro com Signed URL)

## Objetivo

Implementar fluxo de upload de imagens para Cloudflare R2 sem expor credenciais no frontend, com autorizacao via backend e upload direto do cliente para o bucket.

## Escopo Funcional

- Criar endpoint para iniciar upload e retornar signed URL (`PUT`).
- Criar endpoint para confirmar upload e persistir vinculo da imagem com entidade de dominio (ex.: sponsor).
- Criar endpoint para exclusao de imagem com autorizacao.
- Garantir validacoes de seguranca (tipo, tamanho, prefixo de chave).
- Nao receber imagem em base64 no backend para upload principal.

---

## Endpoints

### 1) POST `/admin/uploads/init`

Inicia o processo de upload e retorna URL assinada para envio direto ao R2.

#### Request (JSON)
```json
{
  "entity": "sponsor",
  "entityId": "uuid-do-sponsor",
  "fileName": "logo.png",
  "contentType": "image/png",
  "size": 245312
}
```

#### Regras
- Validar autenticacao/autorizacao do usuario para a entidade.
- Validar `contentType` permitido:
  - `image/png`
  - `image/jpeg`
  - `image/webp`
- Validar `size` (ex.: maximo 5MB, configuravel).
- Gerar `objectKey` no padrao:
  - `sponsors/{entityId}/{yyyy}/{MM}/{timestamp}_{hash}.{ext}`
- Garantir que o key gerado esteja em prefixo permitido (nunca arbitrario vindo do cliente).

#### Response (200)
```json
{
  "objectKey": "sponsors/uuid-do-sponsor/2026/03/1711800123_a1b2c3.png",
  "uploadUrl": "https://...signed-put-url...",
  "publicUrl": "https://cdn.seudominio.com/sponsors/uuid-do-sponsor/2026/03/1711800123_a1b2c3.png",
  "expiresIn": 300
}
```

#### Erros
- `400` payload invalido
- `401` nao autenticado
- `403` sem permissao
- `413` arquivo excede tamanho maximo
- `415` content type nao suportado
- `500` erro ao gerar URL assinada

---

### 2) POST `/admin/uploads/confirm`

Confirma upload ja realizado no R2 e persiste referencia no dominio.

#### Request (JSON)
```json
{
  "entity": "sponsor",
  "entityId": "uuid-do-sponsor",
  "objectKey": "sponsors/uuid-do-sponsor/2026/03/1711800123_a1b2c3.png"
}
```

#### Regras
- Validar autenticacao/autorizacao.
- Validar que `objectKey` pertence ao prefixo permitido da entidade.
- (Opcional recomendado) Verificar existencia do objeto no bucket antes de confirmar.
- Persistir no banco:
  - `entity`, `entityId`, `objectKey`, `url`, `mimeType` (quando disponivel), `size` (quando disponivel), `uploadedBy`, `createdAt`.
- Se ja existir imagem ativa da entidade:
  - estrategia recomendada: marcar antiga para remocao assincrona (ou remover apos sucesso do novo vinculo).

#### Response (200)
```json
{
  "id": "uuid-da-imagem",
  "entity": "sponsor",
  "entityId": "uuid-do-sponsor",
  "objectKey": "sponsors/uuid-do-sponsor/2026/03/1711800123_a1b2c3.png",
  "url": "https://cdn.seudominio.com/sponsors/uuid-do-sponsor/2026/03/1711800123_a1b2c3.png"
}
```

#### Erros
- `400` payload invalido
- `401` nao autenticado
- `403` sem permissao
- `404` objeto nao encontrado no bucket (se validacao ativa)
- `409` conflito de estado
- `500` erro interno

---

### 3) DELETE `/admin/uploads`

Exclui imagem com autorizacao.

#### Request (JSON)
```json
{
  "entity": "sponsor",
  "entityId": "uuid-do-sponsor",
  "objectKey": "sponsors/uuid-do-sponsor/2026/03/1711800123_a1b2c3.png"
}
```

#### Regras
- Validar autenticacao/autorizacao.
- Validar prefixo permitido.
- Remover objeto no R2.
- Remover/atualizar vinculo no banco.
- Operacao idempotente: se nao existir, retornar sucesso logico quando apropriado.

#### Response (200)
```json
{
  "success": true
}
```

#### Erros
- `400`, `401`, `403`, `500`

---

## Requisitos Nao Funcionais

- Nao expor secret/access key do R2 ao cliente.
- Assinatura de upload com expiracao curta (ex.: 5 min).
- Logs estruturados com `requestId`, `userId`, `entity`, `entityId`.
- Rate limit para `init` e `confirm`.
- Observabilidade: metricas de sucesso/falha por endpoint.
- Configuracao por ambiente (bucket, CDN base URL, limite de tamanho).

---

## Seguranca (Obrigatorio)

- Bloquear `objectKey` arbitrario enviado pelo cliente (sempre validar prefixo e formato).
- Validar `contentType` e tamanho.
- Sanitizar nome original de arquivo (nao confiar em `fileName`).
- Nao aceitar base64 para fluxo principal de upload.
- Aplicar autorizacao por papel/permissao de dominio.

---

## Criterios de Aceite

- [ ] Front consegue iniciar upload e receber signed URL.
- [ ] Upload direto para R2 funciona com arquivo binario.
- [ ] Confirmacao persiste vinculo da imagem no dominio.
- [ ] Exclusao remove no bucket e no dominio.
- [ ] Fluxo bloqueia tipos/tamanhos invalidos.
- [ ] Segredos R2 nao aparecem no frontend.
- [ ] Testes automatizados dos tres endpoints cobrindo casos de sucesso e erro.
