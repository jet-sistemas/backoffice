package backoffice.common.exceptions;

public enum MessageErrorEnum {
  INTERNAL_ERROR("Erro inesperado no servidor."),
  ACCOUNT_INVALID_TO_ACTION("Sua conta ainda não está ativa para efetuar esta ação"),
  USER_NOT_FOUND("Usuário não encontrado."),
  USER_PASS_NOT_MATCH("Dados de login estão errados!"),
  USER_INVALID_TYPE_ENUM("Tipo de usuário inválido."),
  USER_ALREADY_EXISTS("Um usuário já existe com estes dados."),

  MEMBER_NOT_FOUND("Membro não encontrado."),
  MEMBER_ALREADY_EXISTS("Um membro já existe com estes dados."),
  MEMBER_INVALID_TYPE_ENUM("Tipo de membro inválido."),
  MEMBER_ALREADY_SPONSORED("Este membro já está patrocinado."),
  MEMBER_NOT_SPONSORED("Este membro não está patrocinado."),
  MEMBER_NOT_SUBSCRIBED("Este membro não está assinante."),
  MEMBER_NOT_ACTIVE("Este membro não está ativo."),

  SPONSOR_NOT_FOUND("Patrocinador não encontrado."),
  SPONSOR_ALREADY_EXISTS("Um patrocinador já existe com estes dados."),
  SPONSOR_INVALID_TYPE_ENUM("Tipo de patrocinador inválido."),
  SPONSOR_TIER_INVALID_ENUM("Tier de patrocinador inválido."),
  SPONSOR_NOT_ACTIVE("Este patrocinador não está ativo."),
  SPONSOR_PERSONA_REQUIRED_FOR_PERSON(
      "Persona é obrigatória quando o tipo de entidade é PERSON. Valores aceitos: POLITICIAN, INFLUENCER, ATHLETE, OTHER"),
  SPONSOR_DATA_REQUIRED("Dados do patrocinador são obrigatórios para usuários do tipo SPONSOR."),
  USER_TYPE_NOT_IMPLEMENTED("Tipo de usuário ainda não implementado."),
  MEMBER_DATA_REQUIRED("Dados do membro são obrigatórios para usuários do tipo MEMBER."),
  MEMBER_SUBSCRIBER_DATA_REQUIRED("Dados de mensalidade são obrigatórios para membros assinantes (SUBSCRIBER)."),
  MEMBER_SPONSORED_DATA_REQUIRED("Dados de patrocínio são obrigatórios para membros patrocinados (SPONSORED)."),
  MEMBER_SUBSCRIBER_EXTRA_INVALID(
      "Membros assinantes não devem enviar bloco de patrocínio (sponsored); use apenas subscriber."),
  MEMBER_SPONSORED_EXTRA_INVALID(
      "Membros patrocinados não devem enviar bloco de mensalidade (subscriber); use apenas sponsored."),
  MEMBER_SPONSORED_GRANT_INVALID(
      "O usuário concedente deve existir, ser patrocinador (SPONSOR ou SPONSOR_MEMBER) e possuir registro de patrocinador ativo."),
  MEMBER_SUBSCRIBER_NOT_FOUND("Este membro não possui cadastro de assinante."),
  MEMBER_SUBSCRIBER_UPDATE_INVALID("Não é possível atualizar mensalidade: membro não é assinante."),
  SUBSCRIBER_PAYMENT_ALREADY_REGISTERED("Pagamento deste ciclo já registrado."),
  SUBSCRIBER_PAYMENT_INACTIVE("Não é possível registrar pagamento: mensalidade inativa."),
  SUBSCRIBER_PAYMENT_CONCURRENT_UPDATE(
      "Este pagamento já está sendo processado ou o registro foi atualizado. Recarregue e tente novamente."),
  MEMBER_TYPE_FILTER_REQUIRES_USER_TYPE_MEMBER(
      "O filtro de tipo de membro (memberType) só pode ser usado quando type=MEMBER."),

  BENEFIT_NOT_FOUND("Benefício não encontrado."),

  STORAGE_NOT_CONFIGURED("Armazenamento de arquivos não está configurado."),
  STORAGE_OPERATION_FAILED("Falha ao operar no armazenamento de objetos."),
  UPLOAD_CONTENT_TYPE_NOT_ALLOWED("Tipo de arquivo não permitido. Use image/png, image/jpeg ou image/webp."),
  UPLOAD_SIZE_EXCEEDED("O arquivo excede o tamanho máximo permitido."),
  UPLOAD_OBJECT_NOT_FOUND("Objeto não encontrado no armazenamento."),
  UPLOAD_KEY_INVALID("A chave do objeto não é válida para esta entidade."),
  UPLOAD_KEY_MISMATCH("A chave informada não corresponde à imagem atual desta entidade."),
  UPLOAD_RATE_LIMIT("Limite de solicitações de upload excedido. Tente novamente em instantes.");

  public String message;

  private MessageErrorEnum(String message) {
    this.message = message;
  }

  public String getMessage() {
    return this.message;
  }
}
