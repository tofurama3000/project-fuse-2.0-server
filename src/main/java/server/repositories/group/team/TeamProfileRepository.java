package server.repositories.group.team;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.group.GroupProfile;
import server.entities.dto.group.team.Team;
import server.entities.dto.group.team.TeamProfile;
import server.repositories.group.GroupProfileRepository;


public interface TeamProfileRepository extends GroupProfileRepository<TeamProfile> {

}
