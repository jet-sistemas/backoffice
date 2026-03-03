# Backoffice - Backend

- versão 1.0.0
- data atualização: 22/02/26

## Objetivos Gerais - Desenvolvimento dos módulos restantes

### Módulo III - Gestão de Patrocinadores

- Cadastro completo de patrocinadores
- Gerenciamento de benefícios fornecidos aos associados
- Exibição dos patrocinadores na plataforma como forma de publicidade

### Módulo IV - Gestão de Associados

- Cadastro e segmentação
- ​Controle de mensalidades
- Sinalização de vencimentos

### Módulo V - Áreas logadas

- Área de validação de associado para patrocinadores
- Área do associado com visualização de dados, status e benefícios

### Enciclopédia

#### Associação Desportiva Artística e Cultural Joyce e Teatino

É uma associação que deseja transformar vidas através do esporte. Possuem um time de vôlei e pretendem expandir a quantidade de esportes. Realizam campeonatos e ajudam jovens a realizar sonhos através do esforço esportivo, artístico e cultural. Formas de monetização incluem auxílio municipal, patrocínio de políticos e empresas privadas em troca de publicidade, entusiastas e atletas que se vinculam tornando-se associados com contribuição mensal.

#### Parceiros

Nomenclatura geral para se referir a todas as personas relacionadas a associação Jet. São eles: **Associados** e **Patrocinadores**

#### Patrocinadores

Através de incentivo financeiro e benefícios cedidos aos associados, trocam o montante por publicidade. É possível se tornar um patrocinador de três tiers diferentes: Bronze, Prata ou Ouro. Sendo o último mais relevante, com maior espaço publicitário. Não cabe ao sistema a logística e discernimento para diferir os tiers, pois ficará a cargo do admin do sistema, bastando o backoffice poder identificar e classificar os tipos de patrocinadores, inclusive se o "acordo" está vigente ou não.

Ao se tornar patrocinador terá acesso dependendo do tier:

- Visibilidade em eventos: Sua marca exposta em todos os campeonatos e eventos esportivos.
- Divulgações nas redes: Presença garantida em todas as publicações e redes sociais da associação.
- Categorias Exclusivas: Escolha entre Ouro, Prata ou Bronze com benefícios diferenciados.
- Destaque na Plataforma: Logo e informações da empresa em destaque no site oficial.

#### Associados

Entusiastas e atletas que desejam fazer parte da associação. Eles terão acesso a campeonatos, treinos e benefícios exclusivos dos assinantes como desconto e vantagens.

- Treinos Regulares: Participe de treinos semanais de vôlei com orientação profissional.
- Campeonatos: Acesso garantido a todos os campeonatos organizados pela associação.
- Benefícios Exclusivos: Descontos e vantagens em parceiros comerciais da associação.
- Comunidade Ativa: Faça parte de uma comunidade apaixonada por esportes no sul do Piauí.

#### Adm's

São os administradores da plataforma. Eles terão acesso ao backoffice e gerenciarão a plataforma como um todo.

### Requisitos

#### Módulo III - Gestão dos Patrocinadores

##### ADM - Caso de Uso 

- Deve ser capaz de logar na plataforma
  - Ao fazer login deve visualizar os patrocinadores ativos na plataforma
- ADM deve ser capaz de criar um novo usuário (table_name `users`)
  - Durante o cadastro, deve-se salvar as informações relativas aos `members` ou `sponsors` dependendo do `users.type`
  - Deve vir ativo por padrão
- ADM deve ser capaz de atualizar um _sponsor_
  - Ao desativar um _sponsor_, os beneficios relacionados a ele devem ser omitidos
  - Somente _sponsors_ ativos devem aparecer no site
    - O endpoint de listagem deve conter filtro por `tier`
  - Ao desativar um _sponsor_, o campo `last_active_sponsorship` deve ser preenchido com a data
  - ADM deve ser capaz de atualizar os campos do _sponsors_: `public_name`, `document`, `tier`, `logo_url`, `site`, `instagram`, `whatsapp` ao mesmo tempo mas não necessariamente, se optar por atualizar somente um único campo, deve ser possível
    - As logos devem ser salvas no R2 da cloudflare e somente o path do arquivo estático salvo no banco de dados
      - Salvar no bucket chamado: `sponsors_logo`

#### Relacionamentos entre parceiros (Adm's, Sponsors, Members)

