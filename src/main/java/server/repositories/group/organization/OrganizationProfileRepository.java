package server.repositories.group.organization;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.organization.OrganizationProfile;
import server.repositories.group.GroupProfileRepository;

public interface OrganizationProfileRepository extends GroupProfileRepository<OrganizationProfile> {
}
