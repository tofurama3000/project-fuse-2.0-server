package server.permissions;

import lombok.Setter;
import org.hibernate.Session;
import server.entities.dto.User;
import server.entities.dto.group.project.Project;
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
  protected String getGroupFieldName() {
    return "project";
  }

  @Override
  protected Iterable<Integer> getRoles() {
    return repository.getRoles(group, user);
  }

}
