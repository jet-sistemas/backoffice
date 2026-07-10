package backoffice.v1.dtos.member;

import com.fasterxml.jackson.annotation.JsonProperty;

import backoffice.common.annotations.Mask;
import backoffice.common.annotations.enums.MaskType;
import backoffice.v1.entities.enums.MemberTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberCardDTO {
  private Long id;
  private Long userId;
  private String name;

  @Mask(MaskType.CPF)
  private String document;

  private String code;
  private String avatarUrl;
  private MemberTypeEnum memberType;

  @JsonProperty("accountActive")
  private boolean accountActive;
}
