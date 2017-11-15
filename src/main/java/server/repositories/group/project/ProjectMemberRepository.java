package server.repositories.group.project;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import server.entities.dto.User;
import server.entities.dto.group.project.Project;
import server.entities.dto.group.project.ProjectMember;
import server.repositories.group.GroupMemberRepository;

@Transactional
public interface ProjectMemberRepository extends GroupMemberRepository<Project, ProjectMember> {
  @Query("SELECT user From ProjectMember a where a.project = :group")
  Iterable<User> getUsersByGroup(@Param("group") Project group);

  @Query("SELECT roleId FROM ProjectMember a where a.project = :group AND a.user = :user")
  Iterable<Integer> getRoles(@Param("group") Project group, @Param("user") User user);
}
