package backoffice.v1.dtos.member;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

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

  @JsonProperty("active")
  private boolean isActive;

  private Instant createdAt;

  @JsonInclude(Include.NON_NULL)
  private SubscriberMemberDTO subscriber;

  @JsonInclude(Include.NON_NULL)
  private SponsoredMemberDTO sponsored;
}
