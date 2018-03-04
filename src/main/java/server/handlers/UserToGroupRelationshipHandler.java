package server.handlers;

import static server.constants.RoleValue.DEFAULT_USER;
import static server.constants.RoleValue.INVITED_TO_INTERVIEW;
import static server.constants.RoleValue.INVITED_TO_JOIN;
import static server.controllers.rest.response.CannedResponse.INSUFFICIENT_PRIVELAGES;
import static server.controllers.rest.response.CannedResponse.INVALID_FIELDS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.controllers.rest.response.BaseResponse;
import server.entities.PossibleError;
import server.entities.dto.group.GroupInvitation;
import server.entities.dto.user.User;
import server.entities.user_to_group.permissions.UserToGroupPermission;
import server.entities.user_to_group.permissions.results.JoinResult;
import server.entities.user_to_group.relationships.UserToGroupRelationship;
import server.utility.RolesUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class UserToGroupRelationshipHandler {

  private final InterviewHelper interviewHelper;

  @Autowired
  public UserToGroupRelationshipHandler(InterviewHelper interviewHelper) {
    this.interviewHelper = interviewHelper;
  }

  public PossibleError addRelationshipsIfNotError(GroupInvitation invitation, UserToGroupPermission permission,
                                                  UserToGroupRelationship relationship) {

    List<String> errors = new ArrayList<>();

    User user = invitation.getReceiver();
    Optional<Integer> roleFromInvitationType = RolesUtility.getRoleFromInvitationType(invitation.getType());
    if (!roleFromInvitationType.isPresent()) {
      errors.add(INVALID_FIELDS);
      return new PossibleError(errors);
    }

    switch (roleFromInvitationType.get()) {
      case INVITED_TO_INTERVIEW:
        if (interviewHelper.tryAcceptInterview(invitation, permission, relationship, errors, user))
          return new PossibleError(errors);
        break;
      case INVITED_TO_JOIN:
        if (permission.canJoin() != JoinResult.HAS_INVITE) {
          errors.add(INSUFFICIENT_PRIVELAGES);
          return new PossibleError(errors);
        }
        relationship.addRelationship(DEFAULT_USER);
        relationship.removeRelationship(INVITED_TO_JOIN);
        break;
    }

    return new PossibleError(BaseResponse.Status.OK);
  }
}
