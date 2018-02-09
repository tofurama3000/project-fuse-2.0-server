package server.repositories.group.team;

import server.entities.dto.group.team.TeamInvitation;
import server.entities.dto.user.User;
import server.repositories.group.GroupInvitationRepository;

import java.util.List;


public interface TeamInvitationRepository extends GroupInvitationRepository<TeamInvitation> {
  List<TeamInvitation> findByReceiver(User receiver);
}
