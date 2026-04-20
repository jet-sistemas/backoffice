package backoffice.v1.dtos.upload;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum UploadTargetEnum {
  @JsonProperty("user")
  USER,
  @JsonProperty("sponsor")
  SPONSOR
}
