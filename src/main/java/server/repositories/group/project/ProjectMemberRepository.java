package server.repositories.group.project;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import server.entities.dto.User;
import server.entities.dto.group.project.Project;
import server.entities.dto.group.project.ProjectMember;
import server.repositories.group.GroupMemberRepository;

import java.util.List;

@Transactional
public interface ProjectMemberRepository extends GroupMemberRepository<Project, ProjectMember> {
  @Query("SELECT user From ProjectMember a where a.project = :group")
  List<User> getUsersByGroup(@Param("group") Project group);

  @Query("SELECT roleId FROM ProjectMember a where a.project = :group AND a.user = :user")
  List<Integer> getRoles(@Param("group") Project group, @Param("user") User user);

  @Query("SELECT project FROM ProjectMember a where a.user = :user AND a.roleId = :roleId")
  List<Project> getGroups(@Param("user") User member, @Param("roleId") int roleId);

  @Modifying(clearAutomatically = true)
  @Query("DELETE from ProjectMember a where a.project =:group AND a.user =:user and a.roleId = :roleId")
  void delete(@Param("group") Project group, @Param("user") User user, @Param("roleId") int roleId);


}
