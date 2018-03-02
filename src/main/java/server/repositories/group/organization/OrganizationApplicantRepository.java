package server.repositories.group.organization;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.organization.OrganizationApplicant;
import server.repositories.group.GroupApplicantRepository;

import java.util.List;

@Transactional
public interface OrganizationApplicantRepository extends GroupApplicantRepository<OrganizationApplicant, Organization> {

  @Query("FROM OrganizationApplicant a where a.organization = :organization and a.status = :status ")
  List<OrganizationApplicant> getApplicants(@Param("organization") Organization organization, @Param("status") String status);

  @Query("FROM OrganizationApplicant a where a.status = :status ")
  List<OrganizationApplicant> getApplicantsByStatus(@Param("status") String status);
}
