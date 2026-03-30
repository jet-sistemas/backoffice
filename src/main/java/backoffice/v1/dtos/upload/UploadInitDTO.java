package backoffice.v1.dtos.upload;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadInitDTO {

  @NotNull(message = "O tipo de entidade é obrigatório")
  private UploadTargetEnum entity;

  @NotNull(message = "O identificador da entidade é obrigatório")
  private Long entityId;

  @Length(max = 255, message = "O nome do arquivo deve ter no máximo 255 caracteres")
  private String fileName;

  @NotBlank(message = "O content type é obrigatório")
  private String contentType;

  @NotNull(message = "O tamanho do arquivo é obrigatório")
  @Min(value = 1, message = "O tamanho do arquivo deve ser maior que zero")
  private Long size;
}
