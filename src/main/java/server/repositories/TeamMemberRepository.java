package server.repositories;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.team.TeamMember;

public interface TeamMemberRepository extends CrudRepository<TeamMember, Long> {
}
