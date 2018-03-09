package server.handlers;

import static server.constants.InvitationStatus.ACCEPTED;
import static server.constants.InvitationStatus.DECLINED;
import static server.controllers.rest.response.BaseResponse.Status.ERROR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.controllers.rest.NotificationController;
import server.controllers.rest.response.BaseResponse;
import server.controllers.rest.response.GeneralResponse;
import server.entities.PossibleError;
import server.entities.dto.Notification;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.organization.OrganizationApplicant;
import server.entities.dto.group.organization.OrganizationInvitation;
import server.entities.dto.group.project.Project;
import server.entities.dto.group.project.ProjectApplicant;
import server.entities.dto.group.project.ProjectInvitation;
import server.entities.dto.user.User;
import server.entities.user_to_group.permissions.PermissionFactory;
import server.entities.user_to_group.permissions.UserToOrganizationPermission;
import server.entities.user_to_group.permissions.UserToProjectPermission;
import server.entities.user_to_group.relationships.RelationshipFactory;
import server.entities.user_to_group.relationships.UserToOrganizationRelationship;
import server.entities.user_to_group.relationships.UserToProjectRelationship;
import server.repositories.group.InterviewRepository;
import server.repositories.group.organization.OrganizationApplicantRepository;
import server.repositories.group.organization.OrganizationInvitationRepository;
import server.repositories.group.project.ProjectApplicantRepository;
import server.repositories.group.project.ProjectInvitationRepository;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Component
public class InvitationHandler {

  private final PermissionFactory permissionFactory;
  private final InterviewRepository interviewRepository;
  private final RelationshipFactory relationshipFactory;
  private final UserToGroupRelationshipHandler userToGroupRelationshipHandler;
  private final ProjectApplicantRepository projectApplicantRepository;
  private final ProjectInvitationRepository projectInvitationRepository;
  private final OrganizationApplicantRepository organizationApplicantRepository;
  private final OrganizationInvitationRepository organizationInvitationRepository;
  private final NotificationController notificationController;

  private final Logger logger = LoggerFactory.getLogger(InvitationHandler.class);

  @Autowired
  public InvitationHandler(PermissionFactory permissionFactory, InterviewRepository interviewRepository,
                           RelationshipFactory relationshipFactory, UserToGroupRelationshipHandler userToGroupRelationshipHandler,
                           ProjectApplicantRepository projectApplicantRepository, ProjectInvitationRepository projectInvitationRepository,
                           OrganizationApplicantRepository organizationApplicantRepository, OrganizationInvitationRepository organizationInvitationRepository,
                           NotificationController notificationController) {

    this.permissionFactory = permissionFactory;
    this.interviewRepository = interviewRepository;
    this.relationshipFactory = relationshipFactory;
    this.userToGroupRelationshipHandler = userToGroupRelationshipHandler;
    this.projectApplicantRepository = projectApplicantRepository;
    this.projectInvitationRepository = projectInvitationRepository;
    this.organizationApplicantRepository = organizationApplicantRepository;
    this.organizationInvitationRepository = organizationInvitationRepository;
    this.notificationController = notificationController;
  }

