package server.utility;

import java.util.ArrayList;
import java.util.List;

public class PagingUtil {
  public static <T> List<T> getPagedResults(List<T> allResults, int page, int pageSize) {
    int startIndex = page * pageSize;
    if (allResults.size() < startIndex) {
      return new ArrayList<>();
    } else {
      int endIndex = Math.min(startIndex + pageSize, allResults.size());
      return allResults.subList(startIndex, endIndex);
    }
  }
}
