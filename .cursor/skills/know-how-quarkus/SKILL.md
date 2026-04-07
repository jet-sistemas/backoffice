---
name: know-how-quarkus
description: Roteador Quarkus para este repo — rules canónicas, documentação oficial pt.quarkus.io e runbook de falhas (dev/porta/tasks), sem duplicar normas no texto da skill.
alwaysApply: false
---

## Purpose

Combinar o **padrão deste backoffice** com a documentação oficial do Quarkus, em **ordem fixa** de leitura.

## When to Use

- Perguntas sobre Quarkus no contexto deste projeto: extensões, `application.properties` / YAML, perfis `dev`/`test`/`prod`, CDI, REST/JAX-RS, Panache, JWT, testes `@QuarkusTest`, etc.
- Entender como uma feature do código se apoia no Quarkus (camadas, configuração, segurança).
- Falhas de **runtime ou dev** ligadas ao Quarkus (app não sobe, porta em uso, tasks VS Code) — seguir até o passo 3 e abrir o runbook quando aplicável.

Do **not** use for Java genérico sem relação com Quarkus nem com as convenções deste repositório.

## Instructions (ordem fixa — roteador)

Siga os passos **nesta ordem**. Não cole normas longas deste ficheiro; aponte para os `.mdc` e resuma só o necessário na resposta ao utilizador.

### 1. Rules canónicas do repositório

- Ler o necessário em `.cursor/rules/architecture.mdc` (visão geral, OpenAPI, R2, conduta).
- Ler o necessário em `.cursor/rules/code-style.mdc` — a **stack tecnológica** está no início (secção **Stack Tecnológica**); o resto cobre pacotes, validação na borda, JPA, regras 1–13, etc.

### 2. Documentação oficial do Quarkus

- Consultar `https://pt.quarkus.io/` **apenas** nas secções pertinentes à pergunta.

### 3. Runbook de falhas conhecidas (dev / ambiente)

- Se o problema for “não sobe”, bind de porta, mensagem sobre **8080**, ou tasks do Cursor/VS Code: usar a skill **`historico-falhas`** ou ler diretamente `.cursor/rules/historico-falhas.mdc`.
- **Não** inventar procedimentos que o runbook já descartou (ver “O que NÃO fazer” lá).

### Resposta

- Resumir e adaptar ao contexto deste projeto; evitar copiar documentação em excesso.
