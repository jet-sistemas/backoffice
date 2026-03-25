package backoffice.common.requests;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import backoffice.common.database.Pageable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_EMPTY)
@Schema(description = "Envelope padrão de todas as respostas HTTP")
public class ResponseModel<T> {
  public static final String SUCESS = "OK";
  public static final String ERROR = "ERROR";

  @Schema(description = "OK em sucesso ou ERROR em falha", enumeration = { "OK", "ERROR" })
  private String status;

  @Schema(description = "Código HTTP da operação")
  private Integer statusCode;

  @Schema(description = "Mensagem única de erro (quando aplicável)")
  private String message;

  @Schema(description = "Lista de mensagens de erro ou validação (quando aplicável)")
  private List<String> messages;

  @JsonInclude(Include.ALWAYS)
  @Schema(description = "Payload em sucesso; em erro costuma ser omitido ou null")
  private T data;

  @Schema(description = "Total de elementos (listagens paginadas)")
  private Long totalElements;

  @Schema(description = "Total de páginas (listagens paginadas)")
  private Integer totalPages;

  @Schema(description = "Tamanho da página (listagens paginadas)")
  private Integer pageSize;

  @Schema(description = "Página atual, 1-based (listagens paginadas)")
  private Integer currentPage;

  @Schema(description = "Stack trace em ambiente de desenvolvimento (erros)")
  private List<String> stackTrace;

  public static <T> ResponseModel<T> success() {
    ResponseModel<T> ret = new ResponseModel<T>();
    ret.status = ResponseModel.SUCESS;
    ret.setStatusCode(200);

    return ret;
  }

  public static <T> ResponseModel<T> success(Integer statusCode) {
    ResponseModel<T> ret = new ResponseModel<T>();
    ret.setStatus(ResponseModel.SUCESS);
    ret.setStatusCode(statusCode);
    return ret;
  }

  public static <T> ResponseModel<List<T>> success(Integer statusCode, List<T> data) {
    ResponseModel<List<T>> ret = new ResponseModel<List<T>>();

    ret.status = ResponseModel.SUCESS;
    ret.setStatusCode(statusCode);

    ret.setData(data);

    ret.setCurrentPage(1);
    ret.setTotalElements((long) data.size());
    ret.setTotalPages(1);

    return ret;
  }

  public static <T> ResponseModel<List<T>> success(Integer statusCode, Pageable<T> data) {
    ResponseModel<List<T>> ret = new ResponseModel<List<T>>();

    ret.status = ResponseModel.SUCESS;
    ret.setStatusCode(statusCode);

    ret.setData(data.getData());

    ret.setPageSize(data.getPageSize());
    ret.setTotalElements(data.getTotalElements());
    ret.setTotalPages(data.getTotalPages());
    ret.setCurrentPage(data.getCurrentPage());

    return ret;
  }

  public static <T> ResponseModel<T> success(Integer statusCode, T data) {
    ResponseModel<T> ret = new ResponseModel<T>();
    ret.setStatus(ResponseModel.SUCESS);
    ret.setStatusCode(statusCode);
    ret.setData(data);

    return ret;
  }

  public static <T> ResponseModel<T> error(String message) {
    ResponseModel<T> ret = new ResponseModel<T>();
    ret.setStatus(ResponseModel.ERROR);
    ret.setMessage(message);
    ret.setStatusCode(500);

    return ret;
  }

  public static <T> ResponseModel<T> error(List<String> messages) {
    ResponseModel<T> ret = new ResponseModel<T>();
    ret.setStatus(ResponseModel.ERROR);
    ret.setMessages(messages);
    ret.setStatusCode(500);

    return ret;
  }

  public static <T> ResponseModel<T> error(Integer statusCode, String message) {
    ResponseModel<T> ret = new ResponseModel<T>();
    ret.setStatus(ResponseModel.ERROR);
    ret.setMessage(message);
    ret.setStatusCode(statusCode);

    return ret;
  }

  public static <T> ResponseModel<T> error(Integer statusCode, List<String> messages) {
    ResponseModel<T> ret = new ResponseModel<T>();
    ret.setStatus(ResponseModel.ERROR);
    ret.setMessages(messages);
    ret.setStatusCode(statusCode);

    return ret;
  }
}
