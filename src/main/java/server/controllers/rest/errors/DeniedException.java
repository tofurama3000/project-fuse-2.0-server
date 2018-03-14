package server.controllers.rest.errors;

public class DeniedException extends Throwable {
  public DeniedException(String message) {
    super(message);
  }
}
