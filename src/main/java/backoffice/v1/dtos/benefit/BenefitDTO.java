package backoffice.v1.dtos.benefit;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import backoffice.v1.dtos.sponsor.SponsorMinDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BenefitDTO {
  private Long id;
  private String name;
  private String description;
  private String address;
  private boolean isActive;

  @JsonInclude(Include.NON_NULL)
  private SponsorMinDTO sponsor;

  private Instant createdAt;
}
