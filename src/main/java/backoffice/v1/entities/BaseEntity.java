package backoffice.v1.entities;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;

@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class BaseEntity extends PanacheEntityBase {
  @Id
  @GeneratedValue
  private Long id;

  @CreationTimestamp
  @Column(columnDefinition = "timestamp")
  protected Instant createdAt;

  @UpdateTimestamp
  @Column(columnDefinition = "timestamp")
  protected Instant updatedAt;
}
