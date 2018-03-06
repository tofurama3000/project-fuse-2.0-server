package server.repositories.group;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import server.entities.dto.Notification;


@NoRepositoryBean
public interface GroupInvitationRepository<T> extends CrudRepository<T, Long> {
}
