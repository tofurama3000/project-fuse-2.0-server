package server.handlers;

import static server.constants.Availability.AVAILABLE;
import static server.constants.Availability.NOT_AVAILABLE;
import static server.constants.RoleValue.INVITED_TO_INTERVIEW;
import static server.constants.RoleValue.TO_INTERVIEW;
import static server.controllers.rest.response.CannedResponse.INSUFFICIENT_PRIVELAGES;
import static server.controllers.rest.response.CannedResponse.INVALID_FIELDS;
import static server.controllers.rest.response.CannedResponse.NO_INTERVIEW_FOUND;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.entities.dto.group.Group;
import server.entities.dto.group.GroupInvitation;
import server.entities.dto.group.interview.Interview;
import server.entities.dto.user.User;
import server.entities.user_to_group.permissions.UserToGroupPermission;
import server.entities.user_to_group.relationships.UserToGroupRelationship;
import server.repositories.group.InterviewRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@Component
public class InterviewHelper {

  private final InterviewRepository interviewRepository;

  @Autowired
  public InterviewHelper(InterviewRepository interviewRepository) {
    this.interviewRepository = interviewRepository;
  }

  public boolean tryAcceptInterview(GroupInvitation invitation, UserToGroupPermission permission, UserToGroupRelationship relationship, List<String> errors, User user) {
    Interview interview = invitation.getInterview();
    if (interview == null) {
      errors.add(INVALID_FIELDS);
      return true;
    }
    Group group = invitation.getGroup();
    LocalDateTime currentDateTime = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime();

    List<Interview> availableInterviewsAfterDate = interviewRepository
        .getAvailableInterviewsAfterDate(group.getId(), group.getGroupType(), currentDateTime);

    long count = availableInterviewsAfterDate.stream().map(Interview::getId)
        .filter(id -> Objects.equals(id, interview.getId())).count();

    if (count < 1) {
      errors.add(NO_INTERVIEW_FOUND);
      return true;
    }

    if (!permission.hasRole(INVITED_TO_INTERVIEW)) {
      errors.add(INSUFFICIENT_PRIVELAGES);
      return true;
    }

    relationship.addRelationship(TO_INTERVIEW);
    relationship.removeRelationship(INVITED_TO_INTERVIEW);

    interview.setUser(user);
    interview.setAvailability(NOT_AVAILABLE);
    interviewRepository.save(interview);
    return false;
  }

  public void saveNewInterviewsForGroup(List<Interview> interviews, Group group) {
    for (Interview interview : interviews) {
      interview.setGroupType(group.getGroupType());
      interview.setAvailability(AVAILABLE);
      interview.setGroupId(group.getId());
    }

    interviewRepository.save(interviews);
  }

  public void setInterviewsAvailableAndSave(List<Interview> interviews) {
    for (Interview interview : interviews) {
      interview.setUser(null);
      interview.setAvailability(AVAILABLE);
    }
    interviewRepository.save(interviews);
  }

}
