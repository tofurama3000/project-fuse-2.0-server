package server.controllers.rest.response;

import lombok.Data;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

@Data
public class GeneralResponse extends BaseResponse {

  private Object data;


  public GeneralResponse(HttpServletResponse response, Status status, List<String> errors, Object data) {
    super(response, status, errors);
    this.data = data;
  }

  public GeneralResponse(HttpServletResponse response, Status status, List<String> errors) {
    this(response, status, errors, null);
  }

  public GeneralResponse(HttpServletResponse response, Status status, String error) {
    this(response, status, Collections.singletonList(error), null);
  }

  public GeneralResponse(HttpServletResponse response, Status status) {
    this(response, status, null, null);
  }

  public GeneralResponse(HttpServletResponse response) {
    this(response, Status.OK);
  }

  public GeneralResponse(HttpServletResponse response, List<String> errors) {
    this(response, statusFromError(errors), errors, null);
  }

  public GeneralResponse() {}
}
