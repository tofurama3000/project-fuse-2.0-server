package server.permissions;

import lombok.Setter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import server.entities.dto.User;
import server.entities.dto.project.Project;
import server.repositories.ProjectMemberRepository;

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

}
