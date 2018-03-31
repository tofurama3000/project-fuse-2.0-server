package server.handlers;

import static server.constants.RoleValue.ADMIN;
import static server.controllers.rest.response.CannedResponse.INSUFFICIENT_PRIVELAGES;
import static server.controllers.rest.response.CannedResponse.NO_GROUP_FOUND;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.controllers.rest.errors.BadDataException;
import server.controllers.rest.errors.DeniedException;
import server.entities.dto.group.interview.Interview;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.project.Project;
import server.entities.dto.group.project.ProjectInterviewSlots;
import server.entities.dto.user.User;
import server.entities.dto.user.UserInterviewSlots;
import server.entities.user_to_group.permissions.PermissionFactory;
import server.entities.user_to_group.permissions.UserToOrganizationPermission;
import server.repositories.group.InterviewRepository;
import server.repositories.group.organization.OrganizationRepository;
import server.repositories.group.project.ProjectRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class InterviewSlotsHelper {

  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private InterviewRepository interviewRepository;

  @Autowired
  private PermissionFactory permissionFactory;

  public List<ProjectInterviewSlots> getInterviewSlotsForAllProjectsInOrganization
      (Long organizationId, User loggedInUser, LocalDateTime start, LocalDateTime end) throws BadDataException, DeniedException {
    Organization organization = Optional.ofNullable(organizationRepository.findOne(organizationId))
        .orElseThrow(() -> new BadDataException(NO_GROUP_FOUND));

    UserToOrganizationPermission userToOrganizationPermission = permissionFactory.createUserToOrganizationPermission(loggedInUser, organization);
    if (!userToOrganizationPermission.hasRole(ADMIN)) {
      throw new DeniedException(INSUFFICIENT_PRIVELAGES);
    }

    List<Project> groupsInOrganization = projectRepository.getGroupsInOrganization(organization);

    return groupsInOrganization.stream().map(project -> getProjectsInterviewSlots(project, start, end))
        .collect(Collectors.toList());
  }

  public List<UserInterviewSlots> getInterviewSlotsForAllMembersInOrganization
      (Long organizationId, User loggedInUser, LocalDateTime start, LocalDateTime end) throws BadDataException, DeniedException {
    List<ProjectInterviewSlots> projectInterviewSlots = getInterviewSlotsForAllProjectsInOrganization(organizationId, loggedInUser, start, end);

    return projectInterviewSlots.stream().flatMap(p -> p.getInterviews().stream().filter(interview -> interview.getUser() != null))
        .collect(Collectors.groupingBy(Interview::getUser))
        .entrySet().stream()
        .map(entry -> new UserInterviewSlots(entry.getKey(), entry.getValue()))
        .sorted(Comparator.comparing(e -> e.getUser().getName())).collect(Collectors.toList());
  }

  private ProjectInterviewSlots getProjectsInterviewSlots(Project project, LocalDateTime startTime, LocalDateTime endTime) {
    List<Interview> interviews = interviewRepository.getAllInterviewsBetweenDates(project.getId(), project.getGroupType(), startTime, endTime);
    return new ProjectInterviewSlots(project, interviews);
  }
}
