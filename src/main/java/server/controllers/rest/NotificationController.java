package server.controllers.rest;

import static server.constants.RoleValue.ADMIN;
import static server.constants.RoleValue.OWNER;
import static server.controllers.rest.response.BaseResponse.Status.OK;
import static server.controllers.rest.response.CannedResponse.INSUFFICIENT_PRIVELAGES;
import static server.controllers.rest.response.CannedResponse.INVALID_FIELDS;
import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import server.controllers.FuseSessionController;
import server.controllers.rest.response.BaseResponse;
import server.controllers.rest.response.GeneralResponse;
import server.controllers.rest.response.TypedResponse;
import server.entities.dto.FuseSession;
import server.entities.dto.Notification;
import server.entities.dto.Notification.NotificationEntity;
import server.entities.dto.Notification.NotificationStatus;
import server.entities.dto.Notification.NotificationType;
import server.entities.dto.group.Group;
import server.entities.dto.group.GroupApplication;
import server.entities.dto.group.GroupInvitation;
import server.entities.dto.group.interview.Interview;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.project.Project;
import server.entities.dto.user.Friendship;
import server.entities.dto.user.User;
import server.repositories.FriendRepository;
import server.repositories.NotificationRepository;
import server.repositories.group.organization.OrganizationApplicantRepository;
import server.repositories.group.organization.OrganizationInvitationRepository;
import server.repositories.group.organization.OrganizationMemberRepository;
import server.repositories.group.organization.OrganizationRepository;
import server.repositories.group.project.ProjectApplicantRepository;
import server.repositories.group.project.ProjectInvitationRepository;
import server.repositories.group.project.ProjectMemberRepository;
import server.repositories.group.project.ProjectRepository;
import server.utility.NotificationEntityNames;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/notifications")
@Api(tags = "notification")
public class NotificationController {

  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private FuseSessionController fuseSessionController;

  @Autowired
  private NotificationRepository notificationRepository;

  @Autowired
  private ProjectMemberRepository projectMemberRepository;

  @Autowired
  private OrganizationMemberRepository organizationMemberRepository;

  @Autowired
  private FriendRepository friendRepository;

  @Autowired
  private OrganizationInvitationRepository organizationInvitationRepository;

  @Autowired
  private ProjectInvitationRepository projectInvitationRepository;

  @Autowired
  private ProjectApplicantRepository projectApplicantRepository;

  @Autowired
  private OrganizationApplicantRepository organizationApplicantRepository;

  public void sendFriendshipRequestNotification(Friendship friendship) {
    User reciever = friendship.getReceiver();
    User sender = friendship.getSender();
    String msg = sender.getName() + " has sent you a friend request!";
    sendNotification(
        reciever,
        msg,
        NotificationEntity.FRIEND,
        NotificationType.FRIEND_REQUEST,
        NotificationStatus.PENDING_INVITE,
        friendship.getId()
    );
  }

  public void sendFriendshipAcceptedNotification(Friendship friendship) {
    User reciever = friendship.getReceiver();
    User sender = friendship.getSender();
    String msg = reciever.getName() + " has accepted your friend request!";
    sendNotification(
        sender,
        msg,
        NotificationEntity.FRIEND,
        NotificationType.FRIEND_REQUEST,
        NotificationStatus.ACCEPTED_INVITE,
        friendship.getId()
    );
  }

  public void updateFriendshipRequestNotificationsFor(Friendship friendship) {
    NotificationEntityNames notificationInfo = Notification.getNotificationEntities(
        NotificationEntity.FRIEND,
        NotificationType.FRIEND_REQUEST,
        NotificationStatus.PENDING_INVITE
    );
    notificationRepository.getNotificationsByDataAndType(
        friendship.getId(),
        notificationInfo.dataType,
        notificationInfo.notificationType
    ).forEach(this::markNotificationDone);
  }

  public void sendApplicationRejectedNotification(GroupApplication application) {
    User applicant = application.getSender();
    Group group = application.getGroup();
    String msg = "Your application to " + group.getName() + " was declined.";
    sendNotification(
        applicant,
        msg,
        getNotificationEntityType(group),
        NotificationType.APPLICATION,
        NotificationStatus.DECLINED_INVITE,
        application.getId()
    );
  }

