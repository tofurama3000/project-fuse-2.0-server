package server.controllers.rest.errors;

public class BadDataException extends Throwable {
  public BadDataException(String message) {
    super(message);
  }
}
