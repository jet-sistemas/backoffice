# Native prod — deploy paralelo no Railway

Guia passo a passo para subir uma **nova instância `backoffice`** no ambiente **production** do Railway, rodando imagem **native** (GraalVM) publicada no GHCR, **sem alterar** o serviço `backoffice-prod` (prod atual em JVM).

## Visão geral

| Ambiente | Serviço | Runtime | Fonte do deploy |
|---|---|---|---|
| develop | `backoffice` | JVM + sleep | GitHub repo (branch develop) |
| production | `backoffice-prod` | JVM | GitHub repo — **não mexer até cutover** |
| production | `backoffice` (novo) | Native | `ghcr.io/jet-sistemas/backoffice:native` |

Fluxo automatizado (após setup manual):

```
push na main → GitHub Actions → build native → push GHCR → redeploy backoffice (production)
```

---

## A. Pré-requisitos

- Acesso admin ao projeto Railway **jet-project** (envs `develop` e `production`).
- Acesso admin ao repositório GitHub **jet-sistemas/backoffice**.
- Docker habilitado no GitHub Actions (runners `ubuntu-latest` já incluem).
- Anotar **todas** as variáveis de ambiente do serviço `backoffice-prod` (production) antes de começar — serão copiadas para o novo serviço.
- Anotar o domínio customizado atual (ex.: `admin-api.associacaojet.co...`) — permanece em `backoffice-prod` até o cutover.

---

## B. GHCR (GitHub Container Registry)

A imagem é publicada automaticamente pelo workflow `.github/workflows/native-prod.yml` na primeira execução bem-sucedida.

**Package:** `ghcr.io/jet-sistemas/backoffice`  
**Tag usada pelo Railway:** `native`  
**Tag adicional por commit:** SHA do git (`ghcr.io/jet-sistemas/backoffice:<sha>`)

### Primeiro push

1. Faça merge do workflow na branch `main` (ou dispare manualmente: Actions → **Native prod** → **Run workflow**).
2. Aguarde o job **Build and push image** concluir.
3. No GitHub: **Packages** → `backoffice` → confirme tags `native` e `<sha>`.

### Visibilidade do package

**Opção 1 — Público (mais simples)**

1. Package → **Package settings** → **Change visibility** → **Public**.
2. Railway puxa a imagem sem credenciais extras.

**Opção 2 — Privado**

1. Crie um PAT (Personal Access Token) no GitHub com scope `read:packages`.
2. No Railway, ao criar o serviço Docker, configure **Registry Credentials**:
   - **Username:** seu usuário GitHub (ou org `jet-sistemas` se aplicável)
   - **Password:** PAT com `read:packages`

---

## C. GitHub Secrets

No repositório **jet-sistemas/backoffice**:

**Settings → Secrets and variables → Actions → New repository secret**

| Secret | Valor | Como obter |
|---|---|---|
| `RAILWAY_TOKEN` | Token de projeto Railway | Railway → **jet-project** → **Project Settings** → **Tokens** → Create token com acesso ao environment **production** |

O workflow usa esse token no passo `railway redeploy --service backoffice --environment production`.

> **Importante:** o redeploy automático só funciona **depois** que o serviço `backoffice` existir no env production (Seção D). Na primeira execução do workflow, o build/push pode passar e o redeploy falhar — isso é esperado.

---

## D. Criar serviço `backoffice` no env production

1. Railway → projeto **jet-project** → selecione environment **production** (dropdown no topo).
2. **+ New** → **Docker Image** (ou **Empty Service** → Settings → Source → Docker Image).
3. Imagem: `ghcr.io/jet-sistemas/backoffice:native`
4. Nome do serviço: **`backoffice`**
   - Railway permite o mesmo nome em envs diferentes (`backoffice` em develop ≠ `backoffice` em production).
5. Se GHCR privado: **Settings → Registry Credentials** (Seção B, opção 2).
6. **Settings → Deploy** → confirme que **não** há `startCommand` JVM — o binário native usa o `ENTRYPOINT` do Dockerfile.

### Variáveis de ambiente

Copie **todas** as vars de `backoffice-prod` para o novo `backoffice` (production).

Referência das vars exigidas pelo código (`application.properties`):

| Variável | Uso |
|---|---|
| `DB_KIND` | Tipo do banco (ex.: `postgresql`) |
| `DB_USERNAME` | Usuário PostgreSQL |
| `DB_PASSWORD` | Senha PostgreSQL |
| `DB_URL` | JDBC URL (Neon/prod) |
| `CORS_ORIGINS` | Origens permitidas (front prod) |
| `STORAGE_ENDPOINT` | Cloudflare R2 endpoint |
| `STORAGE_REGION` | Região R2 |
| `STORAGE_ACCESS_KEY_ID` | Access key R2 |
| `STORAGE_SECRET_ACCESS_KEY` | Secret R2 |
| `STORAGE_BUCKET` | Bucket prod |
| `STORAGE_KEY_ENV_PREFIX` | Prefixo de paths (ex.: `prod`) |
| `STORAGE_UPLOAD_MAX_BYTES` | Limite de upload |
| `STORAGE_UPLOAD_SIGN_TTL_SECONDS` | TTL de URL assinada |
| `STORAGE_UPLOAD_RATE_LIMIT_CAPACITY` | Rate limit upload |
| `STORAGE_UPLOAD_RATE_LIMIT_REFILL_MINUTES` | Refill rate limit |

