package server.repositories;

import static server.constants.Availability.AVAILABLE;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import server.entities.dto.Notification;
import server.entities.dto.User;
import server.entities.dto.group.interview.Interview;
import server.entities.dto.group.team.Team;
import server.entities.dto.group.team.TeamApplicant;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends CrudRepository<Notification, Long> {
  @Query("FROM Notification a where a.receiver = :receiver and a.deleted = 0")
  List<Notification> getNotifications(@Param("receiver") User receiver);
  @Query("FROM Notification a where a.receiver = :receiver and a.deleted = 0 and a.hasRead=1")
  List<Notification> getReadNotifications(@Param("receiver") User receiver);
  @Query("FROM Notification a where a.receiver = :receiver and a.deleted = 0 and a.hasRead=0")
  List<Notification> getUnreadNotifications(@Param("receiver") User receiver);
}