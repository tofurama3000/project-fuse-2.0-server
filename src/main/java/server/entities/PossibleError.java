package server.entities;

import static server.controllers.rest.response.BaseResponse.Status.BAD_DATA;
import static server.controllers.rest.response.BaseResponse.Status.OK;

import lombok.Getter;
import server.controllers.rest.response.BaseResponse.Status;

import java.util.ArrayList;
import java.util.List;

public class PossibleError {

  private final boolean hasError;

  @Getter
  private final List<String> errors;

  @Getter
  private final Status status;

  public PossibleError(List<String> errors) {
    this.errors = errors;

    if (errors.size() > 0) {
      hasError = true;
      status = BAD_DATA;
    } else {
      hasError = false;
      status = OK;
    }
  }

  public PossibleError(List<String> errors, Status status) {
    this.errors = errors;
    this.status = status;

    hasError = this.status != OK;
  }

  public PossibleError(String error, Status status) {
    this.errors = new ArrayList<>();
    errors.add(error);
    this.status = status;

    hasError = this.status != OK;
  }

  public PossibleError(Status status) {
    this.errors = new ArrayList<>();
    this.status = status;

    hasError = this.status != OK;
  }

  public boolean hasError() {
    return hasError;
  }
}
