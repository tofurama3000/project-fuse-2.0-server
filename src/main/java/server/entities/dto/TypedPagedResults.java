package server.entities.dto;

import lombok.Data;

import java.util.List;

@Data
public class TypedPagedResults<T> {
    List<T> items;

    long totalItems;
    long start;
    long end;
    long pageSize;
}
