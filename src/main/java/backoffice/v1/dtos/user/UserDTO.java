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
public class UserDTO {
  private Long id;
  private String email;
  private String name;
  private String document;
  private String code;
  private boolean isAccountActive;
  private UserTypeEnum type;
  private String avatarUrl;
  private Instant createdAt;
}
