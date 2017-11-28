package server.repositories.group;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import server.entities.dto.User;
import server.entities.dto.group.Group;
import server.entities.dto.group.GroupProfile;

@NoRepositoryBean
public interface GroupProfileRepository<T extends GroupProfile> extends CrudRepository<T, Long> {

}
