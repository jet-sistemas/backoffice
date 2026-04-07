package backoffice.v1.dtos.upload;

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
public class UploadConfirmDTO {

  @NotNull(message = "O tipo de entidade é obrigatório")
  private UploadTargetEnum entity;

  @NotNull(message = "O identificador da entidade é obrigatório")
  private Long entityId;

  @NotBlank(message = "A chave do objeto é obrigatória")
  private String objectKey;
}
