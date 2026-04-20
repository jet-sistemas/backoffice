package backoffice.v1.dtos.publics;

import java.time.Instant;

import backoffice.v1.entities.enums.UserTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicSponsorUserDTO {
  private Long id;
  private String name;
  private UserTypeEnum type;
  private Instant createdAt;
  private PublicSponsorDataDTO sponsor;
}
