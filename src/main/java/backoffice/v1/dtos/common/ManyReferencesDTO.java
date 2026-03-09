package backoffice.v1.dtos.common;

import java.util.List;

import io.smallrye.common.constraint.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ManyReferencesDTO {
  @NotNull
  @Size(min = 1, max = 20, message = "A quantidade mínima e máxima corresponde entre 1 a 20 itens.")
  private List<Long> data;
}
