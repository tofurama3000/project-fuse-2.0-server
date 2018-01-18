package server.utility;

import static server.constants.RoleValue.INVITED_TO_INTERVIEW;
import static server.constants.RoleValue.INVITED_TO_JOIN;

import java.util.Optional;

public class RolesUtility {

  public static Optional<Integer> getRoleFromInvitationType(String type) {
    if (type == null) {
      return Optional.empty();
    }
    switch (type.toLowerCase()) {
      case "join":
        return Optional.of(INVITED_TO_JOIN);
      case "interview":
        return Optional.of(INVITED_TO_INTERVIEW);
      default:
        return Optional.empty();
    }
  }
}
