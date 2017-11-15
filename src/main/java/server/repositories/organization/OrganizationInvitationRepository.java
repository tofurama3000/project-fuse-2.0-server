package server.repositories.organization;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.User;
import server.entities.dto.group.organization.OrganizationInvitation;

import java.util.List;

public interface OrganizationInvitationRepository extends CrudRepository<OrganizationInvitation, Long> {
  List<OrganizationInvitation> findByReceiver(User receiver);
}
