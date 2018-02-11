package server.controllers.rest.response;

import lombok.Data;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Data
public abstract class BaseResponse {

  public enum Status {
    OK,
    ERROR,
    BAD_DATA,
    DENIED,
  }

  public BaseResponse(HttpServletResponse response, Status status, List<String> errors) {
    this.status = status;
    this.errors = errors;
    setReturnStatus(response);
  }

  public BaseResponse() {
  }

  protected Status status;
  protected List<String> errors;


  private void setReturnStatus(HttpServletResponse response) {
    switch (this.status) {
      case OK:
        response.setStatus(HttpServletResponse.SC_OK);
        break;
      case ERROR:
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        break;
      case BAD_DATA:
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        break;
      case DENIED:
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        break;
      default:
        response.setStatus(201);
    }
  }

  protected static Status statusFromError(List<String> errors) {
    if (errors == null || errors.size() == 0) {
      return Status.OK;
    }
    return Status.BAD_DATA;
  }
}
