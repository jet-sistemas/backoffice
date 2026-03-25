package backoffice.v1.dtos.benefit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BenefitUpdateDTO {

  private String name;

  private String description;

  private String address;

  private Long sponsorId;
}
