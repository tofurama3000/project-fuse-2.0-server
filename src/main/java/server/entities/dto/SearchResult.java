package server.entities.dto;

import lombok.Getter;

import java.util.Map;

public class SearchResult {

  @Getter
  private final Map<String, Object> data;

  public SearchResult(Map<String, Object> data) {
    this.data = data;
  }
}
