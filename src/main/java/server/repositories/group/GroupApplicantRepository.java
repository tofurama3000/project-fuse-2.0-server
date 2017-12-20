package server.repositories.group;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.group.GroupApplicant;

public interface GroupApplicantRepository<T extends GroupApplicant> extends CrudRepository<T, Long> {

}
