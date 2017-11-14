package server.repositories.team;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.team.TeamInvitation;

public interface TeamInvitationRepository extends CrudRepository<TeamInvitation, Long> {
}
