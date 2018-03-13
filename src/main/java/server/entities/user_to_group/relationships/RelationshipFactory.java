package server.entities.user_to_group.relationships;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.project.Project;
import server.entities.dto.user.User;
import server.repositories.group.organization.OrganizationMemberRepository;
import server.repositories.group.project.ProjectMemberRepository;

@Service
public class RelationshipFactory {

  @Autowired
  private OrganizationMemberRepository organizationMemberRepository;

  @Autowired
  private ProjectMemberRepository projectMemberRepository;

  public UserToProjectRelationship createUserToProjectRelationship(User user, Project project) {
    UserToProjectRelationship relationship = new UserToProjectRelationship(user, project);
    relationship.setProjectMemberRepository(projectMemberRepository);
    return relationship;
  }

  public UserToOrganizationRelationship createUserToOrganizationRelationship(User user, Organization organization) {
    UserToOrganizationRelationship relationship = new UserToOrganizationRelationship(user, organization);
    relationship.setOrganizationMemberRepository(organizationMemberRepository);
    return relationship;
  }
}
