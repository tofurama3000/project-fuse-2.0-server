package server.entities;

import server.entities.dto.User;

public interface Joinable {
  User getOwner();

  void setOwner(User owner);

  String getName();

  String getTableName();
}
