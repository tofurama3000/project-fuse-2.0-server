package server.controllers.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import server.controllers.FuseSessionController;
import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.FuseSession;
import server.entities.dto.Notification;
import server.entities.dto.User;
import server.entities.dto.group.Group;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.project.Project;
import server.entities.dto.group.team.Team;
import server.entities.user_to_group.permissions.UserToGroupPermission;
import server.repositories.NotificationRepository;
import server.repositories.group.organization.OrganizationMemberRepository;
import server.repositories.group.project.ProjectMemberRepository;
import server.repositories.group.team.TeamMemberRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static server.constants.RoleValue.ADMIN;
import static server.constants.RoleValue.OWNER;
import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;
import static server.controllers.rest.response.GeneralResponse.Status.OK;

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
  private OrganizationMemberRepository organizationMemberRepository;

  public void sendNotification(User user, String message, String time) {
    Notification notification = new Notification();
    notification.setReceiver(user);
    notification.setMessage(message);
    notification.setHasRead(false);
    notification.setTime(time);
    notificationRepository.save(notification);
  }

  public void sendGroupNotificationToAdmins(T group, String message, String time) {
    String type = group.getGroupType();
    if (type.equals("Team")) {
      List<User> usersByGroup = teamMemberRepository.getUsersByGroup((Team) group);
      Set<User> s = new HashSet<>(usersByGroup);
      for (User u : s) {
        List<Integer> roleList = teamMemberRepository.getRoles((Team) group, u);

        for (int i : roleList) {
          if (i == ADMIN || i == OWNER) {
            sendNotification(u, message, time);
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
            sendNotification(u, message, time);
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
            sendNotification(u, message, time);
            break;
          }
        }
      }
    }
  }

  @CrossOrigin
  @PutMapping(path = "/read/{id}")
  @ResponseBody
  public GeneralResponse readNotification(@PathVariable(value = "id") Long id, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, GeneralResponse.Status.DENIED, errors);
    }
    Notification notification = notificationRepository.findOne(id);
    notification.setHasRead(true);
    notificationRepository.save(notification);
    return new GeneralResponse(response, OK);
  }

  @CrossOrigin
  @PutMapping(path = "/delete/{id}")
  @ResponseBody
  public GeneralResponse deleteNotification(@PathVariable(value = "id") Long id, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, GeneralResponse.Status.DENIED, errors);
    }
    Notification notification = notificationRepository.findOne(id);
    notification.setDeleted(true);
    notificationRepository.save(notification);
    return new GeneralResponse(response, OK);
  }

  @GetMapping
  @ResponseBody
  public GeneralResponse getNotifications(HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, GeneralResponse.Status.DENIED, errors);
    }

    return new GeneralResponse(response, OK, null, notificationRepository.getNotifications(session.get().getUser()));
  }
}