package backoffice.v1.dtos.user;

import backoffice.common.annotations.UnmaskNumber;
import backoffice.v1.dtos.sponsor.SponsorDataUpdateDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWithSponsorUpdateDTO {

  @Email(message = "E-mail inválido")
  private String email;

  private String name;

  @Length(min = 11, max = 14, message = "O documento deve ter entre 11 e 14 caracteres")
  @UnmaskNumber
  private String document;

  private String avatarUrl;

  @Valid
  private SponsorDataUpdateDTO sponsor;
}
