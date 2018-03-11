package server.controllers.rest;

public class BadDataException extends Throwable {
  public BadDataException(String message) {
    super(message);
  }
}
