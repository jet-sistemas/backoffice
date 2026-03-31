---
name: Dedup stack rules
overview: Deduplicar a stack entre architecture e code-style; criar skill de histórico de falhas; enriquecer know-how-quarkus como roteador (rules → Quarkus → runbook) sem duplicar normas.
todos:
  - id: edit-architecture
    content: "Remover ## Stack do Projeto de architecture.mdc; adicionar ponteiro à Stack Tecnológica em code-style.mdc"
    status: completed
  - id: optional-code-style-note
    content: (Opcional) Uma linha em code-style.mdc indicando que a stack é canónica só ali
    status: completed
  - id: skill-historico-falhas
    content: Criar .cursor/skills/historico-falhas/SKILL.md com triggers + leitura obrigatória de historico-falhas.mdc
    status: completed
  - id: adapt-historico-mdc
    content: Ajustar historico-falhas.mdc (nota de skill + manter regra requestable como espelho/descoberta)
    status: completed
  - id: enrich-know-how-quarkus
    content: "Atualizar know-how-quarkus/SKILL.md: caminhos corretos + fluxo em 3 passos + ponte para runbook"
    status: completed
  - id: verify-grep
    content: Verificar com grep que não resta lista duplicada de stack nas rules
    status: completed
isProject: false
---

# Plano: stack única nas Cursor rules

## Decisão

- **Manter** o bloco **## Stack Tecnológica** (lista com Java 25, Quarkus 3.31.4, Panache, JAX-RS, JWT, PostgreSQL, Lombok, ModelMapper, BCrypt, Hibernate Validator) **apenas** em `[/.cursor/rules/code-style.mdc](/home/carlos/Documents/codes/work/jet/backoffice/.cursor/rules/code-style.mdc)`.
- **Remover** o bloco **## Stack do Projeto** (e a lista equivalente) de `[/.cursor/rules/architecture.mdc](/home/carlos/Documents/codes/work/jet/backoffice/.cursor/rules/architecture.mdc)`.

**Por quê aqui:** o próprio `architecture.mdc` já afirma que `[code-style.mdc](/home/carlos/Documents/codes/work/jet/backoffice/.cursor/rules/code-style.mdc)` é a fonte da verdade para estilo, estrutura de pacotes e regras arquiteturais; a stack é contexto técnico imediato antes de **Estrutura de Pacotes** no `code-style`, evitando duas listas a atualizar (versão Quarkus/Java, etc.).

## Alterações concretas

### 1. `[architecture.mdc](/home/carlos/Documents/codes/work/jet/backoffice/.cursor/rules/architecture.mdc)`

- Apagar a secção `## Stack do Projeto` e os nove bullets (linhas atuais ~7–17).
- Logo após a linha de identidade (“Você é um especialista…”), inserir **uma** linha de encaminhamento, por exemplo: referência explícita à stack no início de `code-style.mdc` (nome da secção **Stack Tecnológica**).
- **Preservar** o blockquote existente que aponta para `code-style.mdc` como fonte da verdade (pode ficar imediatamente a seguir ao ponteiro da stack, para não perder o fluxo “quem manda no detalhe”).

### 2. `[code-style.mdc](/home/carlos/Documents/codes/work/jet/backoffice/.cursor/rules/code-style.mdc)`

- **Nenhuma alteração obrigatória** no conteúdo da stack: permanece como única lista canónica.
- Opcional (sua escolha): uma linha curta sob **Stack Tecnológica** do tipo “lista canónica da stack do projeto — atualizar aqui ao mudar versões”, para deixar explícito no ficheiro que é a referência única.

## Fora de escopo (sugestão rápida, não bloqueante)

- Corrigir em `[.cursor/skills/know-how-quarkus/SKILL.md](/home/carlos/Documents/codes/work/jet/backoffice/.cursor/skills/know-how-quarkus/SKILL.md)` a referência inexistente a `.cursor/architecture.md` para `.cursor/rules/architecture.mdc` (e, se desejado, mencionar `code-style.mdc` para stack) — **não** faz parte da remoção do cabeçalho duplicado, mas evita grounding errado.

## Verificação

- Grep por “Java 25” ou “Stack do Projeto” em `.cursor/rules/` — deve aparecer **uma** lista de stack (só em `code-style.mdc`).
- Ler o início de `architecture.mdc` para garantir que a introdução + blockquote leem bem sem a secção removida.

