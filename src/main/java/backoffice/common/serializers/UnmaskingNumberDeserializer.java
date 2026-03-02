package project.common.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import project.common.utils.MaskUtils;

import java.io.IOException;

public class UnmaskingNumberDeserializer extends JsonDeserializer<String> {

  @Override
  public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    String raw = p.getText();
    if (raw == null)
      return null;

    return MaskUtils.removeNumbericMask(raw);
  }
}
