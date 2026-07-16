package backoffice.v1.dtos.sponsor;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SponsorCheckinCreateDTO {

  @NotBlank(message = "Identificador de membro inválido. Informe um código de 5 caracteres ou um CPF.")
  private String lookup;

  @Builder.Default
  private Boolean confirmDuplicateToday = false;
}
