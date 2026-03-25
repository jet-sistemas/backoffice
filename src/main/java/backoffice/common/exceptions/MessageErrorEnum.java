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

  BENEFIT_NOT_FOUND("Benefício não encontrado.");

  public String message;

  private MessageErrorEnum(String message) {
    this.message = message;
  }

  public String getMessage() {
    return this.message;
  }
}
