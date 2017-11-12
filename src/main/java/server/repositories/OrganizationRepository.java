package server.repositories;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.Organization;

public interface OrganizationRepository extends CrudRepository<Organization, Long> {
}
