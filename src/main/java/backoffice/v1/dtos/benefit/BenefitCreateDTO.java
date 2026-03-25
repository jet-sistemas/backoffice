package backoffice.v1.dtos.benefit;

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

  private String description;

  private String address;

  private Long sponsorId;
}
