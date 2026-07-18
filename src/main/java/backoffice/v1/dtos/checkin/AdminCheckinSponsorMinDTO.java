package backoffice.v1.dtos.checkin;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCheckinSponsorMinDTO {

  private Long id;

  private String publicName;

  private String tier;

  @JsonProperty("active")
  private boolean active;
}
