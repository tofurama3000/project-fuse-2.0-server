package server.repositories.organization;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.organization.Organization;

public interface OrganizationRepository extends CrudRepository<Organization, Long> {
}
