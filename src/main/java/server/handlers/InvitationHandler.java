package server.handlers;

import static server.constants.InvitationStatus.ACCEPTED;
import static server.constants.InvitationStatus.DECLINED;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.controllers.rest.NotificationController;
import server.controllers.rest.response.BaseResponse;
import server.controllers.rest.response.GeneralResponse;
import server.entities.PossibleError;
import server.entities.dto.group.interview.Interview;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.organization.OrganizationApplication;
import server.entities.dto.group.organization.OrganizationInvitation;
import server.entities.dto.group.project.Project;
import server.entities.dto.group.project.ProjectApplication;
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

    UserToProjectRelationship userToGroupRelationship = relationshipFactory.createUserToProjectRelationship(user, group);
    PossibleError possibleError = userToGroupRelationshipHandler.addRelationshipsIfNotError(savedInvitation, permission, userToGroupRelationship);

    if (!possibleError.hasError()) {
      savedInvitation.setStatus(ACCEPTED);
      ProjectApplication applicant = projectApplicantRepository.findOne(savedInvitation.getApplicant().getId());
      if (savedInvitation.getType().equals("join")) {
        applicant.setStatus("accepted");
        notificationController.markAsDoneForApplicant(applicant);
        notificationController.markInvitationsAsDoneFor(applicant);
        notificationController.sendUserJoinedNotification(user, group);

      } else {
        applicant.setStatus("interview_scheduled");
        Interview interview = interviewRepository.findOne(savedInvitation.getInterview().getId());
        applicant.setInterview(interview);
        notificationController.sendUserAcceptedInterviewNotification(user, group, interview);
      }
      projectApplicantRepository.save(applicant);
      projectInvitationRepository.save(savedInvitation);

    }
    return new GeneralResponse(response, possibleError.getStatus(), possibleError.getErrors());
  }

  public BaseResponse declineProjectInvitation(HttpServletResponse response, ProjectInvitation savedInvitation, User user, Project group) {
    savedInvitation.setStatus(DECLINED);
    if (savedInvitation.getApplicant() != null) {
      ProjectApplication applicant = projectApplicantRepository.findOne(savedInvitation.getApplicant().getId());
      applicant.setStatus("declined");
      notificationController.markAsDoneForApplicant(applicant);
      notificationController.markInvitationsAsDoneFor(applicant);
      projectApplicantRepository.save(applicant);
    }

    projectInvitationRepository.save(savedInvitation);
    try {
      notificationController.sendUserDeclinedJoinInvite(user, group);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    notificationController.markInvitationNotificationsAsDone(savedInvitation);

    return new GeneralResponse(response);
  }

  public BaseResponse acceptOrganizationInvitation(OrganizationInvitation organizationInvitation, HttpServletResponse response,
                                                   OrganizationInvitation savedInvitation, User user, Organization group) {
    UserToOrganizationPermission permission = permissionFactory.createUserToOrganizationPermission(user, group);

    if (organizationInvitation.getInterview() != null) {
      savedInvitation.setInterview(interviewRepository.findOne(organizationInvitation.getInterview().getId()));
    } else {
      savedInvitation.setInterview(null);
    }

    UserToOrganizationRelationship userToGroupRelationship = relationshipFactory.createUserToOrganizationRelationship(user, group);
    PossibleError possibleError = userToGroupRelationshipHandler.addRelationshipsIfNotError(savedInvitation, permission, userToGroupRelationship);

    if (!possibleError.hasError()) {
      savedInvitation.setStatus(ACCEPTED);
      OrganizationApplication applicant = organizationApplicantRepository.findOne(savedInvitation.getApplicant().getId());
      if (savedInvitation.getType().equals("join")) {
        applicant.setStatus("accepted");
        notificationController.markAsDoneForApplicant(applicant);
        notificationController.markInvitationsAsDoneFor(applicant);
        notificationController.sendUserJoinedNotification(user, group);
      } else {
        applicant.setStatus("interview_scheduled");
        Interview interview = interviewRepository.findOne(savedInvitation.getInterview().getId());
        applicant.setInterview(interview);
        notificationController.sendUserAcceptedInterviewNotification(user, group, interview);
      }
      organizationApplicantRepository.save(applicant);
      organizationInvitationRepository.save(savedInvitation);
    }
    return new GeneralResponse(response, possibleError.getStatus(), possibleError.getErrors());
  }

  public BaseResponse declineOrganizationInvitation(HttpServletResponse response, OrganizationInvitation savedInvitation, User user, Organization group) {
    savedInvitation.setStatus(DECLINED);
    if (savedInvitation.getApplicant() != null) {
      OrganizationApplication applicant = organizationApplicantRepository.findOne(savedInvitation.getApplicant().getId());
      applicant.setStatus("declined");
      notificationController.markAsDoneForApplicant(applicant);
      notificationController.markInvitationsAsDoneFor(applicant);
      organizationApplicantRepository.save(applicant);
    }

    organizationInvitationRepository.save(savedInvitation);
    try {
      notificationController.sendUserDeclinedJoinInvite(user, group);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    notificationController.markInvitationNotificationsAsDone(savedInvitation);

    return new GeneralResponse(response);
  }

}
