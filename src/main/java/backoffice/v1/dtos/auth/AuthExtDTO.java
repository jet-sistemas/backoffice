package backoffice.v1.dtos.auth;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import backoffice.v1.entities.enums.UserTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_EMPTY)
public class AuthExtDTO {
  private Long id;
  private String email;
  private String name;
  private String avatarUrl;

  private UserTypeEnum type;

  private Instant createdAt;
  private Boolean isAccountActive;
}
