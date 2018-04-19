package server.entities.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class PagedResults {

  @JsonIgnore
  private List<SearchResult> searchResults;

  public List<Object> getItems() {
    return searchResults.stream().map(SearchResult::getData).collect(Collectors.toList());
  }

  private long totalItems;
  private long start;
  private long end;
  private long pageSize;
}
