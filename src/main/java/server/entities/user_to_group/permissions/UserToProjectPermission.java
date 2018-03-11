package server.entities.user_to_group.permissions;

import lombok.Setter;
import org.hibernate.Session;
import server.entities.dto.group.project.Project;
import server.entities.dto.user.User;
import server.repositories.group.project.ProjectMemberRepository;

public class UserToProjectPermission extends UserToGroupPermission<Project> {

  @Setter
  private ProjectMemberRepository repository;

  @Setter
  private Session session;

  public UserToProjectPermission(User user, Project group) {
    super(user, group);
  }

  @Override
  protected Session getSession() {
    return session;
  }

  @Override
  public Iterable<Integer> getRoles() {
    return repository.getRoles(group, user);
  }
}
