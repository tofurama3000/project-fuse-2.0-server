package server.controllers.rest.response;

import lombok.Getter;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


public class GeneralResponse {

  public enum Status {
    OK,
    ERROR,
    BAD_DATA,
    DENIED,
  }

  public GeneralResponse(HttpServletResponse response, Status status, List<String> errors, Object data){
    this.status = status;
    this.errors = errors;
    this.data = data;
    setReturnStatus(response);
  }

  public GeneralResponse(HttpServletResponse response, Status status, List<String> errors){
    this(response, status, errors, null);
  }

  public GeneralResponse(HttpServletResponse response, Status status){
    this(response, status, null);
  }

  public GeneralResponse(HttpServletResponse response){
    this(response, Status.OK);
  }

  public GeneralResponse(HttpServletResponse response, List<String> errors){
    if(errors.size() > 0) {
      this.status = Status.BAD_DATA;
      this.errors = errors;
    }
    else {
      this.status = Status.OK;
      this.errors = null;
    }
    this.data = null;
    setReturnStatus(response);
  }

  private void setReturnStatus(HttpServletResponse response){
    switch(this.status){
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

  @Getter
  private final Status status;
  @Getter
  private final List<String> errors;
  @Getter
  private final Object data;
}
