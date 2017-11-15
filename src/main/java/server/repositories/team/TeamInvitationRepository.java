package server.repositories.team;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.User;
import server.entities.dto.team.TeamInvitation;

import java.util.List;


public interface TeamInvitationRepository extends CrudRepository<TeamInvitation, Long> {
  List<TeamInvitation> findByReceiver(User receiver);
}
