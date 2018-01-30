package server.entities.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by TR on 1/27/2018.
 */
@Data
public class TypedPagedResults<T> {
    Iterable<T> items;

    long totalItems;
    long start;
    long end;
    long pageSize;
}
