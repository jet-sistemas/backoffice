package backoffice.v1.dtos.benefit;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BenefitCreateDTO {

  @NotBlank(message = "O nome do benefício é obrigatório")
  private String name;

  @Length(max = 255, message = "A descrição do benefício deve ter no máximo 255 caracteres")
  private String description;

  @Length(max = 255, message = "O endereço do benefício deve ter no máximo 255 caracteres")
  private String address;

  private Long sponsorId;
}
