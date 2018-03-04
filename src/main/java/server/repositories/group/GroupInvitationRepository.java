package server.repositories.group;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import server.entities.dto.Notification;

import java.util.List;

/**
 * Created by TR on 1/30/2018.
 */
@NoRepositoryBean
public interface GroupInvitationRepository<T> extends CrudRepository<T, Long> {
  @Query("FROM Notification n where n.objectId = :id and n.notification_type = :notificationType")
  Notification getNotification(@Param("id") Long id, @Param("notificationType") String notificationType);
}
