package server.service;

import org.elasticsearch.common.inject.Singleton;
import org.springframework.beans.factory.annotation.Autowired;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.project.Project;
import server.entities.dto.user.User;
import server.repositories.UserRepository;
import server.repositories.group.organization.OrganizationRepository;
import server.repositories.group.project.ProjectRepository;

import java.util.Optional;

//Probably an anti pattern but it makes it easy
@Singleton
public class EntityFinder {

  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private UserRepository userRepository;

  @SuppressWarnings("unchecked")
  public <T> Optional<T> findEntity(Long id, Class<T> typeClass) {
    switch (typeClass.getSimpleName()) {
      case "Organization":
        return Optional.ofNullable((T) organizationRepository.findOne(id));
      case "Project":
        return Optional.ofNullable((T) projectRepository.findOne(id));
      case "User":
        return Optional.ofNullable((T) userRepository.findOne(id));
      default:
        return Optional.empty();
    }
  }

  public Optional<Class> getClassOfType(String type) {
    switch (type) {
      case "Organization":
        return Optional.of(Organization.class);
      case "Project":
        return Optional.of(Project.class);
      case "User":
        return Optional.of(User.class);
      default:
        return Optional.empty();
    }
  }
}
