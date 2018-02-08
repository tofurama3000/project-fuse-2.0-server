package server.controllers.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import server.controllers.FuseSessionController;
import server.controllers.rest.response.BaseResponse;
import server.controllers.rest.response.GeneralResponse;
import server.controllers.rest.response.TypedResponse;
import server.entities.dto.FuseSession;
import server.entities.dto.Notification;
import server.entities.dto.User;
import server.entities.dto.group.Group;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.project.Project;
import server.entities.dto.group.team.Team;
import server.entities.user_to_group.permissions.UserToGroupPermission;
import server.repositories.NotificationRepository;
import server.repositories.UserRepository;
import server.repositories.group.organization.OrganizationInvitationRepository;
import server.repositories.group.organization.OrganizationMemberRepository;
import server.repositories.group.organization.OrganizationRepository;
import server.repositories.group.project.ProjectInvitationRepository;
import server.repositories.group.project.ProjectMemberRepository;
import server.repositories.group.project.ProjectRepository;
import server.repositories.group.team.TeamMemberRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static server.constants.RoleValue.ADMIN;
import static server.constants.RoleValue.OWNER;
import static server.controllers.rest.response.CannedResponse.INSUFFICIENT_PRIVELAGES;
import static server.controllers.rest.response.CannedResponse.INVALID_FIELDS;
import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;
import static server.controllers.rest.response.BaseResponse.Status.OK;

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

  public void sendNotification(User user, String message, String type, long id) throws Exception {
    if (!Notification.isValidType(type)) {
      throw new Exception("Invalid type '" + type + "'");
    }
    Notification notification = new Notification();
    notification.setReceiver(user);
    notification.setMessage(message);
    notification.setHasRead(false);
    ZonedDateTime now = ZonedDateTime.now();
    notification.setTime(now.toString());
    notification.setObjectType(type);
    notification.setObjectId(id);
    notificationRepository.save(notification);
  }

  public void sendGroupNotificationToAdmins(T group, String message, String objectType, long id) throws Exception {
    if (!Notification.isValidType(objectType)) {
      throw new Exception("Invalid type '" + objectType + "'");
    }
    String type = group.getGroupType();
    if (type.equals("Team")) {
      List<User> usersByGroup = teamMemberRepository.getUsersByGroup((Team) group);
      Set<User> s = new HashSet<>(usersByGroup);
      for (User u : s) {
        List<Integer> roleList = teamMemberRepository.getRoles((Team) group, u);

        for (int i : roleList) {
          if (i == ADMIN || i == OWNER) {
            sendNotification(u, message, objectType,id);
            break;
          }
        }
      }

    } else if (type.equals("Project")) {
      List<User> usersByGroup = projectMemberRepository.getUsersByGroup((Project) group);
      Set<User> s = new HashSet<>(usersByGroup);
      for (User u : s) {
        List<Integer> roleList = projectMemberRepository.getRoles((Project) group, u);
        for (int i : roleList) {
          if (i == ADMIN || i == OWNER) {
            sendNotification(u, message,objectType,id);
            break;
          }
        }
      }
    } else if (type.equals("Organization")) {
      List<User> usersByGroup = organizationMemberRepository.getUsersByGroup((Organization) group);
      Set<User> s = new HashSet<>(usersByGroup);
      for (User u : s) {
        List<Integer> roleList = organizationMemberRepository.getRoles((Organization) group, u);
        for (int i : roleList) {
          if (i == ADMIN || i == OWNER) {
            sendNotification(u, message,objectType,id);
            break;
          }
        }
      }
    }
  }

  @CrossOrigin
  @PutMapping(path = "/{id}/read")
  @ResponseBody
  public GeneralResponse readNotification(@PathVariable(value = "id") Long id, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    Notification notification = notificationRepository.findOne(id);
    if(!Objects.equals(notification.getReceiver().getId(), session.get().getUser().getId())){
      errors.add(INSUFFICIENT_PRIVELAGES);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    notification.setHasRead(true);
    notificationRepository.save(notification);
    return new GeneralResponse(response, OK);
  }

  @CrossOrigin
  @PutMapping(path = "/{id}/delete")
  @ResponseBody
  public GeneralResponse deleteNotification(@PathVariable(value = "id") Long id, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    Notification notification = notificationRepository.findOne(id);
    if(notification.getReceiver().getId()!=session.get().getUser().getId()){
      errors.add(INSUFFICIENT_PRIVELAGES);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    notification.setDeleted(true);
    notificationRepository.save(notification);
    return new GeneralResponse(response, OK);
  }

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

  @GetMapping(path = "/{status}")
  @ResponseBody
  public TypedResponse<List<Notification>> getNotifications(@PathVariable(value = "status") String status,HttpServletRequest request, HttpServletResponse response) {
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
          if (notification == null || notification.getObjectType() == null || notification.getObjectId() == null) {
            return notification;
          }
          Map<String, Object> d = new HashMap<>();
          switch(notification.getObjectType()) {
            case "OrganizationApplicant":
              notification.setData(organizationRepository.findOne(notification.getObjectId()));
              break;
            case "ProjectApplicant":
              notification.setData(projectRepository.findOne(notification.getObjectId()));
              break;
            case "ProjectInterview:Invite":
            case "ProjectInvitation":
              notification.setData(projectInvitationRepository.findOne(notification.getObjectId()));
              break;
            case "OrganizationInterview:Invite":
            case "OrganizationInvitation":
              notification.setData(organizationInvitationRepository.findOne(notification.getObjectId()));
              break;
            case "Friend:Accepted":
            case "Friend:Request":
              notification.setData(userRepository.findOne(notification.getObjectId()));
              break;
          }
          return notification;
        }
    ).collect(Collectors.toList());
  }
}