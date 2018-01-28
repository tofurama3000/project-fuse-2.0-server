package server.repositories.group.organization;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.user.User;
import server.entities.dto.group.organization.OrganizationInvitation;

import java.util.List;

public interface OrganizationInvitationRepository extends CrudRepository<OrganizationInvitation, Long> {
  List<OrganizationInvitation> findByReceiver(User receiver);
}
