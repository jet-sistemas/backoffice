package backoffice.v1.dtos.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadConfirmResponseDTO {
  private Long id;
  private String entity;
  private Long entityId;
  private String objectKey;
  private String url;
}
