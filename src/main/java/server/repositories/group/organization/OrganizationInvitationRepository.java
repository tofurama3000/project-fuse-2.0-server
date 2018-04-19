package server.repositories.group.organization;

import server.entities.dto.group.organization.OrganizationInvitation;
import server.entities.dto.user.User;
import server.repositories.group.GroupInvitationRepository;

import java.util.List;

public interface OrganizationInvitationRepository extends GroupInvitationRepository<OrganizationInvitation> {
  List<OrganizationInvitation> findByReceiver(User receiver);
}
