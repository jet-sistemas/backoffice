# Plano de Execução — Módulo Gestão de Membro (ADM) + Mensalidade (ADM)

## Objetivo
Entregar o módulo em ciclos funcionais completos, cada ciclo com backend + frontend + testes, priorizando valor de negócio e baixo risco de integração.

## Escopo focado
- Gestão de membros pelo ADM.
- Controle de mensalidade pelo ADM.
- Observabilidade mínima de status e histórico para operação.

## Estratégia por ciclos

### Ciclo 1 — Fundação de domínio e cadastro de membro (MVP operacional)
**Resultado do ciclo:** ADM consegue criar/listar/visualizar membro com segmentação inicial (`SUBSCRIBER`/`SPONSORED`) e dados mínimos consistentes.

**Backend**
- Implementar camada completa de membro (`resource`/`api`/`service`/`repository`/`dtos`/`mapper`) em `backoffice/v1`.
- Habilitar criação de usuário com tipo `MEMBER` no fluxo ADM, removendo bloqueio atual de “não implementado” para esse tipo.
- Persistir vínculo obrigatório `user -> member` com validações de unicidade e regras de entrada na borda REST.
- Incluir listagem paginada e busca por filtros administrativos essenciais (status ativo, tipo, busca textual).

**Frontend (backoffice-front)**
- Criar tela ADM de listagem de membros com paginação/filtros.
- Criar fluxo de cadastro de membro com formulário segmentado por tipo.
- Exibir detalhe básico do membro (dados cadastrais + segmentação).
- Encapsular consumo de API em hooks TanStack Query (sem chamadas Axios diretas em páginas).

**Testes backend**
- Integração de endpoints: create/list/get de membro via ADM.
- Casos de erro: payload inválido, tipo inválido, duplicidade (email/document/code/whatsapp).
- Contrato de resposta com `ResponseModel` e paginação.

**Testes frontend**
- Testes de hooks (query/mutation) para sucesso/erro normalizado.
- Testes de tela: render de listagem, filtros, submissão de cadastro e feedback de erro.

---

### Ciclo 2 — Controle de mensalidade do assinante (núcleo financeiro)
**Resultado do ciclo:** ADM consegue configurar e atualizar mensalidade de membro `SUBSCRIBER` com regras de cobrança válidas e status calculado.

**Backend**
- Implementar casos de uso para `subscriber_member`:
  - definir/alterar valor da mensalidade;
  - definir/alterar dia de cobrança (1–31);
  - recalcular `nextDueDate` com tratamento de meses curtos;
  - atualizar `status` (`ACTIVE`, `DUE_SOON`, `OVERDUE`, `INACTIVE`) conforme regras de negócio.
- Adicionar endpoints ADM para leitura/atualização do estado de mensalidade por membro.
- Garantir idempotência e consistência transacional nas alterações.

**Frontend (backoffice-front)**
- Tela de configuração de mensalidade no detalhe do membro assinante.
- Exibição de status atual, próximo vencimento e última atualização.
- Validações de formulário (faixas de valor e dia de cobrança) e UX de loading/erro.

**Testes backend**
- Casos de cálculo de vencimento (incluindo dia 29/30/31 em meses menores).
- Transições de status por cenário de data.
- Testes de atualização parcial e validação de limites.

**Testes frontend**
- Testes de fluxo de edição de mensalidade (sucesso/erro).
- Testes de renderização correta de status e data projetada.

---

### Ciclo 3 — Operação ADM completa do membro (edição, inativação e histórico)
**Resultado do ciclo:** ADM fecha o ciclo de vida do membro (editar/inativar/reativar) e acompanha histórico operacional.

**Backend**
- Implementar update completo de membro (dados cadastrais e segmentação permitida).
- Implementar inativação/reativação lógica de membro com efeitos coerentes em mensalidade.
- Implementar consulta de histórico de check-ins do membro (quando existir registro) para visão ADM.
- Garantir coerência entre `member.type`, `subscriber_member.status` e vínculos patrocinados ativos.

**Frontend (backoffice-front)**
- Ações administrativas de editar, inativar e reativar membro.
- Aba/seção de histórico de check-ins no detalhe do membro.
- Estados de confirmação, loading otimista e invalidação de cache.

**Testes backend**
- Cenários de inativação/reativação com validação de efeitos em cadeia.
- Testes de consulta de histórico e filtros.

**Testes frontend**
- Testes de ações críticas (inativar/reativar) e atualização de UI pós-mutação.
- Testes da visualização de histórico (vazio/com dados/erro).

---

### Ciclo 4 — Hardening, regressão e prontidão de release
**Resultado do ciclo:** módulo estável para produção com cobertura de regressão e critérios de aceite validados.

**Backend**
- Revisão de mensagens de erro padronizadas (`MessageErrorEnum`) e mapeadores globais.
- Revisão de permissões/roles e validações de borda REST (`@Valid`, `@BeanParam`).
- Suite de regressão para fluxos ADM de usuário/sponsor não impactados.

**Frontend (backoffice-front)**
- Revisão de UX de erro/loading para todos os fluxos do módulo.
- Ajustes finais de acessibilidade e consistência visual.
- Testes de regressão dos fluxos administrativos correlatos.

**Testes E2E (opcional recomendado)**
- Cenário ponta a ponta: criar membro -> configurar mensalidade -> editar -> inativar/reativar.

## Critérios de pronto por ciclo
- Endpoints e telas do ciclo operacionais em ambiente local.
- Testes backend e frontend do ciclo passando em CI/local.
- Sem regressão crítica em fluxos ADM existentes.
- Regras documentadas do módulo refletidas no comportamento implementado.

## Dependências e riscos
- Dependência da definição final de regras de transição entre `SUBSCRIBER` e `SPONSORED` em cenários limítrofes.
- Risco de inconsistência temporal no cálculo de vencimento (mitigado com testes de datas de borda).
- Risco de divergência backend/frontend no contrato de filtros e paginação (mitigado com contratos e testes de integração).

## Arquivos-alvo prioritários (implementação posterior)
- Backend: `src/main/java/backoffice/v1/{openapi/api,resources,services,repositories,dtos}`
- Frontend: `backoffice-front/src/{pages,components,hooks,api}`
- Testes backend: `src/test/java/...`
- Testes frontend: `backoffice-front/src/**/*.test.ts(x)`
