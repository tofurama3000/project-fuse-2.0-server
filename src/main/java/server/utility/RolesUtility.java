package server.utility;

import static server.constants.RoleValue.INVITED_TO_INTERVIEW;
import static server.constants.RoleValue.INVITED_TO_JOIN;

import java.util.Optional;

public class RolesUtility {

  public static Optional<Integer> getRoleFromInvitationType(String type) {
    switch (type) {
      case "Join":
        return Optional.of(INVITED_TO_JOIN);
      case "Interview":
        return Optional.of(INVITED_TO_INTERVIEW);
      default:
        return Optional.empty();
    }
  }
}
