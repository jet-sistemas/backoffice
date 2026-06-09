package backoffice.v1.dtos.member;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SponsoredDataCreateDTO {
  @NotNull(message = "O usuário patrocinador concedente é obrigatório.")
  private Long grantedByUserId;

  @NotNull(message = "A data de início do patrocínio é obrigatória.")
  private LocalDate startAt;

  private LocalDate endAt;

  private String reason;
}
