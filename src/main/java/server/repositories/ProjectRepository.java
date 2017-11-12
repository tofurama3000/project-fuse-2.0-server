package server.repositories;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.Project;

public interface ProjectRepository extends CrudRepository<Project, Long> {
}
