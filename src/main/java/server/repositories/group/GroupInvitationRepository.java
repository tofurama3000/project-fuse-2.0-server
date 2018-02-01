package server.repositories.group;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Created by TR on 1/30/2018.
 */
@NoRepositoryBean
public interface GroupInvitationRepository<T> extends CrudRepository<T, Long> {

}
