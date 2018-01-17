package server.controllers;

import static server.constants.RoleValue.ADMIN;
import static server.constants.RoleValue.DEFAULT_USER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import server.entities.dto.User;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.project.Project;
import server.entities.dto.group.team.Team;
import server.repositories.group.organization.OrganizationMemberRepository;
import server.repositories.group.organization.OrganizationRepository;
import server.repositories.group.project.ProjectMemberRepository;
import server.repositories.group.project.ProjectRepository;
import server.repositories.group.team.TeamMemberRepository;
import server.repositories.group.team.TeamRepository;

import java.util.ArrayList;
import java.util.List;

@Controller
public class MembersOfGroupController {

  @Autowired
  private TeamMemberRepository teamMemberRepository;

  @Autowired
  private OrganizationMemberRepository organizationMemberRepository;

  @Autowired
  private ProjectMemberRepository projectMemberRepository;

  @Autowired
  private TeamRepository teamRepository;

  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private ProjectRepository projectRepository;

  public List<Team> getTeamsUserIsPartOf(User user) {
    List<Team> asDefault = teamMemberRepository.getGroups(user, DEFAULT_USER);
    List<Team> asAdmin = teamMemberRepository.getGroups(user, ADMIN);
    List<Team> all = new ArrayList<>();
    all.addAll(asDefault);
    all.addAll(asAdmin);
    return all;
  }

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
