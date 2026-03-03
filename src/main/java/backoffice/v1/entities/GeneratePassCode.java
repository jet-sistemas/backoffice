package backoffice.v1.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "generate_pass_codes")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GeneratePassCode {
  @Id
  @Column(nullable = false, length = 5)
  private String code;

  @Builder.Default
  @Column(columnDefinition = "boolean default true")
  private boolean isActive = true;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Column(nullable = false)
  private LocalDate expirationDate;
}
