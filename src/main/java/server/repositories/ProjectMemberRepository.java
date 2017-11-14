package server.repositories;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.project.ProjectMember;

public interface ProjectMemberRepository extends CrudRepository<ProjectMember, Long> {
}
