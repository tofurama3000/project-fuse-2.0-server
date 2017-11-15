package server.repositories.organization;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.group.organization.OrganizationMember;

public interface OrganizationMemberRepository extends CrudRepository<OrganizationMember, Long> {
}
