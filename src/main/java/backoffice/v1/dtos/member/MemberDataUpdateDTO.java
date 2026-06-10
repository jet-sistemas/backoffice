package backoffice.v1.dtos.member;

import backoffice.common.annotations.UnmaskNumber;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDataUpdateDTO {

  @Size(min = 1, max = 500, message = "O nome completo do associado é inválido.")
  private String fullname;

  @Length(min = 10, max = 11, message = "WhatsApp inválido (DDD + número).")
  @UnmaskNumber
  private String whatsapp;
}
