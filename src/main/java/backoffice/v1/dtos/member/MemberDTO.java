package backoffice.v1.dtos.member;

import java.time.Instant;

import backoffice.v1.entities.enums.MemberTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDTO {
  private Long id;
  private Long userId;
  private String email;
  private String code;
  private String document;
  private String fullname;
  private String whatsapp;
  private MemberTypeEnum type;
  private boolean isActive;
  private Instant createdAt;
}
