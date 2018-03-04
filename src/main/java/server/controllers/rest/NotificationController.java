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
import server.entities.dto.group.Group;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.project.Project;
import server.entities.dto.user.User;
import server.repositories.NotificationRepository;
import server.repositories.UserRepository;
import server.repositories.group.organization.OrganizationApplicantRepository;
import server.repositories.group.organization.OrganizationInvitationRepository;
import server.repositories.group.organization.OrganizationMemberRepository;
import server.repositories.group.organization.OrganizationRepository;
import server.repositories.group.project.ProjectApplicantRepository;
import server.repositories.group.project.ProjectInvitationRepository;
import server.repositories.group.project.ProjectMemberRepository;
import server.repositories.group.project.ProjectRepository;
import server.repositories.group.team.TeamMemberRepository;

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
public class NotificationController<T extends Group> {

  @Autowired
  private FuseSessionController fuseSessionController;

  @Autowired
  private NotificationRepository notificationRepository;

  @Autowired
  private TeamMemberRepository teamMemberRepository;

  @Autowired
  private ProjectMemberRepository projectMemberRepository;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private OrganizationMemberRepository organizationMemberRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private OrganizationInvitationRepository organizationInvitationRepository;

  @Autowired
  private ProjectInvitationRepository projectInvitationRepository;

  @Autowired
  private ProjectApplicantRepository projectApplicantRepository;

  @Autowired
  private OrganizationApplicantRepository organizationApplicantRepository;


  public void sendNotification(User user, String message, String dataType, String notificationType, long id) throws Exception {
    if (!Notification.isValidNotificationType(notificationType)) {
      throw new Exception("Invalid notification type '" + notificationType + "'");
    }
    if (!Notification.isValidDataType(dataType)) {
      throw new Exception("Invalid data type '" + dataType + "'");
    }
    Notification notification = new Notification();
    notification.setReceiver(user);
    notification.setMessage(message);
    notification.setHasRead(false);
    ZonedDateTime now = ZonedDateTime.now();
    notification.setTime(now.toString());
    notification.setNotification_type(notificationType);
    notification.setData_type(dataType);

    notification.setObjectId(id);
    notificationRepository.save(notification);
  }

  public void sendGroupNotificationToAdmins(T group, String message, String dataType, String notificationType, long id) throws Exception {
    if (!Notification.isValidNotificationType(notificationType)) {
      throw new Exception("Invalid notification type '" + notificationType + "'");
    }
    if (!Notification.isValidDataType(dataType)) {
      throw new Exception("Invalid data type '" + dataType + "'");
    }
    String type = group.getGroupType();

    if (type.equals("Project")) {
      List<User> usersByGroup = projectMemberRepository.getUsersByGroup((Project) group);
      Set<User> s = new HashSet<>(usersByGroup);
      for (User user : s) {
        List<Integer> roles = projectMemberRepository.getRoles((Project) group, user);
        for (int role : roles) {
          if (role == ADMIN || role == OWNER) {
            sendNotification(user, message, dataType, notificationType, id);
            break;
          }
        }
      }
    } else if (type.equals("Organization")) {
      List<User> usersByGroup = organizationMemberRepository.getUsersByGroup((Organization) group);
      Set<User> s = new HashSet<>(usersByGroup);
      for (User user : s) {
        List<Integer> roles = organizationMemberRepository.getRoles((Organization) group, user);
        for (int role : roles) {
          if (role == ADMIN || role == OWNER) {
            sendNotification(user, message, dataType, notificationType, id);
            break;
          }
        }
      }
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
    if (notification.getReceiver().getId() != session.get().getUser().getId()) {
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

  private List<Notification> populateNotifications(List<Notification> notifications) {
    return notifications.stream().map(
        notification -> {
          if (notification == null || notification.getData_type() == null || notification.getObjectId() == null) {
            return notification;
          }
          switch (notification.getData_type()) {

            case "ProjectApplicant":
              notification.setData(projectApplicantRepository.findOne(notification.getObjectId()));
              break;
            case "ProjectInvitation":
              notification.setData(projectInvitationRepository.findOne(notification.getObjectId()));
              break;

            case "OrganizationApplicant":
              notification.setData(organizationApplicantRepository.findOne(notification.getObjectId()));
              break;
            case "OrganizationInvitation":
              notification.setData(organizationInvitationRepository.findOne(notification.getObjectId()));
              break;
            case "Friend":
              notification.setData(userRepository.findOne(notification.getObjectId()));  //return the sender info
              break;
          }
          return notification;
        }
    ).collect(Collectors.toList());
  }
}