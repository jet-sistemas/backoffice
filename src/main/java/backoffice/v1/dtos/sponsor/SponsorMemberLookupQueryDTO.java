package backoffice.v1.dtos.sponsor;

import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SponsorMemberLookupQueryDTO {

  @Parameter(description = "Código de 5 caracteres ou CPF do membro", required = true)
  @QueryParam("lookup")
  @NotBlank(message = "Identificador de membro inválido. Informe um código de 5 caracteres ou um CPF.")
  private String lookup;
}
