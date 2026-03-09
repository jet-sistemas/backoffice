package backoffice.v1.dtos.user;

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
public class UserExtDTO {
  private Long id;
  private String email;
  private String name;
  private String avatarUrl;

  private UserTypeEnum type;

  private Instant createdAt;
  private Instant updatedAt;

  private Boolean isAccountActive;
}
