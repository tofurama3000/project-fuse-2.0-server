package server.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.controllers.rest.NotificationController;
import server.entities.dto.Notification;
import server.entities.dto.group.Group;
import server.repositories.NotificationRepository;

@Component
public class NotificationHandler {
  private final NotificationRepository notificationRepository;
  private final NotificationController notificationController;

  @Autowired
  public NotificationHandler(NotificationRepository notificationRepository, NotificationController notificationController) {
    this.notificationRepository = notificationRepository;
    this.notificationController = notificationController;
  }

  public void markNotificationDone(Notification notification) {
    notification.setHasRead(true);
    notification.setAction_done(true);
    notificationRepository.save(notification);
  }

  @SuppressWarnings("unchecked")
  public void sendGroupNotificationToAdmins(Group group, String message, String dataType, String notificationType, long id) throws Exception {
    notificationController.sendGroupNotificationToAdmins(group, message, dataType, notificationType, id);
  }
}
