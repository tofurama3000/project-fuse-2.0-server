package server.repositories.project;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.project.ProjectInvitation;

public interface ProjectInvitationRepository  extends CrudRepository<ProjectInvitation, Long> {
}
