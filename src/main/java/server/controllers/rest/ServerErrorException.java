package server.controllers.rest;

public class ServerErrorException extends Throwable {
  public ServerErrorException(String message) {
    super(message);
  }
}
