package server.repositories.group.organization;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import server.entities.dto.User;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.organization.OrganizationMember;
import server.repositories.group.GroupMemberRepository;

@Transactional
public interface OrganizationMemberRepository extends GroupMemberRepository<Organization, OrganizationMember> {
  @Query("SELECT user From OrganizationMember a where a.organization = :group")
  Iterable<User> getUsersByGroup(@Param("group") Organization group);
}