  public <T extends Group, I extends GroupInvitation<T>> void sendInterviewInvitation(I interviewInvitation) {
    GroupApplication application = interviewInvitation.getApplicant();
    User applicant = application.getSender();
    Group group = application.getGroup();

    String msg = "You have been invited to interview with " + group.getName();
    sendNotification(
        applicant,
        msg,
        getNotificationEntityType(group),
        NotificationType.INTERVIEW_INVITATION,
        NotificationStatus.PENDING_INVITE,
        interviewInvitation.getId()
    );
  }

  public <T extends Group, I extends GroupInvitation<T>> void sendJoinInvitationNotification(I groupInvitation) {
    User receiver = groupInvitation.getReceiver();
    T group = groupInvitation.getGroup();
    String msg = "You have been invited to join " + group.getName() + "!";
    sendNotification(
        receiver,
        msg,
        getNotificationEntityType(group),
        NotificationType.JOIN_INVITATION,
        NotificationStatus.PENDING_INVITE,
        groupInvitation.getId()
    );
  }

  public <T extends Group> void sendUserJoinedNotification(User user, T group) {
    String msg = user.getName() + " has joined the " + group.getGroupType().toLowerCase() + " " + group.getName();
    sendGroupNotificationToAdmins(
        group,
        msg,
        getNotificationEntityType(group),
        NotificationType.JOINED,
        NotificationStatus.INFO,
        group.getId()
    );
  }

  public <T extends Group> void sendUserAcceptedInterviewNotification(User user, T group, Interview interview) {
    String msg = user.getName() + " has accepted invitation to interview for  " + group.getGroupType().toLowerCase() + " "
        + group.getName() + " at " + interview.getPrettyFormattedTimeInterval();
    sendGroupNotificationToAdmins(
        group,
        msg,
        getNotificationEntityType(group),
        NotificationType.JOINED,
        NotificationStatus.INFO,
        group.getId()
    );
  }

  public <T extends Group> void sendUserDeclinedJoinInvite(User user, T group) {
    String msg = user.getName() + " declined to join the project "
        + group.getGroupType().toLowerCase() + " " + group.getName();
    sendGroupNotificationToAdmins(
        group,
        msg,
        getNotificationEntityType(group),
        NotificationType.JOIN_INVITATION,
        NotificationStatus.DECLINED_INVITE,
        group.getId()
    );
  }

  public void sendUserAppliedNotification(GroupApplication application) {
    User user = application.getSender();
    Group group = application.getGroup();
    String msg = user.getName() + " has applied to the " + group.getGroupType().toLowerCase() + " " + group.getName();
    sendGroupNotificationToAdmins(
        group,
        msg,
        getNotificationEntityType(group),
        NotificationType.APPLICATION,
        NotificationStatus.INFO,
        application.getId()
    );
  }

  public <T extends Group, A extends GroupApplication<T>> void markAsDoneForApplicant(A application) {
    NotificationEntityNames notificationInfo = Notification.getNotificationEntities(
        getNotificationEntityType(application.getGroup()),
        NotificationType.APPLICATION,
        NotificationStatus.INFO
    );
    notificationRepository.getNotificationsByData(
        application.getId(),
        notificationInfo.dataType
    ).forEach(this::markNotificationDone);
  }

  public <T extends Group, A extends GroupApplication<T>> void markInvitationsAsDoneFor(A application) {
    Group group = application.getGroup();
    User applicant = application.getSender();
    if (isGroupProject(group)) {
      projectInvitationRepository.findByReceiver(applicant).stream()
          .filter(p -> p.getApplicant() != null && p.getApplicant().getId() == application.getId())
          .forEach(this::markInvitationNotificationsAsDone);
    } else if (isGroupOrganzization(group)) {
      organizationInvitationRepository.findByReceiver(applicant).stream()
          .filter(o -> o.getApplicant() != null && o.getApplicant().getId() == application.getId())
          .forEach(this::markInvitationNotificationsAsDone);
    }
  }

