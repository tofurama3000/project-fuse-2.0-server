package server.controllers.rest.response;

import lombok.Data;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Data
public class GeneralResponse {

  public enum Status {
    OK,
    ERROR,
    BAD_DATA,
    DENIED,
  }

  private Status status;
  private List<String> errors;
  private Object data;


  public GeneralResponse(HttpServletResponse response, Status status, List<String> errors, Object data) {
    this.status = status;
    this.errors = errors;
    this.data = data;
    setReturnStatus(response);
  }

  public GeneralResponse(HttpServletResponse response, Status status, List<String> errors) {
    this(response, status, errors, null);
  }

  public GeneralResponse(HttpServletResponse response, Status status, String error) {
    this.status = status;
    this.errors = new ArrayList<>();
    errors.add(error);
    setReturnStatus(response);
  }

  public GeneralResponse(HttpServletResponse response, Status status) {
    this.status = status;
    setReturnStatus(response);
  }

  public GeneralResponse(HttpServletResponse response) {
    this(response, Status.OK);
  }

  public GeneralResponse(HttpServletResponse response, List<String> errors) {
    if (errors.size() > 0) {
      this.status = Status.BAD_DATA;
      this.errors = errors;
    } else {
      this.status = Status.OK;
      this.errors = null;
    }
    this.data = null;
    setReturnStatus(response);
  }

  public GeneralResponse() {
    // Default constructor for serialization
  }

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


}
