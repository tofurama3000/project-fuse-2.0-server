package server.repositories.group.project;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.group.GroupProfile;
import server.entities.dto.group.project.Project;
import server.entities.dto.group.team.TeamProfile;
import server.repositories.group.GroupProfileRepository;

public interface ProjectProfileRepository<T extends GroupProfile>  extends GroupProfileRepository<GroupProfile> {
}
