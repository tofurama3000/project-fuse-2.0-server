package server.controllers.rest.response;

import lombok.Data;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class TypedResponse<T> extends BaseResponse {
  private T data;


  public TypedResponse(HttpServletResponse response, Status status, List<String> errors, T data) {
    super(response, status, errors);
    this.data = data;
  }

  public TypedResponse(HttpServletResponse response, Status status, List<String> errors) {
    this(response, status, errors, null);
  }

  public TypedResponse(HttpServletResponse response, Status status, String error) {
    this(response, status, Collections.singletonList(error), null);
  }

  public TypedResponse(HttpServletResponse response, T data) {
    this(response, Status.OK, null, data);
  }

  public TypedResponse(HttpServletResponse response, Status status) {
    this(response, status, null, null);
  }

  public TypedResponse(HttpServletResponse response) {
    this(response, Status.OK);
  }

  public TypedResponse(HttpServletResponse response, List<String> errors) {
    this(response, statusFromError(errors), errors, null);
  }

  public TypedResponse() {}
}
