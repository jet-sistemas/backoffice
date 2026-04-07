---
name: historico-falhas
description: Runbook de falhas conhecidas no desenvolvimento local (porta 8080, quarkus:dev, tasks VS Code/Cursor, bind, processo órfão). Consultar antes de sugerir kill genérico ou tasks duplicadas.
alwaysApply: false
---

## Purpose

Orientar a descoberta e o diagnóstico de **problemas de ambiente e de subida do Quarkus** já documentados no repositório, sem duplicar o texto canónico do runbook.

## When to Use

- Log com **“Port 8080 seems to be in use”** ou servidor que não sobe após `./mvnw quarkus:dev` / task `quarkus:dev`.
- Quarkus ou REST não responde após fechar terminal, live reload ou processo aparentemente órfão.
- Utilizador relata o mesmo sintoma de uma entrada já tratada no projeto (ex.: porta em uso, tasks de liberar porta).
- Antes de sugerir apenas `kill` manual, `netstat`/`lsof` genéricos ou **criar nova task** para liberar porta — ver o que o repo já define.
- Diagnóstico de **dev local** (VS Code/Cursor tasks, `tasks.json`).

## Instructions

1. Ler **na íntegra** o ficheiro canónico `.cursor/rules/historico-falhas.mdc` e **priorizar** as soluções e os “o que NÃO fazer” já registados.
2. **Não** copiar para esta skill entradas longas do runbook — o detalhe mantém-se só no `.mdc`.
3. **Não** substituir ou resumir normas de código: arquitetura e estilo estão em `.cursor/rules/architecture.mdc` e `.cursor/rules/code-style.mdc`.
4. Para **nova** falha do projeto: acrescentar **apenas** uma secção em `.cursor/rules/historico-falhas.mdc`, no formato descrito no próprio ficheiro.
