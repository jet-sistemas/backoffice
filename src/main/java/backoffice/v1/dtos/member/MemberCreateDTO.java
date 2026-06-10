package backoffice.v1.dtos.member;

import backoffice.v1.dtos.user.UserCreateDTO;
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
public class MemberCreateDTO {
  @Valid
  @NotNull(message = "Os dados do usuário são obrigatórios")
  private UserCreateDTO user;

  @Valid
  @NotNull(message = "Os dados do membro são obrigatórios")
  private MemberDataCreateDTO member;
}
