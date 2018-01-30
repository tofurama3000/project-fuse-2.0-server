package server.repositories.group.organization;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.User;
import server.entities.dto.group.organization.OrganizationInvitation;
import server.repositories.group.GroupInvitationRepository;

import java.util.List;

public interface OrganizationInvitationRepository extends GroupInvitationRepository<OrganizationInvitation> {
  List<OrganizationInvitation> findByReceiver(User receiver);
}
