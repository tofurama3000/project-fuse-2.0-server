package server.repositories.group;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;


@NoRepositoryBean
public interface GroupInvitationRepository<T> extends CrudRepository<T, Long> {
}
