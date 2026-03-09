package backoffice.v1.dtos.user;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import backoffice.v1.entities.enums.UserTypeEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMinDTO {
  private Long id;
  private String email;
  private String name;
  private UserTypeEnum type;
  private Instant createdAt;
}