**Profile Quarkus (se usado):**

```
QUARKUS_PROFILE=prod
```

**Pool JDBC prod (opcional, recomendado):**

```
QUARKUS_DATASOURCE_JDBC_MIN_SIZE=1
QUARKUS_DATASOURCE_JDBC_MAX_SIZE=10
```

**Não copie** vars de sleep do develop:

- `QUARKUS_DATASOURCE_JDBC_BACKGROUND_VALIDATION_INTERVAL=0`
- `QUARKUS_DATASOURCE_JDBC_MIN_SIZE=0`
- `QUARKUS_DATASOURCE_JDBC_IDLE_REMOVAL_INTERVAL=1M`

### Verificar startup

1. Abra **Deployments** → logs do novo `backoffice` (production).
2. Startup native: **sem banner OpenJDK**, tempo de boot em milissegundos.
3. Acesse a URL `.railway.app` gerada pelo serviço.

---

## E. Teste antes do cutover

Use a URL temporária do Railway (não o domínio público ainda).

Checklist de smoke test:

- [ ] `POST /v1/auth/...` — login retorna JWT
- [ ] Endpoint autenticado (ex.: listagem de usuários/patrocinadores)
- [ ] Upload R2 (avatar/logo) se aplicável
- [ ] `/q/openapi` ou `/q/swagger-ui` acessível
- [ ] Comparar respostas com `backoffice-prod` no mesmo endpoint

Se algo falhar, **não faça cutover** — prod público continua em `backoffice-prod`.

---

## F. Cutover de domínio (quando validado)

1. Railway → `backoffice-prod` (production) → **Settings → Networking** → remova custom domain.
2. Railway → `backoffice` (production, native) → **Settings → Networking** → adicione o mesmo custom domain (`admin-api.associacaojet.co...`).
3. Aguarde propagação DNS/SSL (minutos).
4. Monitore logs e métricas:
   - RAM esperada: **~50–80 MB** (vs ~300 MB JVM).
   - CPU continua baixo em idle.
5. Após período de observação (ex.: 24–48 h), **remova ou desligue** `backoffice-prod`.

**Rollback:** reanexe o custom domain em `backoffice-prod` e remova do novo serviço.

---

## G. Troubleshooting

| Problema | Causa provável | Solução |
|---|---|---|
| Workflow falha no build native (SecureRandom / NTLMEngineImpl) | AWS SDK + Apache HttpClient no classpath | Já corrigido em `application.properties` via `--initialize-at-run-time=NTLMEngineImpl,CachedSupplier`. Re-run workflow. |
| Workflow falha no build native (OOM) | GraalVM sem RAM no runner | Workflow já usa `-Dquarkus.native.native-image-xmx=6g`. Re-run. Repo privado tem 7 GB no runner. |
| Workflow falha no passo Docker build | Binário `target/*-runner` ausente | Verifique logs do `mvnw package -Dnative`; corrija erro de compilação/reflexão primeiro. |
| `ImagePullBackOff` no Railway | GHCR privado sem credenciais | Configure Registry Credentials (Seção B) ou torne package público. |
| `railway redeploy` — service not found | Serviço `backoffice` ainda não criado em production | Complete Seção D; re-run workflow ou redeploy manual. |
| `railway redeploy` — unauthorized | `RAILWAY_TOKEN` inválido ou sem scope production | Regere token no Railway; atualize secret no GitHub. |
| App sobe mas 401 em rotas autenticadas | Chaves JWT diferentes entre builds | Chaves `publicKey.pem`/`privateKey.pem` são baked no binário native. Confirme que o build usa as mesmas chaves do prod atual (commitadas em `src/main/resources/`). |
| 500 em runtime (ClassNotFound / reflection) | Dependência incompatível com native | Veja stack trace nos logs. Adicione `@RegisterForReflection` ou `@RuntimeInitializedClass` na classe afetada. |
| BCrypt falha em native | Init em build-time | Registrar `at.favre.lib.crypto.bcrypt.BCrypt` com `@RuntimeInitializedClass` se necessário. |
| Domínio ainda aponta pro serviço antigo | Cutover não feito | Seção F. |
| Billing mostra dois `backoffice` | develop + production com mesmo nome | Normal — Railway cobra por instância/ambiente; são serviços distintos. |
| Primeiro workflow: push OK, redeploy falha | Ordem esperada na migração | Crie serviço manualmente (Seção D); próximo push/re-run redeploya. |

### Re-executar workflow sem push

GitHub → **Actions** → **Native prod** → **Run workflow** → branch `main`.

### Logs úteis no Railway

- **Build logs:** N/A (imagem vem pronta do GHCR).
- **Deploy logs:** startup do binário `./application`.
- Erros native em runtime aparecem como stack trace direto (sem JVM wrapper).

---

## H. Referência técnica

- **Workflow:** `.github/workflows/native-prod.yml`
- **Dockerfile runtime:** `src/main/docker/Dockerfile.native-micro`
- **Profile Maven native:** `./mvnw package -Dnative -Dquarkus.native.container-build=true`
- **Dev (JVM + sleep):** continua via repo + `railway.toml` no env develop — não afetado por este guia.
