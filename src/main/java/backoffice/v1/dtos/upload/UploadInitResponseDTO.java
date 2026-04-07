package backoffice.v1.dtos.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadInitResponseDTO {
  private String objectKey;
  private String uploadUrl;
  private String publicUrl;
  private int expiresIn;
}
