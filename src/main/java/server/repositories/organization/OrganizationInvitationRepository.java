package server.repositories.organization;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.organization.OrganizationInvitation;

public interface OrganizationInvitationRepository extends CrudRepository<OrganizationInvitation, Long> {
}
