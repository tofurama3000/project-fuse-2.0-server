package server.repositories.group.organization;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import server.entities.dto.user.User;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.organization.OrganizationMember;
import server.repositories.group.GroupMemberRepository;

import java.util.List;

@Transactional
public interface OrganizationMemberRepository extends GroupMemberRepository<Organization, OrganizationMember> {
  @Query("SELECT user From OrganizationMember a where a.organization = :group")
  List<User> getUsersByGroup(@Param("group") Organization group);

  @Query("SELECT roleId FROM OrganizationMember a where a.organization = :group AND a.user = :user")
  List<Integer> getRoles(@Param("group") Organization group, @Param("user") User user);

  @Query("SELECT organization FROM OrganizationMember a where a.user = :user AND a.roleId = :roleId")
  List<Organization> getGroups(@Param("user") User member, @Param("roleId") int roleId);

  @Modifying(clearAutomatically = true)
  @Query("DELETE from OrganizationMember a where a.organization =:group AND a.user =:user and a.roleId = :roleId")
  void delete(@Param("group") Organization group, @Param("user") User user, @Param("roleId") int roleId);
}