  public <T extends Group, I extends GroupInvitation<T>> void markInvitationNotificationsAsDone(I invitation) {
    NotificationEntityNames notificationInfo = Notification.getNotificationEntities(
        getNotificationEntityType(invitation.getGroup()),
        NotificationType.JOIN_INVITATION,
        NotificationStatus.PENDING_INVITE
    );
    notificationRepository.getNotificationsByDataAndType(
        invitation.getId(),
        notificationInfo.dataType,
        notificationInfo.notificationType
    ).forEach(this::markNotificationDone);
  }

  private void markNotificationDone(Notification notification) {
    notification.setHasRead(true);
    notification.setActionState(Notification.NotificationActionState.DONE);
    notificationRepository.save(notification);
  }

  private NotificationEntity getNotificationEntityType(Group group) {
    return isGroupProject(group) ?
        NotificationEntity.PROJECT :
        NotificationEntity.ORGANIZATION;
  }

  private boolean isGroupProject(Group group) {
    return group.getEsIndex().equalsIgnoreCase("projects");
  }

  private boolean isGroupOrganzization(Group group) {
    return group.getEsIndex().equalsIgnoreCase("organizations");
  }

  private void sendNotification(
      User user,
      String message,
      NotificationEntity dataType,
      NotificationType notificationType,
      NotificationStatus notificationStatus,
      long id
  ) throws IllegalArgumentException {

    Notification notification = new Notification();
    notification.setReceiver(user);
    notification.setMessage(message);
    notification.setHasRead(false);
    ZonedDateTime now = ZonedDateTime.now();
    notification.setTime(now.toString());
    notification.setInfo(dataType, notificationType, notificationStatus);

    notification.setObjectId(id);
    notificationRepository.save(notification);
  }

  private <T extends Group> void sendGroupNotificationToAdmins(
      T group,
      String message,
      NotificationEntity dataType,
      NotificationType notificationType,
      NotificationStatus notificationStatus,
      long id
  ) throws IllegalArgumentException {
    if (isGroupProject(group)) {
      final Set<User> uniqueUsers = new HashSet<>(projectMemberRepository.getUsersByGroup((Project) group));
      uniqueUsers.stream()
          .filter(u -> projectMemberRepository.getRoles((Project) group, u).stream()
              .filter(r -> r == ADMIN || r == OWNER)
              .collect(Collectors.toList())
              .size() > 0)
          .forEach(u -> sendNotification(u, message, dataType, notificationType, notificationStatus, id));

    } else if (isGroupOrganzization(group)) {
      final Set<User> uniqueUsers = new HashSet<>(organizationMemberRepository.getUsersByGroup((Organization) group));
      uniqueUsers.stream()
          .filter(u -> organizationMemberRepository.getRoles((Organization) group, u).stream()
              .filter(r -> r == ADMIN || r == OWNER)
              .collect(Collectors.toList())
              .size() > 0)
          .forEach(u -> sendNotification(u, message, dataType, notificationType, notificationStatus, id));
    } else {
      throw new IllegalArgumentException("Unknown group type " + group.getClass().getName());
    }
  }

