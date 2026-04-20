package backoffice.v1.dtos.common;

import io.quarkus.panache.common.Page;
import lombok.Builder;
import lombok.Data;

@Data
public class PageDTO {
  private Integer page;
  private Integer size;

  private final Integer DEFAULT_PAGE = 1;
  private final Integer DEFAULT_SIZE = 10;

  public PageDTO(Integer page, Integer size) {
    this.page = (page == null || page < 1) ? 0 : page - DEFAULT_PAGE;
    this.size = (size == null || size < 1) ? DEFAULT_SIZE : size;
  }

  public Page getPagination() {
    return Page.of(getPage(), getSize());
  }

  public Integer getOneBasePage() {
    return this.page + 1;
  }

  @Builder
  public static PageDTO of(Integer page, Integer size) {
    return new PageDTO(page, size);
  }
}
