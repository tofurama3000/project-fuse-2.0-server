package server.repositories.project;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.User;
import server.entities.dto.project.ProjectInvitation;

import java.util.List;

public interface ProjectInvitationRepository  extends CrudRepository<ProjectInvitation, Long> {
  List<ProjectInvitation> findByReceiver(User receiver);
}