  public BaseResponse acceptProjectInvitation(ProjectInvitation projectInvitation, HttpServletResponse response,
                                              ProjectInvitation savedInvitation, User user, Project group) {
    UserToProjectPermission permission = permissionFactory.createUserToProjectPermission(user, group);


    if (projectInvitation.getInterview() != null) {
      savedInvitation.setInterview(interviewRepository.findOne(projectInvitation.getInterview().getId()));
    } else {
      savedInvitation.setInterview(null);
    }

    UserToProjectRelationship userToTeamRelationship = relationshipFactory.createUserToProjectRelationship(user, group);
    PossibleError possibleError = userToGroupRelationshipHandler.addRelationshipsIfNotError(savedInvitation, permission, userToTeamRelationship);

    if (!possibleError.hasError()) {
      savedInvitation.setStatus(ACCEPTED);
      ProjectApplicant applicant = projectApplicantRepository.findOne(savedInvitation.getApplicant().getId());
      if (savedInvitation.getType().equals("join")) {
        applicant.setStatus("accepted");
        notificationController.markAsDoneForApplicant(applicant);
        notificationController.markInvitationsAsDoneFor(savedInvitation);
      } else {
        applicant.setStatus("interview_scheduled");
        applicant.setInterview(interviewRepository.findOne(savedInvitation.getInterview().getId()));
      }
      projectApplicantRepository.save(applicant);
      projectInvitationRepository.save(savedInvitation);

      try {
        notificationController.sendUserJoinedNotification(user, group);
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
      }
    }
    return new GeneralResponse(response, possibleError.getStatus(), possibleError.getErrors());
  }

  public BaseResponse declineProjectInvitation(HttpServletResponse response, ProjectInvitation savedInvitation, User user, Project group) {
    savedInvitation.setStatus(DECLINED);
    if (savedInvitation.getType().equals("join")) {
      ProjectApplicant applicant = projectApplicantRepository.findOne(savedInvitation.getApplicant().getId());
      applicant.setStatus("declined");
      notificationController.markAsDoneForApplicant(applicant);
      notificationController.markInvitationsAsDoneFor(savedInvitation);
      projectApplicantRepository.save(applicant);
    }

    projectInvitationRepository.save(savedInvitation);
    try {
      notificationController.sendUserDeclinedJoinInvite(user, group);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    notificationController.markInvitationsAsDoneFor(savedInvitation);

    return new GeneralResponse(response);
  }

  public BaseResponse acceptOrganizationInvitation(OrganizationInvitation organizationInvitation, HttpServletResponse response,
                                                   OrganizationInvitation savedInvitation, User user, Organization group) {
    UserToOrganizationPermission permission = permissionFactory.createUserToOrganizationPermission(user, group);

    UserToOrganizationRelationship userToTeamRelationship = relationshipFactory.createUserToOrganizationRelationship(user, group);

    savedInvitation.setInterview(organizationInvitation.getInterview());
    PossibleError possibleError = userToGroupRelationshipHandler.addRelationshipsIfNotError(savedInvitation, permission, userToTeamRelationship);

    if (!possibleError.hasError()) {
      savedInvitation.setStatus(ACCEPTED);
      OrganizationApplicant applicant = organizationApplicantRepository.findOne(savedInvitation.getApplicant().getId());
      if (savedInvitation.getType().equals("join")) {
        applicant.setStatus("accepted");
        notificationController.markAsDoneForApplicant(applicant);
        notificationController.markInvitationsAsDoneFor(savedInvitation);
      } else {
        applicant.setStatus("interview_scheduled");
        applicant.setInterview(interviewRepository.findOne(savedInvitation.getInterview().getId()));
      }
      organizationApplicantRepository.save(applicant);
      organizationInvitationRepository.save(savedInvitation);
      notificationController.sendUserJoinedNotification(user, group);
    }

    return new GeneralResponse(response, possibleError.getStatus(), possibleError.getErrors());
  }

  public BaseResponse declineOrganizationInvitation(HttpServletResponse response, List<String> errors, OrganizationInvitation savedInvitation, User user, Organization group) {
    savedInvitation.setStatus(DECLINED);
    if (savedInvitation.getType().equals("join")) {
      OrganizationApplicant applicant = organizationApplicantRepository.findOne(savedInvitation.getApplicant().getId());
      applicant.setStatus("declined");
      notificationController.markAsDoneForApplicant(applicant);
      notificationController.markInvitationsAsDoneFor(savedInvitation);
      organizationApplicantRepository.save(applicant);
    }
    organizationInvitationRepository.save(savedInvitation);
    notificationController.sendUserDeclinedJoinInvite(user, group);

    return new GeneralResponse(response);
  }

}
