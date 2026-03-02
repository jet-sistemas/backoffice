package project.common.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import project.common.annotations.Mask;
import project.common.annotations.enums.MaskType;

import java.io.IOException;

public class MaskingSerializer extends JsonSerializer<String> {

  @Override
  public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    Mask annotation = null;
    try {
      annotation = gen.currentValue().getClass()
          .getDeclaredField(gen.getOutputContext().getCurrentName())
          .getAnnotation(Mask.class);
    } catch (NoSuchFieldException | SecurityException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    String masked = value;
    if (annotation != null) {
      MaskType type = annotation.value();
      masked = applyMask(value, type);
    }

    gen.writeString(masked);
  }

  private String applyMask(String raw, MaskType type) {
    if (raw == null || raw.isEmpty())
      return raw;

    switch (type) {
      case CPF:
        return raw.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
      case CNPJ:
        return raw.replaceFirst("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
      case PHONE:
        return /* raw.length() == 11 */
        raw.replaceFirst("(\\d{2})(\\d{5})(\\d{4})", "($1) $2-$3");
      // : raw.replaceFirst("(\\d{2})(\\d{4})(\\d{4})", "($1) $2-$3");
      default:
        return raw;
    }
  }
}
