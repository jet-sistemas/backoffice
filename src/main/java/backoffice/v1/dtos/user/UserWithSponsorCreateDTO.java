package backoffice.v1.dtos.user;

import backoffice.v1.dtos.sponsor.SponsorDataCreateDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWithSponsorCreateDTO {

  @Valid
  @NotNull(message = "Os dados do usuário são obrigatórios")
  private UserCreateDTO user;

  @Valid
  private SponsorDataCreateDTO sponsor;
}
