package backoffice.v1.dtos.member;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberCheckinSponsorOptionDTO {

  private Long id;

  private String publicName;

  private String logoUrl;

  @JsonProperty("active")
  private boolean active;
}
