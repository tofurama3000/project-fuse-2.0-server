package server.repositories.team;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.group.team.TeamMember;

public interface TeamMemberRepository extends CrudRepository<TeamMember, Long> {
}
