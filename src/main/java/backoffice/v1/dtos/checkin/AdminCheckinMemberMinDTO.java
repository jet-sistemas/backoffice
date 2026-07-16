package backoffice.v1.dtos.checkin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCheckinMemberMinDTO {

  private Long id;

  private Long userId;

  private String name;

  private String code;

  private String documentMasked;
}
