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
public class MemberCheckinHistorySponsorDTO {

  private Long id;

  private String publicName;

  private String logoUrl;

  private String tier;

  @JsonProperty("active")
  private boolean active;
}