- **User como raiz de parceiros**
  - Todo `Sponsor` e todo `Member` deve estar vinculado a exatamente um `User` (via `user_id`), conforme o DER em `backoffice.dbml`.
  - O campo `users.type` indica se existirão registros em `sponsors`, `members` ou ambos:
    - `ADM` → apenas acesso de administração (pode não ter `Sponsor`/`Member` vinculado).
    - `SPONSOR` → deve existir um registro em `sponsors` vinculado ao mesmo `User`.
    - `MEMBER` → deve existir um registro em `members` vinculado ao mesmo `User`.
    - `SPONSOR_MEMBER` → **ATENÇÃO – POSSÍVEL CONFLITO**: o DER prevê essa possibilidade, mas as regras de negócio ainda não definem se haverá sempre dois registros (`Sponsor` + `Member`) compartilhando o mesmo `User` ou outra modelagem; essa decisão deve ser explicitada antes de implementar.

- **Members, tipos e vínculos**
  - `Member.type = SUBSCRIBER`:
    - Deve existir um registro correspondente em `subscriber_member` (configuração de cobrança) para o mesmo `member_id`.
    - O status de cobrança em `subscriber_member.status` deve ser coerente com o status apresentado na área logada (ATIVO, ATRASADO, DESATIVADO).
  - `Member.type = SPONSORED`:
    - Deve existir pelo menos um registro ativo em `sponsored_member` vinculado ao `Member`.
    - Quando não houver nenhum patrocínio ativo, o `Member` não deve ser tratado como `SPONSORED` para fins de benefícios que dependem de patrocínio da associação.

- **Benefícios e patrocinadores**
  - Se `benefits.sponsor_id` estiver preenchido:
    - O benefício é considerado “patrocinado” por aquele `Sponsor`.
    - Se o `Sponsor` estiver inativo (`is_active = false`), seus benefícios não devem ser exibidos/publicados para novos usos, mas podem ser mantidos para histórico.
  - Se `benefits.sponsor_id` for nulo:
    - O benefício é geral (vinculado apenas à associação) e não depende de patrocinador específico.

- **Check-in patrocinador ↔ associado (`sponsors_members_checkin`)**
  - Cada registro em `sponsors_members_checkin` representa um evento de validação entre um `Sponsor` e um `Member` em uma data/hora específica.
  - A combinação `(sponsor_id, member_id)` deve sempre ser coerente com o estado atual:
    - Podem existir múltiplos registros ao longo do tempo.
    - Sempre que um `member` passar por uma validação com um `sponsor` (parceiro) um registro nestra tabela deverá ser criada para fins de histórico, se o `member` foi validado ou não

- **Desativação e efeitos em cadeia**
  - Ao desativar um `Sponsor`:
    - Seus benefícios (`benefits` com `sponsor_id` correspondente) deixam de ser exibidos, independentemente de `benefits.is_active`.
    - Check-ins históricos continuam válidos apenas para consulta, não para validações futuras.
  - Ao mudar o tipo de um `Member` (`SUBSCRIBER` ↔ `SPONSORED`):
    - O registro deste membro em `subscriber_member` deve conter o `status` = `INACTIVE`
    - Um `subscriber_member` com `status = INACTIVE` significa OU que o membro foi desligado administrativamente por algum motivo OU que ele se tornou um membro patrocinado
    - A dinâmica entre `sponsored_member.is_active` e `subscriber_member.status` deve suprir todos os casos
    - Não é possível ser um `member` assinante e patrocinado ao mesmo tempo


#### Módulo IV - Gestão dos Membros (associados)
##### ADM - Caso de Uso
- Deve ser capaz de criar um membro
- Deve ser capaz de visualizar os membros que estão ativos e os que estão com a mensalidade atrasada
  - Discernir se: Membro está ATIVO, ATRASADO (falta pagar mensalidade) ou DESATIVADO

#### Módulo V - Áreas Logadas
##### ADM - Caso de Uso
- Durante a criação de um novo usuário
  - Um email deverá ser enviado ao _user_ para ele criar a sua senha
    - O email deverá conter:
      - Template com logo da associação, texto informando o clique do botão ou copiar/colar link no navegador para salvar a senha
      - O link deverá ter um código de uso único, ao ser utilizado perde valia
      - Se for _sponsor_
        - Ao abrir o link de criar senha ele deve informar o CNPJ, o código e a nova senha
      - Se for _member_
        - Ao abrir o link de criar senha ele deve informar o CPF, o código e a nova senha
    - Em caso de falha ou envio para email indevido o ADM deve ser capaz de enviar um novo código por e-mail
    - Não deve existir dois códigos ativos para o mesmo usuário
      - No reenvio do código, deve validar se o primeiro já está desativado, se não, desativá-lo


#### Módulo Extra - Ideias
- Ao fazer login:
  - Se for ADM, deve visualizar um dashboard contendo:
    - Quantidade de _sponsors_ ativos
    - Quantidade de _members_ ativos
    - _Members_ com mensalidade pendente
    - Quantidade total de benefícios ativos