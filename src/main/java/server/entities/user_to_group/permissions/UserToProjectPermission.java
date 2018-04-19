package server.entities.user_to_group.permissions;

import lombok.Setter;
import org.hibernate.Session;
import server.entities.dto.group.project.Project;
import server.entities.dto.user.User;
import server.repositories.group.project.ProjectApplicantRepository;
import server.repositories.group.project.ProjectMemberRepository;

public class UserToProjectPermission extends UserToGroupPermission<Project> {

  @Setter
  private ProjectMemberRepository repository;

  @Setter
  private UserToOrganizationPermission userToOrganizationPermission;

  @Setter
  private Session session;

  @Setter
  private ProjectApplicantRepository projectApplicantRepository;

  public UserToProjectPermission(User user, Project group) {
    super(user, group);
  }

  @Override
  protected boolean allowedToJoin() {
    return userToOrganizationPermission == null || userToOrganizationPermission.isMember();
  }

  @Override
  protected Session getSession() {
    return session;
  }

  @Override
  public Iterable<Integer> getRoles() {
    return repository.getRoles(group, user);
  }

  @Override
  public boolean hasApplied() {
    return projectApplicantRepository.getNumApplications(group, user) != 0;
  }
}
