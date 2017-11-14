package server.repositories;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.organization.OrganizationMember;

public interface OrganizationMemberRepository extends CrudRepository<OrganizationMember, Long> {
}
