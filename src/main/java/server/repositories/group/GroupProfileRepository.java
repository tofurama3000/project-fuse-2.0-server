package server.repositories.group;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.group.GroupProfile;


public interface GroupProfileRepository<T extends GroupProfile> extends CrudRepository<T, Long> {

}
