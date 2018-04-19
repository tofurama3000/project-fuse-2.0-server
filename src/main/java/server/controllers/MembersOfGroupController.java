package server.controllers;

import static server.constants.RoleValue.ADMIN;
import static server.constants.RoleValue.DEFAULT_USER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.project.Project;
import server.entities.dto.user.User;
import server.repositories.group.organization.OrganizationMemberRepository;
import server.repositories.group.organization.OrganizationRepository;
import server.repositories.group.project.ProjectMemberRepository;
import server.repositories.group.project.ProjectRepository;

import java.util.ArrayList;
import java.util.List;

@Controller
public class MembersOfGroupController {
  @Autowired
  private OrganizationMemberRepository organizationMemberRepository;

  @Autowired
  private ProjectMemberRepository projectMemberRepository;

  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private ProjectRepository projectRepository;

  public List<Organization> getOrganizationsUserIsPartOf(User user) {
    List<Organization> asDefault = organizationMemberRepository.getGroups(user, DEFAULT_USER);
    List<Organization> asAdmin = organizationMemberRepository.getGroups(user, ADMIN);
    List<Organization> all = new ArrayList<>();
    all.addAll(asDefault);
    all.addAll(asAdmin);
    return all;
  }

  public List<Project> getProjectsUserIsPartOf(User user) {
    List<Project> asDefault = projectMemberRepository.getGroups(user, DEFAULT_USER);
    List<Project> asAdmin = projectMemberRepository.getGroups(user, ADMIN);
    List<Project> all = new ArrayList<>();
    all.addAll(asDefault);
    all.addAll(asAdmin);
    return all;
  }
}
