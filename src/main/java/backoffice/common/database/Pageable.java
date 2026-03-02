package backoffice.common.database;

import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Pageable<T> {
  private List<T> data;
  private Long totalElements;
  private Integer totalPages;
  private Integer pageSize;
  private Integer currentPage;

  public Pageable(PanacheQuery<T> query, Integer currentPage) {
    this.data = query.list();
    this.totalElements = query.count();
    this.totalPages = query.pageCount();
    this.pageSize = query.list().size();
    this.currentPage = currentPage;
  }
}
