---
name: know-how-quarkus
description: Orienta o agente sobre como usar a documentação do Quarkus e a arquitetura deste projeto.
alwaysApply: false
---

## Purpose
Provide Quarkus-specific know-how for this project, combining the official docs with the project architecture.

## When to Use
- Use this skill when the user explicitly asks about Quarkus (extensões, configuração, profiles, performance, REST, etc.).
- Use this skill when you need to entender como uma feature do projeto se relaciona com o Quarkus (ex: Panache, CDI, configuração, segurança).
- Do NOT use this skill for generic Java questions that are not related to Quarkus.

## Instructions
- First, consult `.cursor/architecture.md` to understand how this project is structured and how it uses Quarkus (extensões principais, camadas, padrões).
- Then, consult the official Quarkus docs at `https://pt.quarkus.io/` apenas nas seções relevantes para a pergunta do usuário.
- Resuma e adapte a resposta ao contexto deste projeto, evitando copiar documentação em excesso.