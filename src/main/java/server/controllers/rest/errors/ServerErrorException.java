package server.controllers.rest.errors;

public class ServerErrorException extends Throwable {
  public ServerErrorException(String message) {
    super(message);
  }
}
