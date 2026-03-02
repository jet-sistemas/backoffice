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

##### Entidades

<details>
  <summary><strong>User</strong></summary>

  - `id`
  - `name`
  - `email`
  - `whatsapp`
  - `password`
  - `is_valid_email`
  - `type` - ADM, SPONSOR, MEMBER
  - `created_at`
  - `updated_at`
</details>

<details>
  <summary><strong>Sponsor (patrocinador)</strong></summary>

  - `id`
  - `public_name` - Se for empresa é o nome associado ao CNPJ se for CPF o nome público completo
  - `document` - CNPJ ou CPF
  - `tier` - BRONZE, SILVER, GOLD
  - `logo`
  - `site` | `instagram` | `contact` - Campos de links acionáveis
  - `is_active` - Se há patrocínio ativo ou não
  - `last_active_sponsorship`- data do último patrocinio (preenchido com data quando is_active se torna false)
  - `created_at`
  - `updated_at`
</details>

<details>
  <summary><strong>Member (associado)</strong></summary>

  - `id`
  - `document` - CPF
  - `member_id` - Número do associado, aparecerá na carteirinha dele
  - `is_active` - Se está com a mensalidade em dias
  - `type` - SUBSCRIBER, SPONSORED
  - `created_at`
  - `updated_at`
</details>

<details>
  <summary><strong>Generate_Pass_Code</strong></summary>

  - `code`
  - `is_active`
  - `user_id`
</details>


<details>
  <summary><strong>Benefits</strong></summary>

  - `id`
  - `sponsor_id` - Pode estar vinculado a um sponsor ou não
  - `name`
  - `description`
  - `is_active`
</details>

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