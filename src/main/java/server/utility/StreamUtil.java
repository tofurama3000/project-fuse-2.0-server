package server.utility;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;


public class StreamUtil {
  public static <T> Predicate<T> uniqueByFunction(Function<? super T, ?> func) {
    Set<Object> seen = ConcurrentHashMap.newKeySet();
    return t -> seen.add(func.apply(t));
  }
}
