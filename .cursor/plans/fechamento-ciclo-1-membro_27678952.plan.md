---
name: fechamento-ciclo-1-membro
overview: Plano detalhado para fechar o Ciclo 1 do módulo de Gestão de Membro (ADM), cobrindo implementação backend, frontend, testes e critérios de pronto.
todos:
  - id: c1-contrato-api
    content: Definir e validar contrato de API para create/list/get de membro ADM
    status: pending
  - id: c1-backend-membro
    content: Implementar camada backend completa de membro e integrar com criação de usuário MEMBER
    status: pending
  - id: c1-testes-backend
    content: Implementar testes de integração backend para sucesso e falhas do ciclo
    status: pending
  - id: c1-frontend-membro
    content: Implementar listagem, cadastro e detalhe básico de membro no frontend com hooks
    status: pending
  - id: c1-testes-frontend
    content: Implementar testes de hooks e telas do fluxo ADM de membro
    status: pending
  - id: c1-aceite
    content: Executar checklist de pronto do ciclo e validar ausência de regressão crítica
    status: pending
isProject: false
---

# Plano Detalhado — Fechamento do Ciclo 1 (Gestão de Membro ADM)

## Objetivo do ciclo
Entregar o fluxo MVP operacional de membro para ADM: **criar, listar e visualizar membro**, com segmentação inicial (`SUBSCRIBER`/`SPONSORED`), mantendo contrato consistente entre backend e frontend e cobertura mínima de testes.

## Escopo funcional fechado
- Cadastro de membro pelo ADM.
- Listagem paginada de membros com filtros essenciais.
- Visualização de detalhe básico do membro.
- Integração frontend com hooks TanStack Query.
- Testes automatizados backend e frontend para os fluxos do ciclo.

## Fora do escopo deste ciclo
- Regras completas de mensalidade (ficam no Ciclo 2).
- Histórico de check-ins e ações de inativar/reativar (Ciclo 3).
- Hardening/regressão completa do módulo (Ciclo 4).

## Entregáveis por frente

### 1) Backend — API e domínio de membro
- Criar estrutura de domínio de membro em `v1`:
  - DTOs de criação, leitura, listagem e filtros.
  - Mapper de conversão `Entity <-> DTO`.
  - Repository com queries de listagem paginada e filtros.
  - Service com regras de negócio do ciclo.
  - Contrato OpenAPI + Resource administrativo para membro.
- Ajustar fluxo ADM de criação de usuário para suportar `MEMBER`:
  - remover bloqueio de “não implementado” para `MEMBER`;
  - criar `Member` vinculado ao `User` no mesmo fluxo transacional;
  - validar segmentação inicial e regras mínimas de consistência.
- Garantir resposta padronizada via `ResponseModel` e paginação com `Pageable`.

### 2) Backend — Regras mínimas de negócio do ciclo
- Validar entradas de borda REST com Bean Validation (`@Valid`, DTOs de query com `@BeanParam` quando necessário).
- Validar unicidade dos campos críticos no fluxo de criação (email/document/code e campos exclusivos de membro quando aplicável).
- Garantir vínculo obrigatório `member.user_id` e consistência com `users.type = MEMBER`.
- Garantir mensagens de erro padronizadas via `MessageErrorEnum`.

### 3) Frontend — Fluxos ADM de membro
- Criar/ajustar tela de listagem de membros com:
  - paginação,
  - filtros essenciais,
  - busca textual.
- Criar fluxo de cadastro de membro com formulário segmentado por tipo inicial.
- Criar página/seção de detalhe básico do membro.
- Implementar hooks dedicados de dados (query/mutation), separando UI de transporte:
  - API module (Axios) -> hooks TanStack Query -> páginas/componentes.

### 4) Testes backend
- Testes de integração para:
  - criar membro (sucesso),
  - listar membro com paginação/filtros,
  - buscar membro por id.
- Casos negativos:
  - payload inválido,
  - enum/tipo inválido,
  - duplicidade de dados únicos,
  - recurso inexistente.
- Asserções de contrato:
  - envelope `ResponseModel`,
  - status HTTP esperados,
  - metadados de paginação.

### 5) Testes frontend
- Testes de hooks:
  - sucesso/erro nas queries e mutations,
  - shape de erro normalizado.
- Testes de tela:
  - render inicial da listagem,
  - aplicação de filtros,
  - cadastro com sucesso e com erro,
  - abertura de detalhe básico.

## Sequência de execução recomendada
1. Definir contrato de API de membro (request/response/filtros/paginação).
2. Implementar backend (DTO -> mapper -> repository -> service -> resource/openapi).
3. Cobrir backend com testes de integração.
4. Implementar frontend (api modules + hooks + telas).
5. Cobrir frontend com testes de hooks e telas.
6. Rodar checklist de aceite do ciclo e validar regressão mínima.

## Critérios de pronto do Ciclo 1
- ADM consegue criar membro com segmentação inicial válida.
- ADM consegue listar membros com paginação e filtros essenciais.
- ADM consegue visualizar detalhe básico do membro.
- Todos os testes do ciclo (backend + frontend) estão passando.
- Não há regressão crítica nos fluxos ADM já existentes.

## Riscos do ciclo e mitigação
- **Risco:** divergência de contrato entre backend e frontend.
  - **Mitigação:** fechar contrato antes da implementação das telas e validar em testes de integração.
- **Risco:** conflito de validações de unicidade entre User e Member.
  - **Mitigação:** centralizar validações no service e cobrir com testes de erro.
- **Risco:** acoplamento indevido no frontend (página chamando API direto).
  - **Mitigação:** forçar arquitetura em camadas (API module + hooks + UI).

## Dependências para iniciar ciclo 2
- Entidade e APIs de membro estáveis.
- Fluxo de criação `MEMBER` consolidado.
- Base de testes pronta para evoluir mensalidade no `subscriber_member`.
