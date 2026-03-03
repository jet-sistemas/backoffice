package backoffice.v1.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "benefits")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class Benefit extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "sponsor_id")
  private Sponsor sponsor;

  @Column(nullable = false)
  private String name;

  private String description;

  private String address;

  @Builder.Default
  @Column(columnDefinition = "boolean default true")
  private boolean isActive = true;
}