  @CrossOrigin
  @ApiOperation(value = "Mark notification as read")
  @PutMapping(path = "/{id}/read")
  @ResponseBody
  public BaseResponse readNotification(
      @ApiParam(value = "Id of notification to mark")
      @PathVariable(value = "id") Long id,
      HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    Notification notification = notificationRepository.findOne(id);
    if (!Objects.equals(notification.getReceiver().getId(), session.get().getUser().getId())) {
      errors.add(INSUFFICIENT_PRIVELAGES);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    notification.setHasRead(true);
    notificationRepository.save(notification);
    return new GeneralResponse(response, OK);
  }

  @CrossOrigin
  @PutMapping(path = "/{id}/done")
  @ResponseBody
  public GeneralResponse actionDone(@PathVariable(value = "id") Long id, HttpServletRequest request, HttpServletResponse response) throws Exception {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    Notification notification = notificationRepository.findOne(id);
    if (!Objects.equals(notification.getReceiver().getId(), session.get().getUser().getId())) {
      errors.add(INSUFFICIENT_PRIVELAGES);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    notification.setAction_done(true);
    notificationRepository.save(notification);
    return new GeneralResponse(response, OK);
  }

  @CrossOrigin
  @PutMapping(path = "/{id}/not-done")
  @ResponseBody
  public GeneralResponse actionNotDone(@PathVariable(value = "id") Long id, HttpServletRequest request, HttpServletResponse response) throws Exception {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    Notification notification = notificationRepository.findOne(id);
    if (!Objects.equals(notification.getReceiver().getId(), session.get().getUser().getId())) {
      errors.add(INSUFFICIENT_PRIVELAGES);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    notification.setAction_done(false);
    notificationRepository.save(notification);
    return new GeneralResponse(response, OK);
  }

  @CrossOrigin
  @ApiOperation(value = "Delete a notification")
  @PutMapping(path = "/{id}/delete")
  @ResponseBody
  public BaseResponse deleteNotification(
      @ApiParam(value = "Id of notification to delete")
      @PathVariable(value = "id") Long id,
      HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    Notification notification = notificationRepository.findOne(id);
    if (notification.getReceiver().getId().equals(session.get().getUser().getId())) {
      errors.add(INSUFFICIENT_PRIVELAGES);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    notification.setDeleted(true);
    notificationRepository.save(notification);
    return new GeneralResponse(response, OK);
  }

  @ApiOperation(value = "Get all notifications")
  @GetMapping
  @ResponseBody
  public TypedResponse<List<Notification>> getAllNotifications(HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, BaseResponse.Status.DENIED, errors);
    }
    return new TypedResponse<>(response, OK, null, populateNotifications(notificationRepository.getNotifications(session.get().getUser())));
  }

  @ApiOperation(value = "Get all notifications by status")
  @GetMapping(path = "/{status}")
  @ResponseBody
  public TypedResponse<List<Notification>> getNotifications(
      @ApiParam(value = "There are three kinds of status: all, read, unread")
      @PathVariable(value = "status") String status,
      HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, BaseResponse.Status.DENIED, errors);
    }
    FuseSession s = session.get();
    switch (status) {
      case "all":
        return new TypedResponse<>(response, OK, null, populateNotifications(notificationRepository.getNotifications(s.getUser())));
      case "read":
        return new TypedResponse<>(response, OK, null, populateNotifications(notificationRepository.getReadNotifications(s.getUser())));
      case "unread":
        return new TypedResponse<>(response, OK, null, populateNotifications(notificationRepository.getUnreadNotifications(s.getUser())));
      default:
        errors.add(INVALID_FIELDS);
        return new TypedResponse<>(response, BaseResponse.Status.BAD_DATA, errors);
    }
  }

  private <T extends Group, I extends GroupInvitation<T>> List<Notification> populateNotifications(List<Notification> notifications) {
    List<Notification> list = notifications.stream().map(
        notification -> {
          if (notification == null || notification.getData_type() == null || notification.getObjectId() == null) {
            return notification;
          }
          switch (notification.getData_type()) {
            case "ProjectApplicant":
              notification.setData(projectApplicantRepository.findOne(notification.getObjectId()));
              break;
            case "ProjectInvitation":
            case "ProjectInterview":
              notification.setData(projectInvitationRepository.findOne(notification.getObjectId()));
              break;
            case "ProjectJoined":
              notification.setData(projectRepository.findOne(notification.getObjectId()));
              break;

            case "OrganizationApplicant":
              notification.setData(organizationApplicantRepository.findOne(notification.getObjectId()));
              break;
            case "OrganizationInvitation":
            case "OrganizationInterview":
              notification.setData(organizationInvitationRepository.findOne(notification.getObjectId()));
              break;
            case "OrganizationJoined":
              notification.setData(organizationRepository.findOne(notification.getObjectId()));
              break;

            case "FriendRequest":
              notification.setData(friendRepository.findOne(notification.getObjectId()));  //return the sender info
              break;
          }
          return notification;
        }
    ).collect(Collectors.toList());
    list.sort((o1, o2) -> o2.getDateTime().compareTo(o1.getDateTime()));
    return list;
  }
}