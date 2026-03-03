## Entidades (JPA)
- Sempre use camelCase
- Classes, Interfaces e Enums sempre devem vir com primeira letra em caixa alta
- Todas as entidades extendem de `@backoffice/v1/entities/BaseEntity.java`
- Dentro do código usaremos camelCase, mas o banco de dados está em snakeCase
- Todas as entidades conterão as seguintes `annotation` do lombok: 
    - @Entity
    - @Table(name = "relative_name_in_snake_case")
    - @NoArgsConstructor
    - @AllArgsConstructor
    - @Data
    - @Builder
    - @EqualsAndHashCode(callSuper = true)
- Todos os enums que representam atributos das entities devem estar em `@backoffice/v1/entities/enums`
- Todos os enums devem seguir o formato: `public enum nameCamelCaseEnum { ATTR1, ATTR2, ATTR3 }` como exemplificado em `@backoffice/v1/entities/enums/UserTypeEnum.java`
- Todas as entidades devem seguir o formato do exemplo: `@backoffice/v1/entities/User.java`

## Relacionamentos entre entidades (JPA)

- Para qualquer coluna `*_id` que represente chave estrangeira no DER (`backoffice.dbml`), o atributo na entidade Java deve ser o **objeto da entidade relacionada**, e não apenas o tipo primitivo/`UUID`:
  - Ex.: `members.user_id uuid [ref: - users.id, not null]` → na entidade `Member` usar `private User user;` com `@ManyToOne(optional = false)` e `@JoinColumn(name = "user_id", nullable = false)`.
  - Ex.: `sponsors.user_id uuid [ref: - users.id, not null]` → na entidade `Sponsor` usar `private User user;` com o mesmo padrão.
- Quando mapeado como relacionamento:
  - O lado `N` da relação (`ManyToOne`) deve sempre ser o **dono da FK** (ou seja, conter o `@JoinColumn`).
  - Coleções (`@OneToMany`) do lado `1` são opcionais e, quando existirem, devem usar `mappedBy` apontando para o nome do atributo da FK.
- **One-to-one (`1:1`)**:
  - Quando o DER indica que uma entidade de “configuração” depende de outra (ex.: `subscriber_member.member_id [not null]` ligado a `members.id`), a entidade dependente deve usar `@OneToOne` com `@JoinColumn(name = "member_id", unique = true)`.
  - O lado dono do relacionamento 1:1 será sempre a entidade “de configuração” (a que contém a FK), e o outro lado usa `@OneToOne(mappedBy = "...")` se precisar do relacionamento bidirecional.
- **Tabelas de junção com atributos próprios** (ex.: `sponsored_member`, `sponsors_members_checkin`):
  - Sempre devem ser modeladas como **entidades dedicadas** com dois (ou mais) `@ManyToOne` (ex.: `Member member`, `User grantedBy`, `Sponsor sponsor`), em vez de um `@ManyToMany` direto.
  - Quando o DER definir chave composta (ex.: `(member_id, granted_by_user_id) [pk]`):
    - Usar `@EmbeddedId` + FKs para refletir literalmente o DER
    - Nesses casos, as entidades com EmbeddedId não devem estender de `BaseEntity`
- **Enums persistidos**:
  - Toda coluna que referencia um `*_enum` do DER deve ser mapeada como `enum` em `backoffice/v1/entities/enums` com `@Enumerated(EnumType.STRING)` na entidade.
- **Nomenclatura de campos de relacionamento**:
  - Atributos que representam FKs devem usar o nome da entidade alvo em camelCase (ex.: `user`, `member`, `sponsor`) e não `userId`, `memberId`, `sponsorId`.
  - **ATENÇÃO – POSSÍVEL CONFLITO**: classes já existentes como `GeneratePassCode` e `SponsoredMember` atualmente usam atributos primitivos (`userId`, `memberId`, `grantedByUserId`). A regra preferencial deste documento é evoluir para relacionamentos fortes com objetos (`User`, `Member`, etc.); cabe ao time decidir manter esses casos como exceção temporária ou refatorar para o novo padrão.