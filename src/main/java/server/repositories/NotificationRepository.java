package server.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import server.entities.dto.Notification;
import server.entities.dto.user.User;

import java.util.List;

public interface NotificationRepository extends CrudRepository<Notification, Long> {
  @Query("FROM Notification a where a.receiver = :receiver and a.deleted = 0 ORDER BY a.time DESC")
  List<Notification> getNotifications(@Param("receiver") User receiver);
  @Query("FROM Notification a where a.receiver = :receiver and a.deleted = 0 and a.hasRead=1 ORDER BY a.time DESC")
  List<Notification> getReadNotifications(@Param("receiver") User receiver);
  @Query("FROM Notification a where a.receiver = :receiver and a.deleted = 0 and a.hasRead=0 ORDER BY a.time DESC")
  List<Notification> getUnreadNotifications(@Param("receiver") User receiver);
}
